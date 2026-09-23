package it.cantarell.linksaver.data

fun interface LinkRepository {
    /** Saves [link] and returns its generated id. */
    suspend fun add(link: Link): Long
}

class OfflineLinkRepository(private val linkDao: LinkDao) : LinkRepository {
    override suspend fun add(link: Link): Long = linkDao.insert(link)
}
