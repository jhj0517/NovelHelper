package com.jhj0517.novelhelper.feature.documenteditor

import com.jhj0517.novelhelper.core.model.Branch
import com.jhj0517.novelhelper.core.model.Document

/**
 * Represents the UI state for the document editor screen.
 */
data class DocumentEditorState(
    val isLoading: Boolean = true,
    val document: Document? = null,
    val currentBranch: Branch? = null,
    val content: String = "",
    val error: String? = null,
    val isSaving: Boolean = false,
    val isSyncing: Boolean = false,
    val syncProgress: SyncProgress? = null,
    val showBranchSelector: Boolean = false
)

/**
 * Represents the progress of a sync operation.
 */
sealed class SyncProgress {
    object Started : SyncProgress()
    data class InProgress(val current: Int, val total: Int) : SyncProgress()
    data class Completed(val successCount: Int) : SyncProgress()
    data class Failed(val error: String) : SyncProgress()
}

/**
 * Represents a user action in the document editor.
 */
sealed class DocumentEditorAction {
    data class LoadDocument(val documentId: String) : DocumentEditorAction()
    data class SelectBranch(val branchId: String) : DocumentEditorAction()
    data class CreateBranch(val name: String) : DocumentEditorAction()
    data class UpdateContent(val content: String) : DocumentEditorAction()
    object SyncToCloud : DocumentEditorAction()
    object ToggleBranchSelector : DocumentEditorAction()
} 