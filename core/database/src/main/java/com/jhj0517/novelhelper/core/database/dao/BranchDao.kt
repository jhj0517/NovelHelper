package com.jhj0517.novelhelper.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jhj0517.novelhelper.core.database.entity.BranchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BranchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(branch: BranchEntity): Long

    @Update
    suspend fun update(branch: BranchEntity)

    @Delete
    suspend fun delete(branch: BranchEntity)

    @Query("SELECT * FROM branches WHERE id = :id")
    suspend fun getBranchById(id: String): BranchEntity?

    @Query("SELECT * FROM branches WHERE documentId = :documentId ORDER BY updatedAt DESC")
    fun getBranchesByDocumentIdFlow(documentId: String): Flow<List<BranchEntity>>

    @Query("SELECT * FROM branches WHERE documentId = :documentId AND isMainBranch = 1 LIMIT 1")
    suspend fun getMainBranchForDocument(documentId: String): BranchEntity?
} 