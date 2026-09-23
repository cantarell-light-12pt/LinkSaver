package it.cantarell.linksaver.ui.addlink

import it.cantarell.linksaver.data.Link
import it.cantarell.linksaver.data.LinkRepository
import it.cantarell.linksaver.data.UrlError
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

private class FakeLinkRepository : LinkRepository {
    val saved = mutableListOf<Link>()
    var failure: Exception? = null
    var gate: CompletableDeferred<Unit>? = null

    override suspend fun add(link: Link): Long {
        gate?.await()
        failure?.let { throw it }
        saved += link
        return saved.size.toLong()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AddLinkViewModelTest {
    private val now = Instant.parse("2026-09-23T17:30:21Z")
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
    private val repository = FakeLinkRepository()
    private lateinit var viewModel: AddLinkViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = AddLinkViewModel(repository, Clock.fixed(now, ZoneOffset.UTC))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val state get() = viewModel.uiState.value

    @Test
    fun fieldEvents_updateState() {
        viewModel.onEvent(AddLinkEvent.UrlChanged("example.com"))
        viewModel.onEvent(AddLinkEvent.NameChanged("Example"))
        viewModel.onEvent(AddLinkEvent.IconChanged("🔗"))
        viewModel.onEvent(AddLinkEvent.CategoryChanged("Work"))
        viewModel.onEvent(AddLinkEvent.TagInputChanged("a"))

        assertEquals(
            AddLinkUiState(url = "example.com", name = "Example", icon = "🔗", category = "Work", tagInput = "a"),
            state,
        )
    }

    @Test
    fun addTag_mergesInputAndClearsIt() {
        viewModel.onEvent(AddLinkEvent.TagInputChanged("news, tech"))
        viewModel.onEvent(AddLinkEvent.AddTag)
        viewModel.onEvent(AddLinkEvent.TagInputChanged("NEWS"))
        viewModel.onEvent(AddLinkEvent.AddTag)

        assertEquals(listOf("news", "tech"), state.tags)
        assertEquals("", state.tagInput)
    }

    @Test
    fun removeTag_removesIt() {
        viewModel.onEvent(AddLinkEvent.TagInputChanged("news, tech"))
        viewModel.onEvent(AddLinkEvent.AddTag)
        viewModel.onEvent(AddLinkEvent.RemoveTag("news"))

        assertEquals(listOf("tech"), state.tags)
    }

    @Test
    fun save_withAllFields_savesLinkAndResetsForm() = runTest(dispatcher) {
        viewModel.onEvent(AddLinkEvent.UrlChanged(" example.com "))
        viewModel.onEvent(AddLinkEvent.NameChanged(" Example "))
        viewModel.onEvent(AddLinkEvent.IconChanged(" 🔗 "))
        viewModel.onEvent(AddLinkEvent.CategoryChanged(" Work "))
        viewModel.onEvent(AddLinkEvent.TagInputChanged("news"))
        viewModel.onEvent(AddLinkEvent.AddTag)
        viewModel.onEvent(AddLinkEvent.TagInputChanged("pending"))

        viewModel.onEvent(AddLinkEvent.Save)
        assertTrue(state.isSaving)
        advanceUntilIdle()

        assertEquals(
            listOf(
                Link(
                    name = "Example",
                    url = "https://example.com",
                    icon = "🔗",
                    category = "Work",
                    tags = listOf("news", "pending"),
                    createdAt = now,
                ),
            ),
            repository.saved,
        )
        assertEquals(AddLinkUiState(savedMessagePending = true), state)

        viewModel.onEvent(AddLinkEvent.SavedMessageShown)
        assertFalse(state.savedMessagePending)
    }

    @Test
    fun save_withOnlyUrl_defaultsNameToUrlAndOptionalFieldsToEmpty() = runTest(dispatcher) {
        viewModel.onEvent(AddLinkEvent.UrlChanged("https://example.com"))
        viewModel.onEvent(AddLinkEvent.NameChanged("   "))
        viewModel.onEvent(AddLinkEvent.CategoryChanged("  "))

        viewModel.onEvent(AddLinkEvent.Save)
        advanceUntilIdle()

        val link = repository.saved.single()
        assertEquals("https://example.com", link.name)
        assertNull(link.icon)
        assertNull(link.category)
        assertEquals(emptyList<String>(), link.tags)
        assertEquals(now, link.createdAt)
    }

    @Test
    fun save_withEmptyUrl_showsErrorWithoutSaving() = runTest(dispatcher) {
        viewModel.onEvent(AddLinkEvent.Save)
        advanceUntilIdle()

        assertEquals(UrlError.EMPTY, state.urlError)
        assertFalse(state.iconError)
        assertTrue(repository.saved.isEmpty())
    }

    @Test
    fun save_withInvalidUrlAndIcon_showsBothErrors() = runTest(dispatcher) {
        viewModel.onEvent(AddLinkEvent.UrlChanged("not a url"))
        viewModel.onEvent(AddLinkEvent.IconChanged("ab"))

        viewModel.onEvent(AddLinkEvent.Save)
        advanceUntilIdle()

        assertEquals(UrlError.INVALID, state.urlError)
        assertTrue(state.iconError)
        assertTrue(repository.saved.isEmpty())
    }

    @Test
    fun save_withValidUrlAndInvalidIcon_showsOnlyIconError() = runTest(dispatcher) {
        viewModel.onEvent(AddLinkEvent.UrlChanged("example.com"))
        viewModel.onEvent(AddLinkEvent.IconChanged("x"))

        viewModel.onEvent(AddLinkEvent.Save)
        advanceUntilIdle()

        assertNull(state.urlError)
        assertTrue(state.iconError)
        assertTrue(repository.saved.isEmpty())
    }

    @Test
    fun editingField_clearsItsError() {
        viewModel.onEvent(AddLinkEvent.IconChanged("x"))
        viewModel.onEvent(AddLinkEvent.Save)

        viewModel.onEvent(AddLinkEvent.UrlChanged("e"))
        assertNull(state.urlError)
        assertTrue(state.iconError)

        viewModel.onEvent(AddLinkEvent.IconChanged("🔗"))
        assertFalse(state.iconError)
    }

    @Test
    fun save_whileSaving_isIgnored() = runTest(dispatcher) {
        repository.gate = CompletableDeferred()
        viewModel.onEvent(AddLinkEvent.UrlChanged("example.com"))
        viewModel.onEvent(AddLinkEvent.Save)
        advanceUntilIdle()
        viewModel.onEvent(AddLinkEvent.Save)

        repository.gate?.complete(Unit)
        advanceUntilIdle()

        assertEquals(1, repository.saved.size)
    }

    @Test
    fun save_whenRepositoryFails_keepsInputAndReportsError() = runTest(dispatcher) {
        repository.failure = IOException("disk full")
        viewModel.onEvent(AddLinkEvent.UrlChanged("example.com"))

        viewModel.onEvent(AddLinkEvent.Save)
        advanceUntilIdle()

        assertTrue(state.saveFailed)
        assertFalse(state.isSaving)
        assertEquals("example.com", state.url)

        viewModel.onEvent(AddLinkEvent.SaveErrorShown)
        assertFalse(state.saveFailed)
    }
}
