package it.cantarell.linksaver

import android.content.Context
import it.cantarell.linksaver.data.LinkDatabase
import it.cantarell.linksaver.data.LinkRepository
import it.cantarell.linksaver.data.OfflineLinkRepository

/** Manual dependency container, created once by [LinkSaverApplication]. */
class AppContainer(context: Context) {
    private val database by lazy { LinkDatabase.create(context) }

    val linkRepository: LinkRepository by lazy { OfflineLinkRepository(database.linkDao()) }
}
