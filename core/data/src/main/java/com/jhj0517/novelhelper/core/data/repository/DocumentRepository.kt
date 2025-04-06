package com.jhj0517.novelhelper.core.data.repository

import com.jhj0517.novelhelper.core.data.content.ContentManager
import com.jhj0517.novelhelper.core.database.dao.BranchDao
import com.jhj0517.novelhelper.core.database.dao.DocumentDao
import com.jhj0517.novelhelper.core.database.entity.BranchEntity
import com.jhj0517.novelhelper.core.database.entity.DocumentEntity
import com.jhj0517.novelhelper.core.model.Branch
import com.jhj0517.novelhelper.core.model.Document
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepository @Inject constructor(
    private val documentDao: DocumentDao,
    private val branchDao: BranchDao,
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
        
        // Create a main branch for the document with empty content
        createBranch(document.id, "Main Branch", "", isMainBranch = true)
        
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
    suspend fun createBranch(documentId: String, name: String, content: String = "", isMainBranch: Boolean = false): String {
        val branchId = java.util.UUID.randomUUID().toString()
        val contentFilePath = contentManager.saveBranchContent(branchId, content)
        
        val branch = BranchEntity(
            id = branchId,
            documentId = documentId,
            name = name,
            content = content,
            contentFilePath = contentFilePath,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            isMainBranch = isMainBranch,
            isSyncedToCloud = false
        )
        branchDao.insert(branch)
        return branch.id
    }

    suspend fun updateBranch(id: String, name: String, content: String) {
        val branch = branchDao.getBranchById(id) ?: return
        val contentFilePath = contentManager.saveBranchContent(id, content)
        
        val updatedBranch = branch.copy(
            name = name,
            content = content,
            contentFilePath = contentFilePath,
            updatedAt = LocalDateTime.now(),
            isSyncedToCloud = false
        )
        branchDao.update(updatedBranch)
    }

    suspend fun updateBranchContent(id: String, content: String) {
        val branch = branchDao.getBranchById(id) ?: return
        val contentFilePath = contentManager.saveBranchContent(id, content)
        
        val updatedBranch = branch.copy(
            content = content,
            contentFilePath = contentFilePath,
            updatedAt = LocalDateTime.now(),
            isSyncedToCloud = false
        )
        branchDao.update(updatedBranch)
    }

    suspend fun deleteBranch(id: String) {
        val branch = branchDao.getBranchById(id) ?: return
        branchDao.delete(branch)
        contentManager.deleteBranchContent(id)
    }

    suspend fun getBranchById(id: String): Branch? {
        val branch = branchDao.getBranchById(id) ?: return null
        val content = contentManager.getBranchContent(id)
        return branch.toBranch(content)
    }

    fun getBranchesByDocumentIdFlow(documentId: String): Flow<List<Branch>> {
        return branchDao.getBranchesByDocumentIdFlow(documentId).map { branchEntities ->
            branchEntities.map { entity -> 
                val content = contentManager.getBranchContent(entity.id)
                entity.toBranch(content)
            }
        }
    }

    suspend fun getMainBranchForDocument(documentId: String): Branch? {
        val branch = branchDao.getMainBranchForDocument(documentId) ?: return null
        val content = contentManager.getBranchContent(branch.id)
        return branch.toBranch(content)
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

    private fun BranchEntity.toBranch(content: String = this.content): Branch {
        return Branch(
            id = id,
            documentId = documentId,
            name = name,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isMainBranch = isMainBranch,
            isSyncedToCloud = isSyncedToCloud
        )
    }
} 