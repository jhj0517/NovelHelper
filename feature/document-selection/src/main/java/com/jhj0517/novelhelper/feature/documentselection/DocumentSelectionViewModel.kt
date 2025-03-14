package com.jhj0517.novelhelper.feature.documentselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhj0517.novelhelper.core.data.repository.DocumentRepository
import com.jhj0517.novelhelper.core.model.Document
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the document selection screen.
 */
data class DocumentSelectionState(
    val isLoading: Boolean = true,
    val documents: List<Document> = emptyList(),
    val error: String? = null,
    val showSearch: Boolean = false,
    val searchQuery: String = ""
)

/**
 * Actions that can be performed on the document selection screen.
 */
sealed class DocumentSelectionAction {
    data class CreateDocument(val title: String, val synopsis: String) : DocumentSelectionAction()
    data class UpdateSearchQuery(val query: String) : DocumentSelectionAction()
    object ToggleSearch : DocumentSelectionAction()
}

/**
 * ViewModel for the document selection screen.
 */
@HiltViewModel
class DocumentSelectionViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentSelectionState())
    val uiState: StateFlow<DocumentSelectionState> = _uiState.asStateFlow()

    init {
        loadDocuments()
    }

    fun handleAction(action: DocumentSelectionAction) {
        when (action) {
            is DocumentSelectionAction.CreateDocument -> createDocument(action.title, action.synopsis)
            is DocumentSelectionAction.UpdateSearchQuery -> updateSearchQuery(action.query)
            is DocumentSelectionAction.ToggleSearch -> toggleSearch()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadDocuments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Use flatMapLatest to switch to a new flow when the search query changes
                _uiState.flatMapLatest { state ->
                    if (state.searchQuery.isBlank()) {
                        documentRepository.getAllDocumentsFlow()
                    } else {
                        documentRepository.searchDocumentsFlow(state.searchQuery)
                    }
                }.collectLatest { documents ->
                    _uiState.update { 
                        it.copy(
                            documents = documents,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun createDocument(title: String, synopsis: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Use a placeholder author ID for now
                val authorId = "current-user-id"
                documentRepository.createDocument(title, authorId, synopsis)
                
                // Documents will be updated automatically via Flow
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        // Documents will be updated automatically via flatMapLatest
    }

    private fun toggleSearch() {
        _uiState.update { 
            it.copy(
                showSearch = !it.showSearch,
                searchQuery = if (it.showSearch) "" else it.searchQuery
            )
        }
    }
} 