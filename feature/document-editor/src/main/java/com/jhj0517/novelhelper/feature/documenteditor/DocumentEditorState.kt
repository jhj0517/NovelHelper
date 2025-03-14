package com.jhj0517.novelhelper.feature.documenteditor

import com.jhj0517.novelhelper.core.model.Branch
import com.jhj0517.novelhelper.core.model.Document
import com.jhj0517.novelhelper.core.model.Section
import com.jhj0517.novelhelper.core.model.Version

/**
 * Represents the UI state for the document editor screen.
 */
data class DocumentEditorState(
    val isLoading: Boolean = true,
    val document: Document? = null,
    val currentBranch: Branch? = null,
    val currentVersion: Version? = null,
    val sections: List<Section> = emptyList(),
    val error: String? = null,
    val isSaving: Boolean = false,
    val isSyncing: Boolean = false,
    val syncProgress: SyncProgress? = null,
    val showBranchSelector: Boolean = false,
    val showVersionSelector: Boolean = false,
    val showSectionEditor: Boolean = false,
    val selectedSectionId: String? = null
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
    data class SelectVersion(val versionId: String) : DocumentEditorAction()
    data class UpdateContent(val content: String) : DocumentEditorAction()
    data class SaveVersion(val title: String, val content: String) : DocumentEditorAction()
    data class CreateBranch(val name: String, val fromVersionId: String) : DocumentEditorAction()
    data class AddSection(val title: String, val content: String) : DocumentEditorAction()
    data class UpdateSection(val sectionId: String, val title: String, val content: String) : DocumentEditorAction()
    data class DeleteSection(val sectionId: String) : DocumentEditorAction()
    data class ReorderSections(val sectionIds: List<String>) : DocumentEditorAction()
    object SyncToCloud : DocumentEditorAction()
    object ToggleBranchSelector : DocumentEditorAction()
    object ToggleVersionSelector : DocumentEditorAction()
    object ToggleSectionEditor : DocumentEditorAction()
    data class SelectSection(val sectionId: String?) : DocumentEditorAction()
} 