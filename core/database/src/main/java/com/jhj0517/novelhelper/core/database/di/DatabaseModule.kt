package com.jhj0517.novelhelper.core.database.di

import android.content.Context
import com.jhj0517.novelhelper.core.database.NovelHelperDatabase
import com.jhj0517.novelhelper.core.database.dao.BranchDao
import com.jhj0517.novelhelper.core.database.dao.DocumentDao
import com.jhj0517.novelhelper.core.database.dao.SectionDao
import com.jhj0517.novelhelper.core.database.dao.VersionDao
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

    @Provides
    fun provideVersionDao(database: NovelHelperDatabase): VersionDao {
        return database.versionDao()
    }

    @Provides
    fun provideSectionDao(database: NovelHelperDatabase): SectionDao {
        return database.sectionDao()
    }
} 