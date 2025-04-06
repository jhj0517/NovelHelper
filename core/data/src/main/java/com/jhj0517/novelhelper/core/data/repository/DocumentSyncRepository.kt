package com.jhj0517.novelhelper.core.data.repository

import android.util.Log
import com.jhj0517.novelhelper.core.database.dao.BranchDao
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
    private val branchDao: BranchDao,
    private val s3SyncManager: S3SyncManager
) {
    /**
     * Syncs all unsynced branches to the cloud.
     *
     * @return A flow of sync progress
     */
    fun syncToCloud(): Flow<SyncProgress> = flow {
        emit(SyncProgress.Started)
        
        try {
            val unsyncedBranches = branchDao.getUnsyncedBranches()
            val totalBranches = unsyncedBranches.size
            
            if (totalBranches == 0) {
                emit(SyncProgress.Completed(0))
                return@flow
            }
            
            var successCount = 0
            
            unsyncedBranches.forEachIndexed { index, branch ->
                emit(SyncProgress.InProgress(index + 1, totalBranches))
                
                // Upload branch content
                val contentSuccess = uploadBranchContent(branch.id, branch.contentFilePath)
                
                if (contentSuccess) {
                    branchDao.markBranchSynced(branch.id)
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
     * Uploads branch content to S3.
     *
     * @param branchId The ID of the branch
     * @param contentFilePath The path to the content file
     * @return True if the upload was successful, false otherwise
     */
    private suspend fun uploadBranchContent(
        branchId: String,
        contentFilePath: String
    ): Boolean {
        val objectKey = "branches/content/${branchId}.txt"
        return s3SyncManager.uploadFile(contentFilePath, objectKey)
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