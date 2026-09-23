package it.cantarell.linksaver.ui.addlink

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.cantarell.linksaver.data.UrlError
import it.cantarell.linksaver.ui.theme.LinkSaverTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddLinkScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val events = mutableListOf<AddLinkEvent>()

    private fun setScreen(state: AddLinkUiState) {
        composeRule.setContent {
            LinkSaverTheme {
                AddLinkScreen(state = state, onEvent = { events += it })
            }
        }
    }

    private fun field(label: String) = composeRule.onNode(hasSetTextAction() and hasText(label))

    @Test
    fun typingInFields_sendsChangeEvents() {
        setScreen(AddLinkUiState())

        field("URL").performTextInput("example.com")
        field("Name (optional)").performTextInput("Example")
        field("Icon (optional)").performTextInput("🔗")
        field("Category (optional)").performTextInput("Work")
        field("Tags (optional)").performTextInput("news")

        assertEquals(
            listOf(
                AddLinkEvent.UrlChanged("example.com"),
                AddLinkEvent.NameChanged("Example"),
                AddLinkEvent.IconChanged("🔗"),
                AddLinkEvent.CategoryChanged("Work"),
                AddLinkEvent.TagInputChanged("news"),
            ),
            events,
        )
    }

    @Test
    fun addTagButtonAndImeAction_sendAddTag() {
        setScreen(AddLinkUiState(tagInput = "news"))

        composeRule.onNodeWithContentDescription("Add tag").performClick()
        field("Tags (optional)").performImeAction()

        assertEquals(listOf(AddLinkEvent.AddTag, AddLinkEvent.AddTag), events)
    }

    @Test
    fun tags_areShownAndRemovable() {
        setScreen(AddLinkUiState(tags = listOf("news", "tech")))

        composeRule.onNodeWithText("news").assertExists()
        composeRule.onNodeWithText("tech").performClick()

        assertEquals(listOf(AddLinkEvent.RemoveTag("tech")), events)
        composeRule.onNodeWithContentDescription("Remove tag news").assertExists()
    }

    @Test
    fun saveButton_sendsSave() {
        setScreen(AddLinkUiState(url = "example.com"))

        composeRule.onNodeWithText("Save").performClick()

        assertEquals(listOf(AddLinkEvent.Save), events)
    }

    @Test
    fun saveButton_isDisabledWhileSaving() {
        setScreen(AddLinkUiState(isSaving = true))

        composeRule.onNodeWithText("Save").assertIsNotEnabled()
    }

    @Test
    fun errors_areShown() {
        setScreen(AddLinkUiState(urlError = UrlError.INVALID, iconError = true))

        composeRule.onNodeWithText("Enter a valid http or https URL").assertExists()
        composeRule.onNodeWithText("Enter a single emoji").assertExists()
    }

    @Test
    fun emptyUrlError_isShown() {
        setScreen(AddLinkUiState(urlError = UrlError.EMPTY))

        composeRule.onNodeWithText("Enter a URL").assertExists()
    }

    @Test
    fun savedAndFailedMessages_areShownAsSnackbarsAndAcknowledged() {
        var state by mutableStateOf(AddLinkUiState(savedMessagePending = true))
        composeRule.setContent {
            LinkSaverTheme {
                AddLinkScreen(state = state, onEvent = { events += it })
            }
        }

        composeRule.onNodeWithText("Link saved").assertExists()
        assertTrue(AddLinkEvent.SavedMessageShown in events)

        state = AddLinkUiState(saveFailed = true)
        composeRule.waitUntil { composeRule.onAllNodes(hasText("Could not save the link")).fetchSemanticsNodes().isNotEmpty() }
        assertTrue(AddLinkEvent.SaveErrorShown in events)
    }
}
