package it.cantarell.linksaver.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * A saved link. [name] is never blank: when the user leaves it empty it defaults to [url].
 */
@Entity(tableName = "links")
data class Link(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String,
    val icon: String? = null,
    val category: String? = null,
    val tags: List<String> = emptyList(),
    val createdAt: Instant,
)
