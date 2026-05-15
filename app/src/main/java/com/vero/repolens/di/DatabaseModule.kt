package com.vero.repolens.di

import android.content.Context
import androidx.room.Room
import com.vero.repolens.data.local.ActionItemDao
import com.vero.repolens.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // For development - remove in production
            .build()
    }
    
    @Provides
    @Singleton
    fun provideActionItemDao(database: AppDatabase): ActionItemDao {
        return database.actionItemDao()
    }
}

// Made with Bob