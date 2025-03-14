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

    /**
     * Saves content for a version to a file.
     *
     * @param versionId The ID of the version
     * @param content The content to save
     * @return The path to the saved file
     */
    fun saveContentForVersion(versionId: String, content: String): String {
        val file = File(contentDir, "version_$versionId.txt")
        file.writeText(content)
        return file.absolutePath
    }

    /**
     * Gets the content for a version from a file.
     *
     * @param versionId The ID of the version
     * @return The content of the version, or an empty string if the file doesn't exist
     */
    fun getContentForVersion(versionId: String): String {
        val file = File(contentDir, "version_$versionId.txt")
        return if (file.exists()) file.readText() else ""
    }

    /**
     * Saves a diff between two versions to a file.
     *
     * @param fromVersionId The ID of the source version
     * @param toVersionId The ID of the target version
     * @param diff The diff content
     * @return The path to the saved file
     */
    fun saveDiff(fromVersionId: String, toVersionId: String, diff: String): String {
        val file = File(contentDir, "diff_${fromVersionId}_${toVersionId}.diff")
        file.writeText(diff)
        return file.absolutePath
    }

    /**
     * Gets the diff between two versions from a file.
     *
     * @param fromVersionId The ID of the source version
     * @param toVersionId The ID of the target version
     * @return The diff content, or an empty string if the file doesn't exist
     */
    fun getDiff(fromVersionId: String, toVersionId: String): String {
        val file = File(contentDir, "diff_${fromVersionId}_${toVersionId}.diff")
        return if (file.exists()) file.readText() else ""
    }

    /**
     * Saves content for a section to a file.
     *
     * @param sectionId The ID of the section
     * @param content The content to save
     * @return The path to the saved file
     */
    fun saveContentForSection(sectionId: String, content: String): String {
        val file = File(contentDir, "section_$sectionId.txt")
        file.writeText(content)
        return file.absolutePath
    }

    /**
     * Gets the content for a section from a file.
     *
     * @param sectionId The ID of the section
     * @return The content of the section, or an empty string if the file doesn't exist
     */
    fun getContentForSection(sectionId: String): String {
        val file = File(contentDir, "section_$sectionId.txt")
        return if (file.exists()) file.readText() else ""
    }

    /**
     * Deletes the content file for a version.
     *
     * @param versionId The ID of the version
     * @return True if the file was deleted, false otherwise
     */
    fun deleteVersionContent(versionId: String): Boolean {
        val file = File(contentDir, "version_$versionId.txt")
        return file.delete()
    }

    /**
     * Deletes the content file for a section.
     *
     * @param sectionId The ID of the section
     * @return True if the file was deleted, false otherwise
     */
    fun deleteSectionContent(sectionId: String): Boolean {
        val file = File(contentDir, "section_$sectionId.txt")
        return file.delete()
    }
} 