package com.jhj0517.novelhelper.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "documents"
)
data class DocumentEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val authorId: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val coverImagePath: String?,
    val synopsis: String
)

@Entity(
    tableName = "branches",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("documentId")
    ]
)
data class BranchEntity(
    @PrimaryKey
    val id: String,
    val documentId: String,
    val name: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isMainBranch: Boolean
)

// deprecated
@Entity(
    tableName = "versions",
    foreignKeys = [
        ForeignKey(
            entity = BranchEntity::class,
            parentColumns = ["id"],
            childColumns = ["branchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("branchId")
    ]
)
data class VersionEntity(
    @PrimaryKey
    val id: String,
    val branchId: String,
    val title: String,
    val contentFilePath: String,
    val diffFromVersionId: String?,
    val diffFilePath: String?,
    val createdAt: LocalDateTime,
    val isSyncedToCloud: Boolean
)

// deprecated
@Entity(
    tableName = "sections",
    foreignKeys = [
        ForeignKey(
            entity = VersionEntity::class,
            parentColumns = ["id"],
            childColumns = ["versionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("versionId")
    ]
)
data class SectionEntity(
    @PrimaryKey
    val id: String,
    val versionId: String,
    val title: String,
    val contentFilePath: String,
    val order: Int
) 