package it.cantarell.linksaver.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class LinkDaoTest {
    private lateinit var database: LinkDatabase
    private lateinit var dao: LinkDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LinkDatabase::class.java,
        ).build()
        dao = database.linkDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insert_persistsAllFields() = runTest {
        val link = Link(
            name = "Example",
            url = "https://example.com",
            icon = "🔗",
            category = "Work",
            tags = listOf("news", "tech, stuff"),
            createdAt = Instant.parse("2026-09-23T17:30:21.123Z"),
        )

        val id = dao.insert(link)

        assertEquals(link.copy(id = id), dao.getById(id))
    }

    @Test
    fun insert_persistsLinkWithoutOptionalFields() = runTest {
        val link = Link(name = "https://example.com", url = "https://example.com", createdAt = Instant.EPOCH)

        val id = dao.insert(link)

        assertEquals(link.copy(id = id), dao.getById(id))
    }

    @Test
    fun insert_generatesDistinctIds() = runTest {
        val link = Link(name = "a", url = "https://a.com", createdAt = Instant.EPOCH)

        assertNotEquals(dao.insert(link), dao.insert(link))
    }

    @Test
    fun getById_unknownId_returnsNull() = runTest {
        assertNull(dao.getById(42))
    }

    @Test
    fun repository_add_insertsThroughDao() = runTest {
        val repository = OfflineLinkRepository(dao)
        val link = Link(name = "a", url = "https://a.com", createdAt = Instant.EPOCH)

        val id = repository.add(link)

        assertEquals(link.copy(id = id), dao.getById(id))
    }
}
