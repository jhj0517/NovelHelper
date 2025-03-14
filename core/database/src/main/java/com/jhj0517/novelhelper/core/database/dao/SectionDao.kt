package com.jhj0517.novelhelper.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jhj0517.novelhelper.core.database.entity.SectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(section: SectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sections: List<SectionEntity>)

    @Update
    suspend fun update(section: SectionEntity)

    @Delete
    suspend fun delete(section: SectionEntity)

    @Query("SELECT * FROM sections WHERE id = :id")
    suspend fun getSectionById(id: String): SectionEntity?

    @Query("SELECT * FROM sections WHERE versionId = :versionId ORDER BY `order`")
    fun getSectionsByVersionIdFlow(versionId: String): Flow<List<SectionEntity>>

    @Query("DELETE FROM sections WHERE versionId = :versionId")
    suspend fun deleteSectionsByVersionId(versionId: String)
} 