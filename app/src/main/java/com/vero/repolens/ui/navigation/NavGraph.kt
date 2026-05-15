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
import com.vero.repolens.data.models.SearchItemType
import com.vero.repolens.ui.components.ErrorState
import com.vero.repolens.ui.components.LoadingState
import com.vero.repolens.ui.screens.ActionItemsScreen
import com.vero.repolens.ui.screens.ArchitectureScreen
import com.vero.repolens.ui.screens.ConcurrencyScreen
import com.vero.repolens.ui.screens.DependencyInjectionScreen
import com.vero.repolens.ui.screens.FeatureDetailScreen
import com.vero.repolens.ui.screens.FeaturesScreen
import com.vero.repolens.ui.screens.ModuleDetailScreen
import com.vero.repolens.ui.screens.ModulesScreen
import com.vero.repolens.ui.screens.OverviewScreen
import com.vero.repolens.ui.screens.PerformanceScreen
import com.vero.repolens.ui.screens.PrReadinessScreen
import com.vero.repolens.ui.screens.RecommendationsScreen
import com.vero.repolens.ui.screens.RisksScreen
import com.vero.repolens.ui.screens.SearchScreen
import com.vero.repolens.ui.screens.TestingScreen
import com.vero.repolens.ui.screens.UiLayerScreen
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
                    onNavigateToArchitecture = { navController.navigate(NavRoutes.ARCHITECTURE) },
                    onNavigateToModules = { navController.navigate(NavRoutes.MODULES) },
                    onNavigateToFeatures = { navController.navigate(NavRoutes.FEATURES) },
                    onNavigateToDI = { navController.navigate(NavRoutes.DEPENDENCY_INJECTION) },
                    onNavigateToUI = { navController.navigate(NavRoutes.UI_LAYER) },
                    onNavigateToConcurrency = { navController.navigate(NavRoutes.CONCURRENCY) },
                    onNavigateToTesting = { navController.navigate(NavRoutes.TESTING) },
                    onNavigateToRisks = { navController.navigate(NavRoutes.RISKS) },
                    onNavigateToPR = { navController.navigate(NavRoutes.PR_READINESS) },
                    onNavigateToSearch = { navController.navigate(NavRoutes.SEARCH) },
                    onNavigateToActionItems = { navController.navigate(NavRoutes.ACTION_ITEMS) },
                    onNavigateToPerformance = { navController.navigate(NavRoutes.PERFORMANCE) },
                    onNavigateToRecommendations = { navController.navigate(NavRoutes.RECOMMENDATIONS) },
                    onNavigateToExport = {}
                )
            }
        }

        composable(NavRoutes.SEARCH) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onItemClick = { item ->
                    when (item.type) {
                        SearchItemType.MODULE -> navController.navigate(NavRoutes.moduleDetail(item.id))
                        SearchItemType.FEATURE -> navController.navigate(NavRoutes.featureDetail(item.id))
                        SearchItemType.RISK -> navController.navigate(NavRoutes.RISKS)
                        else -> navController.popBackStack()
                    }
                }
            )
        }

        composable(NavRoutes.ACTION_ITEMS) {
            ActionItemsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(NavRoutes.ARCHITECTURE) {
            when (val state = uiState) {
                is UiState.Success -> ArchitectureScreen(
                    architecture = state.report.architecture,
                    diagram = state.report.architectureDiagram,
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
                    val di = state.report.dependencyInjection
                    if (di != null) {
                        DependencyInjectionScreen(
                            di = di,
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
                    val concurrency = state.report.concurrency
                    if (concurrency != null) {
                        ConcurrencyScreen(
                            concurrency = concurrency,
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
                    val uiLayer = state.report.uiLayer
                    if (uiLayer != null) {
                        UiLayerScreen(
                            uiLayer = uiLayer,
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
                    val testing = state.report.testing
                    if (testing != null) {
                        TestingScreen(
                            testing = testing,
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
                    val prReadiness = state.report.prReadiness
                    if (prReadiness != null) {
                        PrReadinessScreen(
                            prReadiness = prReadiness,
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

        composable(NavRoutes.PERFORMANCE) {
            when (val state = uiState) {
                is UiState.Success -> {
                    val performance = state.report.performance
                    if (performance != null) {
                        PerformanceScreen(
                            performance = performance,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    } else {
                        ErrorState(
                            message = "No performance metrics available",
                            onRetry = { navController.popBackStack() }
                        )
                    }
                }
                else -> LoadingState()
            }
        }

        composable(NavRoutes.RECOMMENDATIONS) {
            RecommendationsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

// Made with Bob
