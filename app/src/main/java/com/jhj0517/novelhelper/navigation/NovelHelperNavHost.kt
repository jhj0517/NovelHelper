package com.jhj0517.novelhelper.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jhj0517.novelhelper.feature.documenteditor.DocumentEditorScreen
import com.jhj0517.novelhelper.feature.documentselection.DocumentSelectionScreen

/**
 * Navigation routes for the app.
 */
object NovelHelperDestinations {
    const val DOCUMENT_SELECTION_ROUTE = "document_selection"
    const val DOCUMENT_EDITOR_ROUTE = "document_editor"
    const val DOCUMENT_ID_ARG = "documentId"
    
    fun documentEditorRoute(documentId: String): String {
        return "$DOCUMENT_EDITOR_ROUTE/$documentId"
    }
}

/**
 * Main navigation component for the app.
 */
@Composable
fun NovelHelperNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NovelHelperDestinations.DOCUMENT_SELECTION_ROUTE,
        modifier = modifier
    ) {
        composable(NovelHelperDestinations.DOCUMENT_SELECTION_ROUTE) {
            DocumentSelectionScreen(
                onDocumentSelected = { documentId ->
                    navController.navigate(NovelHelperDestinations.documentEditorRoute(documentId))
                }
            )
        }
        
        composable(
            route = "${NovelHelperDestinations.DOCUMENT_EDITOR_ROUTE}/{${NovelHelperDestinations.DOCUMENT_ID_ARG}}",
            arguments = listOf(
                navArgument(NovelHelperDestinations.DOCUMENT_ID_ARG) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString(NovelHelperDestinations.DOCUMENT_ID_ARG)
                ?: return@composable
            
            DocumentEditorScreen(
                documentId = documentId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
} 