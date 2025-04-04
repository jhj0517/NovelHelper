package com.jhj0517.novelhelper.feature.documenteditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhj0517.novelhelper.core.data.repository.DocumentRepository
import com.jhj0517.novelhelper.core.data.repository.DocumentSyncRepository
import com.jhj0517.novelhelper.core.data.repository.SyncProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocumentEditorViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val documentSyncRepository: DocumentSyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentEditorState())
    val uiState: StateFlow<DocumentEditorState> = _uiState.asStateFlow()

    fun handleAction(action: DocumentEditorAction) {
        when (action) {
            is DocumentEditorAction.LoadDocument -> loadDocument(action.documentId)
            is DocumentEditorAction.SelectBranch -> selectBranch(action.branchId)
            is DocumentEditorAction.CreateBranch -> createBranch(action.name)
            is DocumentEditorAction.UpdateContent -> updateContent(action.content)
            is DocumentEditorAction.SyncToCloud -> syncToCloud()
            is DocumentEditorAction.ToggleBranchSelector -> toggleBranchSelector()
            is DocumentEditorAction.LoadBranches -> loadBranches()
        }
    }

    private fun loadDocument(documentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val document = documentRepository.getDocumentById(documentId)
                if (document != null) {
                    // Set up a collector for branches
                    setupBranchesCollector(documentId)
                    
                    // Get the main branch
                    val mainBranch = documentRepository.getMainBranchForDocument(documentId)
                    
                    if (mainBranch != null) {
                        // If main branch exists, select it
                        selectBranch(mainBranch.id)
                    } else {
                        // If no main branch exists, create one
                        val branchId = documentRepository.createBranch(document.id, "Main Branch", isMainBranch = true)
                        selectBranch(branchId)
                    }
                    
                    _uiState.update { it.copy(document = document, isLoading = false) }
                } else {
                    _uiState.update { it.copy(error = "Document not found", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun setupBranchesCollector(documentId: String) {
        viewModelScope.launch {
            documentRepository.getBranchesByDocumentIdFlow(documentId).collectLatest { branches ->
                _uiState.update { it.copy(branches = branches) }
            }
        }
    }

    private fun loadBranches() {
        val documentId = _uiState.value.document?.id ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                documentRepository.getBranchesByDocumentIdFlow(documentId).collectLatest { branches ->
                    _uiState.update { 
                        it.copy(
                            branches = branches,
                            isLoading = false
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun selectBranch(branchId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val branch = documentRepository.getBranchById(branchId)
                if (branch != null) {
                    // Get the latest version's content
                    val latestVersion = documentRepository.getLatestVersionForBranch(branchId)
                    val content = latestVersion?.content ?: ""
                    
                    _uiState.update { 
                        it.copy(
                            currentBranch = branch,
                            content = content,
                            isLoading = false,
                            showBranchSelector = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(error = "Branch not found", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun createBranch(name: String) {
        val document = _uiState.value.document ?: return
        val currentContent = _uiState.value.content
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Create new branch
                val branchId = documentRepository.createBranch(document.id, name)
                
                // Create an initial version with the current content
                documentRepository.createVersion(branchId, currentContent, "Initial Version")
                
                // Select the new branch
                selectBranch(branchId)
                
                // Explicitly load branches (though our collector should handle this)
                loadBranches()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun updateContent(content: String) {
        val currentBranch = _uiState.value.currentBranch ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            try {
                // Create a new version with the updated content
                documentRepository.createVersion(currentBranch.id, content, "Updated Version")
                
                // Update the UI state
                _uiState.update { 
                    it.copy(
                        content = content,
                        isSaving = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isSaving = false) }
            }
        }
    }

    private fun syncToCloud() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, error = null) }
            
            try {
                documentSyncRepository.syncToCloud().collectLatest { progress ->
                    val syncProgress = when (progress) {
                        is SyncProgress.Started ->
                            SyncProgress.Started
                        is SyncProgress.InProgress ->
                            SyncProgress.InProgress(progress.current, progress.total)
                        is SyncProgress.Completed -> {
                            _uiState.update { it.copy(isSyncing = false) }
                            SyncProgress.Completed(progress.successCount)
                        }
                        is SyncProgress.Failed -> {
                            _uiState.update { it.copy(isSyncing = false, error = progress.error) }
                            SyncProgress.Failed(progress.error)
                        }
                    }
                    
                    _uiState.update { it.copy(syncProgress = syncProgress) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isSyncing = false) }
            }
        }
    }

    private fun toggleBranchSelector() {
        _uiState.update { it.copy(showBranchSelector = !it.showBranchSelector) }
    }
} 