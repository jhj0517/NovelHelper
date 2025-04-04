package com.jhj0517.novelhelper.feature.documenteditor

import com.jhj0517.novelhelper.core.data.repository.SyncProgress
import com.jhj0517.novelhelper.core.model.Branch
import com.jhj0517.novelhelper.core.model.Document

/**
 * Represents the UI state for the document editor screen.
 */
data class DocumentEditorState(
    val isLoading: Boolean = true,
    val document: Document? = null,
    val currentBranch: Branch? = null,
    val branches: List<Branch> = emptyList(),
    val content: String = "",
    val error: String? = null,
    val isSaving: Boolean = false,
    val isSyncing: Boolean = false,
    val syncProgress: SyncProgress? = null,
    val showBranchSelector: Boolean = false
)


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
    object LoadBranches : DocumentEditorAction()
} 