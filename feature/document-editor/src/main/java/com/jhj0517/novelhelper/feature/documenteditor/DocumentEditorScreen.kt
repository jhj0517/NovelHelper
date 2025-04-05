package com.jhj0517.novelhelper.feature.documenteditor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jhj0517.novelhelper.core.data.repository.SyncProgress
import com.jhj0517.novelhelper.core.model.Branch
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentEditorScreen(
    documentId: String,
    onNavigateBack: () -> Unit,
    viewModel: DocumentEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Load document when the screen is first composed
    LaunchedEffect(documentId) {
        viewModel.handleAction(DocumentEditorAction.LoadDocument(documentId))
    }
    
    // Show error in snackbar if there is one
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.document?.title ?: "Loading...",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Branch selector
                    BranchSelector(
                        currentBranch = uiState.currentBranch,
                        showSelector = uiState.showBranchSelector,
                        onToggleSelector = {
                            // Load branches when toggling the selector
                            if (!uiState.showBranchSelector) {
                                viewModel.handleAction(DocumentEditorAction.LoadBranches)
                            }
                            viewModel.handleAction(DocumentEditorAction.ToggleBranchSelector)
                        },
                        onSelectBranch = { branchId ->
                            viewModel.handleAction(DocumentEditorAction.SelectBranch(branchId))
                        },
                        onCreateBranch = { name ->
                            viewModel.handleAction(DocumentEditorAction.CreateBranch(name))
                        },
                        branches = uiState.branches,
                        isLoading = uiState.isLoading
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                LoadingIndicator()
            } else if (uiState.document == null) {
                ErrorMessage("Document not found")
            } else {
                DocumentContent(
                    uiState = uiState,
                    onUpdateContent = { newContent ->
                        viewModel.handleAction(DocumentEditorAction.UpdateContent(newContent))
                    }
                )
            }
            
            // Show sync progress if syncing
            if (uiState.isSyncing && uiState.syncProgress != null) {
                SyncProgressOverlay(uiState.syncProgress!!)
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun DocumentContent(
    uiState: DocumentEditorState,
    onUpdateContent: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Branch info
        Text(
            text = "Current Branch: ${uiState.currentBranch?.name ?: "No branch selected"}",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Document content editor
        OutlinedTextField(
            value = uiState.content,
            onValueChange = onUpdateContent,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            label = { Text("Document Content") }
        )
    }
}

@Composable
private fun BranchSelector(
    currentBranch: Branch?,
    showSelector: Boolean,
    onToggleSelector: () -> Unit,
    onSelectBranch: (String) -> Unit,
    onCreateBranch: (String) -> Unit,
    branches: List<Branch> = emptyList(),
    isLoading: Boolean = false
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onToggleSelector() }
    ) {
        Text(
            text = currentBranch?.name ?: "Select Branch",
            style = MaterialTheme.typography.bodyMedium
        )
        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Branch")
        
        DropdownMenu(
            expanded = showSelector,
            onDismissRequest = onToggleSelector
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else if (branches.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No branches available") },
                    onClick = { }
                )
            } else {
                // Show available branches
                branches.forEach { branch ->
                    DropdownMenuItem(
                        text = { Text(branch.name) },
                        onClick = { 
                            onSelectBranch(branch.id)
                            onToggleSelector()
                        }
                    )
                }
            }
            
            if (!isLoading && branches.isNotEmpty()) {
                HorizontalDivider()
            }
            
            DropdownMenuItem(
                text = { Text("Create New Branch") },
                onClick = { 
                    showCreateDialog = true
                    onToggleSelector()
                }
            )
        }
    }
    
    if (showCreateDialog) {
        CreateBranchDialog(
            onDismiss = { showCreateDialog = false },
            onCreateBranch = { name ->
                onCreateBranch(name)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun CreateBranchDialog(
    onDismiss: () -> Unit,
    onCreateBranch: (String) -> Unit
) {
    var branchName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Branch") },
        text = {
            Column {
                OutlinedTextField(
                    value = branchName,
                    onValueChange = { 
                        branchName = it 
                        errorMessage = ""
                    },
                    label = { Text("Branch Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage.isNotEmpty(),
                    supportingText = {
                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "This will create a new branch with the current content.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (branchName.isBlank()) {
                        errorMessage = "Branch name cannot be empty"
                    } else {
                        onCreateBranch(branchName)
                    }
                },
                enabled = branchName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SyncProgressOverlay(syncProgress: SyncProgress) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.8f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Syncing to Cloud",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                when (syncProgress) {
                    is SyncProgress.Started -> {
                        CircularProgressIndicator()
                        Text("Starting sync...")
                    }
                    is SyncProgress.InProgress -> {
                        LinearProgressIndicator(
                            progress = { syncProgress.current.toFloat() / syncProgress.total },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Syncing ${syncProgress.current} of ${syncProgress.total}")
                    }
                    is SyncProgress.Completed -> {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text("Sync completed: ${syncProgress.successCount} items synced")
                    }
                    is SyncProgress.Failed -> {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Failed",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Sync failed: ${syncProgress.error}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
} 