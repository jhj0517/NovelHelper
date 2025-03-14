package com.jhj0517.novelhelper.core.data.repository

import android.util.Log
import com.jhj0517.novelhelper.core.database.dao.VersionDao
import com.jhj0517.novelhelper.core.database.entity.VersionEntity
import com.jhj0517.novelhelper.core.network.sync.S3SyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for syncing document content with the cloud.
 */
@Singleton
class DocumentSyncRepository @Inject constructor(
    private val versionDao: VersionDao,
    private val s3SyncManager: S3SyncManager
) {
    /**
     * Syncs all unsynced versions to the cloud.
     *
     * @return A flow of sync progress
     */
    fun syncToCloud(): Flow<SyncProgress> = flow {
        emit(SyncProgress.Started)
        
        try {
            val unsyncedVersions = versionDao.getUnsyncedVersions()
            val totalVersions = unsyncedVersions.size
            
            if (totalVersions == 0) {
                emit(SyncProgress.Completed(0))
                return@flow
            }
            
            var successCount = 0
            
            unsyncedVersions.forEachIndexed { index, version ->
                emit(SyncProgress.InProgress(index + 1, totalVersions))
                
                // Upload content file
                val contentSuccess = uploadVersionContent(version)
                
                // Upload diff file if it exists
                val diffSuccess = if (version.diffFilePath != null && version.diffFromVersionId != null) {
                    uploadVersionDiff(version.diffFromVersionId!!, version.id, version.diffFilePath!!)
                } else true
                
                if (contentSuccess && diffSuccess) {
                    versionDao.markVersionSynced(version.id)
                    successCount++
                }
            }
            
            emit(SyncProgress.Completed(successCount))
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing to cloud", e)
            emit(SyncProgress.Failed(e.message ?: "Unknown error"))
        }
    }
    
    /**
     * Uploads version content to S3.
     *
     * @param version The version entity
     * @return True if the upload was successful, false otherwise
     */
    private suspend fun uploadVersionContent(version: VersionEntity): Boolean {
        val objectKey = "versions/${version.id}/content.txt"
        return s3SyncManager.uploadFile(version.contentFilePath, objectKey)
    }
    
    /**
     * Uploads version diff to S3.
     *
     * @param fromVersionId The ID of the source version
     * @param toVersionId The ID of the target version
     * @param diffFilePath The path to the diff file
     * @return True if the upload was successful, false otherwise
     */
    private suspend fun uploadVersionDiff(
        fromVersionId: String,
        toVersionId: String,
        diffFilePath: String
    ): Boolean {
        val objectKey = "versions/diffs/${fromVersionId}_${toVersionId}.diff"
        return s3SyncManager.uploadFile(diffFilePath, objectKey)
    }
    
    companion object {
        private const val TAG = "DocumentSyncRepository"
    }
}

/**
 * Represents the progress of a sync operation.
 */
sealed class SyncProgress {
    object Started : SyncProgress()
    data class InProgress(val current: Int, val total: Int) : SyncProgress()
    data class Completed(val successCount: Int) : SyncProgress()
    data class Failed(val error: String) : SyncProgress()
} 