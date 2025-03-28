package com.jhj0517.novelhelper.feature.documenteditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhj0517.novelhelper.core.data.repository.DocumentRepository
import com.jhj0517.novelhelper.core.data.repository.DocumentSyncRepository
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
            is DocumentEditorAction.SelectVersion -> selectVersion(action.versionId)
            is DocumentEditorAction.UpdateContent -> updateContent(action.content)
            is DocumentEditorAction.SaveVersion -> saveVersion(action.title, action.content)
            is DocumentEditorAction.CreateBranch -> createBranch(action.name, action.fromVersionId)
            is DocumentEditorAction.SyncToCloud -> syncToCloud()
            is DocumentEditorAction.ToggleBranchSelector -> toggleBranchSelector()
            is DocumentEditorAction.ToggleVersionSelector -> toggleVersionSelector()
        }
    }

    private fun loadDocument(documentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val document = documentRepository.getDocumentById(documentId)
                if (document != null) {
                    // Get the main branch or the first branch
                    val mainBranch = documentRepository.getMainBranchForDocument(documentId)
                    
                    // If main branch is available, select it
                    if (mainBranch != null) {
                        selectBranch(mainBranch.id)
                    } else {
                        // Otherwise, try to get first branch
                        viewModelScope.launch {
                            documentRepository.getBranchesByDocumentIdFlow(documentId).collectLatest { branches ->
                                if (branches.isNotEmpty()) {
                                    selectBranch(branches.first().id)
                                }
                            }
                        }
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

    private fun selectBranch(branchId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val branch = documentRepository.getBranchById(branchId)
                if (branch != null) {
                    // Get the latest version
                    val latestVersion = documentRepository.getLatestVersionForBranch(branchId)
                    if (latestVersion != null) {
                        _uiState.update { 
                            it.copy(
                                currentBranch = branch,
                                currentVersion = latestVersion,
                                isLoading = false,
                                showBranchSelector = false
                            )
                        }
                    } else {
                        _uiState.update { 
                            it.copy(
                                currentBranch = branch,
                                currentVersion = null,
                                isLoading = false,
                                showBranchSelector = false
                            )
                        }
                    }
                } else {
                    _uiState.update { it.copy(error = "Branch not found", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun selectVersion(versionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val version = documentRepository.getVersionById(versionId)
                if (version != null) {
                    _uiState.update { 
                        it.copy(
                            currentVersion = version,
                            isLoading = false,
                            showVersionSelector = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(error = "Version not found", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun updateContent(content: String) {
        val currentVersion = _uiState.value.currentVersion ?: return
        _uiState.update { 
            it.copy(
                currentVersion = currentVersion.copy(content = content)
            )
        }
    }

    private fun saveVersion(title: String, content: String) {
        val currentBranch = _uiState.value.currentBranch ?: return
        val currentVersion = _uiState.value.currentVersion
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            try {
                if (currentVersion != null) {
                    // Update existing version
                    documentRepository.updateVersion(currentVersion.id, content, title)
                    
                    // Refresh the version
                    val updatedVersion = documentRepository.getVersionById(currentVersion.id)
                    _uiState.update { 
                        it.copy(
                            currentVersion = updatedVersion,
                            isSaving = false
                        )
                    }
                } else {
                    // Create new version
                    val versionId = documentRepository.createVersion(currentBranch.id, content, title)
                    
                    // Load the new version
                    val newVersion = documentRepository.getVersionById(versionId)
                    _uiState.update { 
                        it.copy(
                            currentVersion = newVersion,
                            isSaving = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isSaving = false) }
            }
        }
    }

    private fun createBranch(name: String, fromVersionId: String) {
        val document = _uiState.value.document ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Create new branch
                val branchId = documentRepository.createBranch(document.id, name)
                
                // Get the source version
                val sourceVersion = documentRepository.getVersionById(fromVersionId)
                if (sourceVersion != null) {
                    // Create initial version in the new branch with the same content
                    documentRepository.createVersion(
                        branchId,
                        sourceVersion.content,
                        "Initial Version (Branched from ${sourceVersion.title})"
                    )
                }
                
                // Select the new branch
                selectBranch(branchId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun syncToCloud() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, error = null) }
            
            try {
                documentSyncRepository.syncToCloud().collectLatest { progress ->
                    val syncProgress = when (progress) {
                        is com.jhj0517.novelhelper.core.data.repository.SyncProgress.Started -> 
                            SyncProgress.Started
                        is com.jhj0517.novelhelper.core.data.repository.SyncProgress.InProgress -> 
                            SyncProgress.InProgress(progress.current, progress.total)
                        is com.jhj0517.novelhelper.core.data.repository.SyncProgress.Completed -> {
                            _uiState.update { it.copy(isSyncing = false) }
                            SyncProgress.Completed(progress.successCount)
                        }
                        is com.jhj0517.novelhelper.core.data.repository.SyncProgress.Failed -> {
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

    private fun toggleVersionSelector() {
        _uiState.update { it.copy(showVersionSelector = !it.showVersionSelector) }
    }
} 