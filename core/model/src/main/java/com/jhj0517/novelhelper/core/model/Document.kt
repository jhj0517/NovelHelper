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
    val content: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val isMainBranch: Boolean = false,
    val isSyncedToCloud: Boolean = false
)
