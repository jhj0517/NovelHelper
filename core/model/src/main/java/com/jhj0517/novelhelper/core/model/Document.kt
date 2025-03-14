package com.jhj0517.novelhelper.core.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents a novel document.
 */
data class Document(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val authorId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val coverImagePath: String? = null,
    val synopsis: String = "",
    val branches: List<Branch> = emptyList()
)

/**
 * Represents a branch of a novel document.
 */
data class Branch(
    val id: String = UUID.randomUUID().toString(),
    val documentId: String,
    val name: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val isMainBranch: Boolean = false
)

/**
 * Represents a version of a branch.
 */
data class Version(
    val id: String = UUID.randomUUID().toString(),
    val branchId: String,
    val content: String = "",
    val title: String = "",
    val diffFromVersionId: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isSyncedToCloud: Boolean = false
)

/**
 * Represents a section in the document, like a chapter or a section with a subtitle.
 */
data class Section(
    val id: String = UUID.randomUUID().toString(),
    val versionId: String,
    val title: String,
    val content: String,
    val order: Int
) 