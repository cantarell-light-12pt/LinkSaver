package it.cantarell.linksaver.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Link::class], version = 1)
@TypeConverters(Converters::class)
abstract class LinkDatabase : RoomDatabase() {
    abstract fun linkDao(): LinkDao

    companion object {
        private const val NAME = "linksaver.db"

        fun create(context: Context): LinkDatabase =
            Room.databaseBuilder(context.applicationContext, LinkDatabase::class.java, NAME).build()
    }
}
