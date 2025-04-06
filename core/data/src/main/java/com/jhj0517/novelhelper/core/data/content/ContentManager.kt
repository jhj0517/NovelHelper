package com.jhj0517.novelhelper.core.data.content

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the storage of document content in the file system.
 */
@Singleton
class ContentManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val contentDir: File
        get() {
            val dir = File(context.filesDir, "content")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }
    
    private val branchesDir: File
        get() {
            val dir = File(contentDir, "branches")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }
    
    /**
     * Saves content for a branch to the file system.
     *
     * @param branchId The ID of the branch
     * @param content The content to save
     * @return The path to the file where the content was saved
     */
    fun saveBranchContent(branchId: String, content: String): String {
        val file = File(branchesDir, "$branchId.txt")
        file.writeText(content)
        return file.absolutePath
    }
    
    /**
     * Gets content for a branch from the file system.
     *
     * @param branchId The ID of the branch
     * @return The content of the branch, or an empty string if the file doesn't exist
     */
    fun getBranchContent(branchId: String): String {
        val file = File(branchesDir, "$branchId.txt")
        return if (file.exists()) file.readText() else ""
    }
    
    /**
     * Deletes content for a branch from the file system.
     *
     * @param branchId The ID of the branch
     */
    fun deleteBranchContent(branchId: String) {
        val file = File(branchesDir, "$branchId.txt")
        if (file.exists()) {
            file.delete()
        }
    }
} 