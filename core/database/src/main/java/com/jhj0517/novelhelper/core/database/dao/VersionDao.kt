package com.jhj0517.novelhelper.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jhj0517.novelhelper.core.database.entity.VersionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VersionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(version: VersionEntity): Long

    @Update
    suspend fun update(version: VersionEntity)

    @Delete
    suspend fun delete(version: VersionEntity)

    @Query("SELECT * FROM versions WHERE id = :id")
    suspend fun getVersionById(id: String): VersionEntity?

    @Query("SELECT * FROM versions WHERE branchId = :branchId ORDER BY createdAt DESC")
    fun getVersionsByBranchIdFlow(branchId: String): Flow<List<VersionEntity>>

    @Query("SELECT * FROM versions WHERE branchId = :branchId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestVersionForBranch(branchId: String): VersionEntity?

    @Query("SELECT * FROM versions WHERE isSyncedToCloud = 0")
    suspend fun getUnsyncedVersions(): List<VersionEntity>

    @Query("UPDATE versions SET isSyncedToCloud = 1 WHERE id = :versionId")
    suspend fun markVersionSynced(versionId: String)
} 