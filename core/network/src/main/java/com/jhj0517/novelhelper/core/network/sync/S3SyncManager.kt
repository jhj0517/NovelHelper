package com.jhj0517.novelhelper.core.network.sync

import android.content.Context
import android.net.Uri
import android.util.Log
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Manages synchronization of document content with AWS S3.
 */
@Singleton
class S3SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val s3Client: AmazonS3Client
    private val bucketName = "novel-helper-app" // Replace with your actual bucket name
    
    init {
        // Set up credentials provider
        // Note: In a real app, you would use proper authentication with Cognito
        val credentialsProvider = CognitoCachingCredentialsProvider(
            context,
            "your-identity-pool-id", // Replace with your Cognito Identity Pool ID
            Regions.US_EAST_1 // Replace with your AWS region
        )
        
        // Initialize S3 client
        s3Client = AmazonS3Client(credentialsProvider)
    }
    
    /**
     * Uploads a file to S3.
     *
     * @param localFilePath The path to the local file
     * @param objectKey The key to use for the S3 object
     * @return True if the upload was successful, false otherwise
     */
    suspend fun uploadFile(localFilePath: String, objectKey: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(localFilePath)
            if (!file.exists()) {
                Log.e(TAG, "File does not exist: $localFilePath")
                return@withContext false
            }
            
            val transferUtility = TransferUtility.builder()
                .context(context)
                .s3Client(s3Client)
                .build()
                
            val uploadObserver = transferUtility.upload(
                bucketName,
                objectKey,
                file
            )
            
            return@withContext awaitTransferCompletion(uploadObserver)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading file", e)
            return@withContext false
        }
    }
    
    /**
     * Downloads a file from S3.
     *
     * @param objectKey The key of the S3 object
     * @param destinationPath The path where the file should be saved
     * @return True if the download was successful, false otherwise
     */
    suspend fun downloadFile(objectKey: String, destinationPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(destinationPath)
            file.parentFile?.mkdirs()
            
            val transferUtility = TransferUtility.builder()
                .context(context)
                .s3Client(s3Client)
                .build()
                
            val downloadObserver = transferUtility.download(
                bucketName,
                objectKey,
                file
            )
            
            return@withContext awaitTransferCompletion(downloadObserver)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading file", e)
            return@withContext false
        }
    }
    
    /**
     * Deletes a file from S3.
     *
     * @param objectKey The key of the S3 object to delete
     * @return True if the deletion was successful, false otherwise
     */
    suspend fun deleteFile(objectKey: String): Boolean = withContext(Dispatchers.IO) {
        try {
            s3Client.deleteObject(bucketName, objectKey)
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file", e)
            return@withContext false
        }
    }
    
    /**
     * Waits for a transfer to complete.
     *
     * @param observer The transfer observer
     * @return True if the transfer completed successfully, false otherwise
     */
    private suspend fun awaitTransferCompletion(observer: TransferObserver): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val listener = object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState) {
                    if (state == TransferState.COMPLETED) {
                        continuation.resume(true)
                    } else if (state == TransferState.FAILED || state == TransferState.CANCELED) {
                        continuation.resume(false)
                    }
                }
                
                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    // Update progress if needed
                    val percentDone = (bytesCurrent.toDouble() / bytesTotal.toDouble() * 100).toInt()
                    Log.d(TAG, "Transfer progress: $percentDone%")
                }
                
                override fun onError(id: Int, ex: Exception) {
                    Log.e(TAG, "Transfer error", ex)
                    continuation.resume(false)
                }
            }
            
            observer.setTransferListener(listener)
            
            continuation.invokeOnCancellation {
                observer.cleanTransferListener()
            }
        }
    }
    
    companion object {
        private const val TAG = "S3SyncManager"
    }
} 