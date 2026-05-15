package com.vero.repolens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.vero.repolens.ui.navigation.RepoLensNavGraph
import com.vero.repolens.ui.theme.RepolensTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RepolensTheme {
                val navController = rememberNavController()
                RepoLensNavGraph(navController = navController)
            }
        }
    }
}