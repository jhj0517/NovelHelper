package com.jhj0517.novelhelper.core.database.di

import android.content.Context
import com.jhj0517.novelhelper.core.database.NovelHelperDatabase
import com.jhj0517.novelhelper.core.database.dao.BranchDao
import com.jhj0517.novelhelper.core.database.dao.DocumentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNovelHelperDatabase(@ApplicationContext context: Context): NovelHelperDatabase {
        return NovelHelperDatabase.getInstance(context)
    }

    @Provides
    fun provideDocumentDao(database: NovelHelperDatabase): DocumentDao {
        return database.documentDao()
    }

    @Provides
    fun provideBranchDao(database: NovelHelperDatabase): BranchDao {
        return database.branchDao()
    }

} 