package it.cantarell.linksaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LinkDao {
    @Insert
    suspend fun insert(link: Link): Long

    @Query("SELECT * FROM links WHERE id = :id")
    suspend fun getById(id: Long): Link?
}
