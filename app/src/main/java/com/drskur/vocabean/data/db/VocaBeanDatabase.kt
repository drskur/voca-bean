package com.drskur.vocabean.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WordSetEntity::class, WordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VocaBeanDatabase : RoomDatabase() {
    abstract fun wordSetDao(): WordSetDao

    companion object {
        const val NAME = "vocabean.db"
    }
}
