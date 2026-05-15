package com.vero.repolens.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vero.repolens.data.models.ActionItem
import com.vero.repolens.data.models.ActionItemConverters

/**
 * Room database for RepoLens app
 */
@Database(
    entities = [ActionItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ActionItemConverters::class)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * Get the ActionItem DAO
     */
    abstract fun actionItemDao(): ActionItemDao
    
    companion object {
        const val DATABASE_NAME = "repolens_database"
    }
}

// Made with Bob