package com.vero.repolens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vero.repolens.ui.navigation.NavRoutes
import com.vero.repolens.ui.navigation.RepoLensNavGraph
import com.vero.repolens.ui.screens.ActionItemEditorSheet
import com.vero.repolens.ui.theme.RepolensTheme
import com.vero.repolens.viewmodel.ActionItemsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RepolensTheme {
                RepoLensAppShell()
            }
        }
    }
}

@Composable
private fun RepoLensAppShell(
    actionItemsViewModel: ActionItemsViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    var showGlobalActionSheet by remember { mutableStateOf(false) }

    val showGlobalFab = currentRoute != NavRoutes.INTRO && currentRoute != NavRoutes.ACTION_ITEMS

    Scaffold(
        floatingActionButton = {
            if (showGlobalFab) {
                FloatingActionButton(
                    onClick = { showGlobalActionSheet = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add action item")
                }
            }
        }
    ) { _ ->
        RepoLensNavGraph(navController = navController)
    }

    if (showGlobalActionSheet) {
        ActionItemEditorSheet(
            actionItem = null,
            onDismiss = { showGlobalActionSheet = false },
            onSave = { title, description, priority, category ->
                actionItemsViewModel.createActionItem(
                    title = title,
                    description = description,
                    priority = priority,
                    category = category
                )
                showGlobalActionSheet = false
            }
        )
    }
}
