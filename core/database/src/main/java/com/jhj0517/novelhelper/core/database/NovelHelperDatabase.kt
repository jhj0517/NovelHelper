package com.jhj0517.novelhelper.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jhj0517.novelhelper.core.database.converter.LocalDateTimeConverter
import com.jhj0517.novelhelper.core.database.dao.BranchDao
import com.jhj0517.novelhelper.core.database.dao.DocumentDao
import com.jhj0517.novelhelper.core.database.entity.BranchEntity
import com.jhj0517.novelhelper.core.database.entity.DocumentEntity

@Database(
    entities = [
        DocumentEntity::class,
        BranchEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(LocalDateTimeConverter::class)
abstract class NovelHelperDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun branchDao(): BranchDao

    companion object {
        private const val DATABASE_NAME = "novelhelper.db"

        @Volatile
        private var INSTANCE: NovelHelperDatabase? = null

        fun getInstance(context: Context): NovelHelperDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NovelHelperDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 