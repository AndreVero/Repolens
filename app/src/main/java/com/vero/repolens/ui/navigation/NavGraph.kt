package com.vero.repolens.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vero.repolens.ui.components.ErrorState
import com.vero.repolens.ui.components.LoadingState
import com.vero.repolens.ui.screens.*
import com.vero.repolens.viewmodel.RepoLensViewModel
import com.vero.repolens.viewmodel.UiState

@Composable
fun RepoLensNavGraph(
    navController: NavHostController,
    viewModel: RepoLensViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.OVERVIEW
    ) {
        composable(NavRoutes.OVERVIEW) {
            when (val state = uiState) {
                is UiState.Loading -> LoadingState()
                is UiState.Error -> ErrorState(
                    message = state.message,
                    onRetry = { viewModel.retry() }
                )
                is UiState.Success -> OverviewScreen(
                    report = state.report,
                    onNavigateToArchitecture = {
                        navController.navigate(NavRoutes.ARCHITECTURE)
                    },
                    onNavigateToModules = {
                        navController.navigate(NavRoutes.MODULES)
                    },
                    onNavigateToFeatures = {
                        navController.navigate(NavRoutes.FEATURES)
                    },
                    onNavigateToDI = {
                        navController.navigate(NavRoutes.DEPENDENCY_INJECTION)
                    },
                    onNavigateToUI = {
                        navController.navigate(NavRoutes.UI_LAYER)
                    },
                    onNavigateToConcurrency = {
                        navController.navigate(NavRoutes.CONCURRENCY)
                    },
                    onNavigateToTesting = {
                        navController.navigate(NavRoutes.TESTING)
                    },
                    onNavigateToRisks = {
                        navController.navigate(NavRoutes.RISKS)
                    },
                    onNavigateToPR = {
                        navController.navigate(NavRoutes.PR_READINESS)
                    },
                    onNavigateToSearch = {
                        navController.navigate(NavRoutes.SEARCH)
                    }
                )
            }
        composable(NavRoutes.SEARCH) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onItemClick = { item ->
                    // Navigate to appropriate detail screen based on item type
                    when (item.type) {
                        com.vero.repolens.data.models.SearchItemType.MODULE -> {
                            navController.navigate(NavRoutes.moduleDetail(item.id))
                        }
                        com.vero.repolens.data.models.SearchItemType.FEATURE -> {
                            navController.navigate(NavRoutes.featureDetail(item.id))
                        }
                        com.vero.repolens.data.models.SearchItemType.RISK -> {
                            navController.navigate(NavRoutes.RISKS)
                        }
                        else -> {
                            // For other types, navigate to relevant screen
                            navController.popBackStack()
                        }
                    }
                }
            )
        }

        }

        composable(NavRoutes.ARCHITECTURE) {
            when (val state = uiState) {
                is UiState.Success -> ArchitectureScreen(
                    architecture = state.report.architecture,
                    onNavigateBack = { navController.popBackStack() }
                )
                else -> LoadingState()
            }
        }

        composable(NavRoutes.MODULES) {
            when (val state = uiState) {
                is UiState.Success -> ModulesScreen(
                    modules = state.report.modules,
                    onNavigateBack = { navController.popBackStack() },
                    onModuleClick = { moduleId ->
                        navController.navigate(NavRoutes.moduleDetail(moduleId))
                    }
                )
                else -> LoadingState()
            }
        }

        composable(
            route = NavRoutes.MODULE_DETAIL,
            arguments = listOf(navArgument("moduleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString("moduleId")
            when (val state = uiState) {
                is UiState.Success -> {
                    val module = state.report.modules.find { it.id == moduleId }
                    if (module != null) {
                        ModuleDetailScreen(
                            module = module,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    } else {
                        ErrorState(
                            message = "Module not found",
                            onRetry = { navController.popBackStack() }
                        )
                    }
                }
                else -> LoadingState()
            }
        }

        composable(NavRoutes.FEATURES) {
            when (val state = uiState) {
                is UiState.Success -> FeaturesScreen(
                    features = state.report.features,
                    onNavigateBack = { navController.popBackStack() },
                    onFeatureClick = { featureId ->
                        navController.navigate(NavRoutes.featureDetail(featureId))
                    }
                )
                else -> LoadingState()
            }
        }

        composable(
            route = NavRoutes.FEATURE_DETAIL,
            arguments = listOf(navArgument("featureId") { type = NavType.StringType })
        ) { backStackEntry ->
            val featureId = backStackEntry.arguments?.getString("featureId")
            when (val state = uiState) {
                is UiState.Success -> {
                    val feature = state.report.features.find { it.id == featureId }
                    if (feature != null) {
                        FeatureDetailScreen(
                            feature = feature,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    } else {
                        ErrorState(
                            message = "Feature not found",
                            onRetry = { navController.popBackStack() }
                        )
                    }
                }
                else -> LoadingState()
            }
        }

        composable(NavRoutes.DEPENDENCY_INJECTION) {
            when (val state = uiState) {
                is UiState.Success -> {
                    if (state.report.dependencyInjection != null) {
                        DependencyInjectionScreen(
                            di = state.report.dependencyInjection,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    } else {
                        ErrorState(
                            message = "No DI information available",
                            onRetry = { navController.popBackStack() }
                        )
                    }
                }
                else -> LoadingState()
            }
        }

        composable(NavRoutes.CONCURRENCY) {
            when (val state = uiState) {
                is UiState.Success -> {
                    if (state.report.concurrency != null) {
                        ConcurrencyScreen(
                            concurrency = state.report.concurrency,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    } else {
                        ErrorState(
                            message = "No concurrency information available",
                            onRetry = { navController.popBackStack() }
                        )
                    }
                }
                else -> LoadingState()
            }
        }

        composable(NavRoutes.UI_LAYER) {
            when (val state = uiState) {
                is UiState.Success -> {
                    if (state.report.uiLayer != null) {
                        UiLayerScreen(
                            uiLayer = state.report.uiLayer,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    } else {
                        ErrorState(
                            message = "No UI layer information available",
                            onRetry = { navController.popBackStack() }
                        )
                    }
                }
                else -> LoadingState()
            }
        }

        composable(NavRoutes.TESTING) {
            when (val state = uiState) {
                is UiState.Success -> {
                    if (state.report.testing != null) {
                        TestingScreen(
                            testing = state.report.testing,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    } else {
                        ErrorState(
                            message = "No testing information available",
                            onRetry = { navController.popBackStack() }
                        )
                    }
                }
                else -> LoadingState()
            }
        }

        composable(NavRoutes.RISKS) {
            when (val state = uiState) {
                is UiState.Success -> RisksScreen(
                    risks = state.report.risks,
                    onNavigateBack = { navController.popBackStack() }
                )
                else -> LoadingState()
            }
        }

        composable(NavRoutes.PR_READINESS) {
            when (val state = uiState) {
                is UiState.Success -> {
                    if (state.report.prReadiness != null) {
                        PrReadinessScreen(
                            prReadiness = state.report.prReadiness,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    } else {
                        ErrorState(
                            message = "No PR readiness information available",
                            onRetry = { navController.popBackStack() }
                        )
                    }
                }
                else -> LoadingState()
            }
        }
    }
}

// Made with Bob
