package com.jhj0517.novelhelper.core.data.repository

import com.jhj0517.novelhelper.core.data.content.ContentManager
import com.jhj0517.novelhelper.core.database.dao.BranchDao
import com.jhj0517.novelhelper.core.database.dao.DocumentDao
import com.jhj0517.novelhelper.core.database.dao.SectionDao
import com.jhj0517.novelhelper.core.database.dao.VersionDao
import com.jhj0517.novelhelper.core.database.entity.BranchEntity
import com.jhj0517.novelhelper.core.database.entity.DocumentEntity
import com.jhj0517.novelhelper.core.database.entity.SectionEntity
import com.jhj0517.novelhelper.core.database.entity.VersionEntity
import com.jhj0517.novelhelper.core.model.Branch
import com.jhj0517.novelhelper.core.model.Document
import com.jhj0517.novelhelper.core.model.Section
import com.jhj0517.novelhelper.core.model.Version
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepository @Inject constructor(
    private val documentDao: DocumentDao,
    private val branchDao: BranchDao,
    private val versionDao: VersionDao,
    private val sectionDao: SectionDao,
    private val contentManager: ContentManager
) {
    // Document operations
    suspend fun createDocument(title: String, authorId: String, synopsis: String = ""): String {
        val document = DocumentEntity(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            authorId = authorId,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            coverImagePath = null,
            synopsis = synopsis
        )
        documentDao.insert(document)
        
        // Create a main branch for the document
        val branchId = createBranch(document.id, "Main Branch", isMainBranch = true)
        
        // Create an initial empty version
        createVersion(branchId, "", "Initial Version")
        
        return document.id
    }

    suspend fun updateDocument(id: String, title: String, synopsis: String) {
        val document = documentDao.getDocumentById(id) ?: return
        val updatedDocument = document.copy(
            title = title,
            synopsis = synopsis,
            updatedAt = LocalDateTime.now()
        )
        documentDao.update(updatedDocument)
    }

    suspend fun deleteDocument(id: String) {
        val document = documentDao.getDocumentById(id) ?: return
        documentDao.delete(document)
    }

    suspend fun getDocumentById(id: String): Document? {
        val document = documentDao.getDocumentById(id) ?: return null
        val branches = branchDao.getBranchesByDocumentIdFlow(id).map { branchEntities ->
            branchEntities.map { it.toBranch() }
        }
        return document.toDocument(branches)
    }

    fun getAllDocumentsFlow(): Flow<List<Document>> {
        return documentDao.getAllDocumentsFlow().map { documentEntities ->
            documentEntities.map { it.toDocument() }
        }
    }

    fun searchDocumentsFlow(query: String): Flow<List<Document>> {
        return documentDao.searchDocumentsFlow(query).map { documentEntities ->
            documentEntities.map { it.toDocument() }
        }
    }

    // Branch operations
    suspend fun createBranch(documentId: String, name: String, isMainBranch: Boolean = false): String {
        val branch = BranchEntity(
            id = java.util.UUID.randomUUID().toString(),
            documentId = documentId,
            name = name,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            isMainBranch = isMainBranch
        )
        branchDao.insert(branch)
        return branch.id
    }

    suspend fun updateBranch(id: String, name: String) {
        val branch = branchDao.getBranchById(id) ?: return
        val updatedBranch = branch.copy(
            name = name,
            updatedAt = LocalDateTime.now()
        )
        branchDao.update(updatedBranch)
    }

    suspend fun deleteBranch(id: String) {
        val branch = branchDao.getBranchById(id) ?: return
        branchDao.delete(branch)
    }

    suspend fun getBranchById(id: String): Branch? {
        return branchDao.getBranchById(id)?.toBranch()
    }

    fun getBranchesByDocumentIdFlow(documentId: String): Flow<List<Branch>> {
        return branchDao.getBranchesByDocumentIdFlow(documentId).map { branchEntities ->
            branchEntities.map { it.toBranch() }
        }
    }

    suspend fun getMainBranchForDocument(documentId: String): Branch? {
        return branchDao.getMainBranchForDocument(documentId)?.toBranch()
    }

    // Version operations
    suspend fun createVersion(branchId: String, content: String, title: String): String {
        val versionId = java.util.UUID.randomUUID().toString()
        val contentPath = contentManager.saveContentForVersion(versionId, content)
        
        // Get the latest version to set as parent
        val latestVersion = versionDao.getLatestVersionForBranch(branchId)
        val diffPath = if (latestVersion != null) {
            val latestContent = contentManager.getContentForVersion(latestVersion.id)
            val diff = calculateDiff(latestContent, content)
            contentManager.saveDiff(latestVersion.id, versionId, diff)
        } else null
        
        val version = VersionEntity(
            id = versionId,
            branchId = branchId,
            title = title,
            contentFilePath = contentPath,
            diffFromVersionId = latestVersion?.id,
            diffFilePath = diffPath,
            createdAt = LocalDateTime.now(),
            isSyncedToCloud = false
        )
        versionDao.insert(version)
        return version.id
    }

    suspend fun updateVersion(id: String, content: String, title: String) {
        val version = versionDao.getVersionById(id) ?: return
        val contentPath = contentManager.saveContentForVersion(id, content)
        
        val updatedVersion = version.copy(
            title = title,
            contentFilePath = contentPath,
            isSyncedToCloud = false
        )
        versionDao.update(updatedVersion)
    }

    suspend fun deleteVersion(id: String) {
        val version = versionDao.getVersionById(id) ?: return
        versionDao.delete(version)
        contentManager.deleteVersionContent(id)
    }

    suspend fun getVersionById(id: String): Version? {
        val version = versionDao.getVersionById(id) ?: return null
        val content = contentManager.getContentForVersion(id)
        return version.toVersion(content)
    }

    fun getVersionsByBranchIdFlow(branchId: String): Flow<List<Version>> {
        return versionDao.getVersionsByBranchIdFlow(branchId).map { versionEntities ->
            versionEntities.map { 
                val content = contentManager.getContentForVersion(it.id)
                it.toVersion(content)
            }
        }
    }

    suspend fun getLatestVersionForBranch(branchId: String): Version? {
        val version = versionDao.getLatestVersionForBranch(branchId) ?: return null
        val content = contentManager.getContentForVersion(version.id)
        return version.toVersion(content)
    }

    // Section operations
    suspend fun createSection(versionId: String, title: String, content: String, order: Int): String {
        val sectionId = java.util.UUID.randomUUID().toString()
        val contentPath = contentManager.saveContentForSection(sectionId, content)
        
        val section = SectionEntity(
            id = sectionId,
            versionId = versionId,
            title = title,
            contentFilePath = contentPath,
            order = order
        )
        sectionDao.insert(section)
        return section.id
    }

    suspend fun updateSection(id: String, title: String, content: String, order: Int) {
        val section = sectionDao.getSectionById(id) ?: return
        val contentPath = contentManager.saveContentForSection(id, content)
        
        val updatedSection = section.copy(
            title = title,
            contentFilePath = contentPath,
            order = order
        )
        sectionDao.update(updatedSection)
    }

    suspend fun deleteSection(id: String) {
        val section = sectionDao.getSectionById(id) ?: return
        sectionDao.delete(section)
        contentManager.deleteSectionContent(id)
    }

    suspend fun getSectionById(id: String): Section? {
        val section = sectionDao.getSectionById(id) ?: return null
        val content = contentManager.getContentForSection(id)
        return section.toSection(content)
    }

    fun getSectionsByVersionIdFlow(versionId: String): Flow<List<Section>> {
        return sectionDao.getSectionsByVersionIdFlow(versionId).map { sectionEntities ->
            sectionEntities.map { 
                val content = contentManager.getContentForSection(it.id)
                it.toSection(content)
            }
        }
    }

    // Helper methods
    private fun calculateDiff(oldContent: String, newContent: String): String {
        // In a real app, you would use a diff algorithm here
        // For simplicity, we'll just return a placeholder
        return "Diff between versions"
    }

    // Extension functions to convert between entity and domain models
    private fun DocumentEntity.toDocument(branchesFlow: Flow<List<Branch>>? = null): Document {
        return Document(
            id = id,
            title = title,
            authorId = authorId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            coverImagePath = coverImagePath,
            synopsis = synopsis
        )
    }

    private fun BranchEntity.toBranch(): Branch {
        return Branch(
            id = id,
            documentId = documentId,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isMainBranch = isMainBranch
        )
    }

    private fun VersionEntity.toVersion(content: String): Version {
        return Version(
            id = id,
            branchId = branchId,
            content = content,
            title = title,
            diffFromVersionId = diffFromVersionId,
            createdAt = createdAt,
            isSyncedToCloud = isSyncedToCloud
        )
    }

    private fun SectionEntity.toSection(content: String): Section {
        return Section(
            id = id,
            versionId = versionId,
            title = title,
            content = content,
            order = order
        )
    }
} 