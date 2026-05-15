package com.vero.repolens.data.models

import kotlinx.serialization.Serializable

@Serializable
data class RepoLensReport(
    val metadata: Metadata,
    val repository: Repository,
    val metrics: Metrics,
    val architecture: Architecture,
    val modules: List<Module> = emptyList(),
    val features: List<Feature> = emptyList(),
    val dependencyInjection: DependencyInjection? = null,
    val concurrency: Concurrency? = null,
    val uiLayer: UiLayer? = null,
    val networking: Networking? = null,
    val persistence: Persistence? = null,
    val testing: Testing? = null,
    val risks: List<Risk> = emptyList(),
    val prReadiness: PrReadiness? = null,
    val documentation: Documentation? = null,
    val bobUsage: BobUsage? = null,
    val performance: PerformanceMetrics? = null,
    val recommendations: List<SmartRecommendation> = emptyList(),
    val architectureDiagram: ArchitectureDiagram? = null
)

@Serializable
data class Metadata(
    val schemaVersion: String,
    val generatedBy: String,
    val generatedAt: String,
    val analysisType: String,
    val confidence: Double,
    val notes: List<String> = emptyList()
)

@Serializable
data class Repository(
    val name: String,
    val summary: String,
    val mainLanguage: String,
    val platform: String,
    val projectType: String,
    val architectureStyle: String,
    val frameworks: List<String> = emptyList(),
    val buildSystem: String? = null,
    val packageName: String? = null,
    val repositoryRoot: String? = null,
    val importantDirectories: List<String> = emptyList()
)

@Serializable
data class Metrics(
    val modulesCount: Int = 0,
    val featuresCount: Int = 0,
    val screensCount: Int = 0,
    val viewModelsCount: Int = 0,
    val repositoriesCount: Int = 0,
    val useCasesCount: Int = 0,
    val existingTestsCount: Int = 0,
    val missingTestsCount: Int = 0,
    val highRiskAreasCount: Int = 0,
    val criticalRiskAreasCount: Int = 0,
    val prReadinessScore: Int = 0,
    val architectureConfidenceScore: Int = 0
)

@Serializable
data class Architecture(
    val style: String,
    val summary: String,
    val layers: List<ArchitectureLayer> = emptyList(),
    val dataFlow: List<DataFlow> = emptyList(),
    val dependencyDirection: String? = null,
    val strengths: List<String> = emptyList(),
    val concerns: List<String> = emptyList(),
    val recommendations: List<String> = emptyList()
)

@Serializable
data class ArchitectureLayer(
    val id: String,
    val name: String,
    val responsibility: String,
    val commonPatterns: List<String> = emptyList(),
    val relatedFiles: List<String> = emptyList(),
    val risks: List<String> = emptyList()
)

@Serializable
data class DataFlow(
    val from: String,
    val to: String,
    val description: String
)

@Serializable
data class Module(
    val id: String,
    val name: String,
    val type: String,
    val path: String? = null,
    val responsibility: String,
    val riskLevel: String,
    val stability: String? = null,
    val dependencies: List<String> = emptyList(),
    val publicApis: List<String> = emptyList(),
    val keyPackages: List<String> = emptyList(),
    val importantFiles: List<String> = emptyList(),
    val testCoverage: TestCoverage? = null,
    val risks: List<Risk> = emptyList()
)

@Serializable
data class TestCoverage(
    val status: String,
    val existingTests: List<String> = emptyList(),
    val missingAreas: List<String> = emptyList()
)

@Serializable
data class Feature(
    val id: String,
    val name: String,
    val description: String,
    val businessPurpose: String? = null,
    val complexity: String,
    val confidence: Double,
    val riskLevel: String,
    val entryPoints: List<String> = emptyList(),
    val coreFiles: List<String> = emptyList(),
    val screens: List<String> = emptyList(),
    val viewModels: List<String> = emptyList(),
    val useCases: List<String> = emptyList(),
    val repositories: List<String> = emptyList(),
    val uiComponents: List<String> = emptyList(),
    val navigationRoutes: List<String> = emptyList(),
    val analyticsEvents: List<String> = emptyList(),
    val dependencies: List<String> = emptyList(),
    val existingTests: List<String> = emptyList(),
    val missingTests: List<String> = emptyList(),
    val stateModel: StateModel? = null,
    val risks: List<Risk> = emptyList(),
    val suggestedPrChecklist: List<String> = emptyList()
)

@Serializable
data class StateModel(
    val stateClass: String? = null,
    val states: List<String> = emptyList(),
    val events: List<String> = emptyList(),
    val sideEffects: List<String> = emptyList()
)

@Serializable
data class DependencyInjection(
    val framework: String,
    val summary: String,
    val modules: List<DiModule> = emptyList(),
    val injectionPatterns: List<String> = emptyList(),
    val scopes: List<String> = emptyList(),
    val risks: List<Risk> = emptyList()
)

@Serializable
data class DiModule(
    val name: String,
    val path: String? = null,
    val provides: List<String> = emptyList(),
    val scope: String? = null,
    val risks: List<String> = emptyList()
)

@Serializable
data class Concurrency(
    val summary: String,
    val technologies: List<String> = emptyList(),
    val dispatchers: List<Dispatcher> = emptyList(),
    val flows: List<FlowInfo> = emptyList(),
    val backgroundWork: List<BackgroundWork> = emptyList(),
    val sharedMutableState: List<SharedMutableState> = emptyList(),
    val cancellationRisks: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val risks: List<Risk> = emptyList()
)

@Serializable
data class Dispatcher(
    val name: String,
    val usage: String,
    val relatedFiles: List<String> = emptyList()
)

@Serializable
data class FlowInfo(
    val name: String,
    val type: String,
    val owner: String? = null,
    val purpose: String? = null,
    val risks: List<String> = emptyList()
)

@Serializable
data class BackgroundWork(
    val name: String,
    val framework: String? = null,
    val purpose: String? = null,
    val risks: List<String> = emptyList()
)

@Serializable
data class SharedMutableState(
    val name: String,
    val owner: String? = null,
    val risk: String? = null
)

@Serializable
data class UiLayer(
    val framework: String,
    val summary: String,
    val screens: List<Screen> = emptyList(),
    val stateManagement: StateManagement? = null,
    val navigation: Navigation? = null,
    val recompositionRisks: List<RecompositionRisk> = emptyList(),
    val accessibility: Accessibility? = null
)

@Serializable
data class Screen(
    val id: String,
    val name: String,
    val path: String? = null,
    val route: String? = null,
    val stateSource: String? = null,
    val uiState: String? = null,
    val userActions: List<String> = emptyList(),
    val reusableComponents: List<String> = emptyList(),
    val risks: List<String> = emptyList()
)

@Serializable
data class StateManagement(
    val pattern: String? = null,
    val stateClasses: List<String> = emptyList(),
    val sideEffectHandling: List<String> = emptyList(),
    val risks: List<String> = emptyList()
)

@Serializable
data class Navigation(
    val framework: String? = null,
    val routes: List<NavigationRoute> = emptyList(),
    val risks: List<String> = emptyList()
)

@Serializable
data class NavigationRoute(
    val route: String,
    val screen: String? = null,
    val arguments: List<String> = emptyList(),
    val deepLinks: List<String> = emptyList()
)

@Serializable
data class RecompositionRisk(
    val title: String,
    val affectedFiles: List<String> = emptyList(),
    val recommendation: String? = null
)

@Serializable
data class Accessibility(
    val status: String? = null,
    val concerns: List<String> = emptyList(),
    val recommendations: List<String> = emptyList()
)

@Serializable
data class Networking(
    val summary: String,
    val libraries: List<String> = emptyList(),
    val apiClients: List<ApiClient> = emptyList(),
    val interceptors: List<Interceptor> = emptyList(),
    val errorHandling: ErrorHandling? = null,
    val recommendations: List<String> = emptyList()
)

@Serializable
data class ApiClient(
    val name: String,
    val path: String? = null,
    val baseUrlSource: String? = null,
    val endpoints: List<String> = emptyList(),
    val risks: List<String> = emptyList()
)

@Serializable
data class Interceptor(
    val name: String,
    val purpose: String? = null,
    val risks: List<String> = emptyList()
)

@Serializable
data class ErrorHandling(
    val pattern: String? = null,
    val risks: List<String> = emptyList()
)

@Serializable
data class Persistence(
    val summary: String,
    val technologies: List<String> = emptyList(),
    val databases: List<Database> = emptyList(),
    val keyValueStorage: List<KeyValueStorage> = emptyList(),
    val caching: Caching? = null
)

@Serializable
data class Database(
    val name: String,
    val path: String? = null,
    val entities: List<String> = emptyList(),
    val daos: List<String> = emptyList(),
    val migrationRisks: List<String> = emptyList()
)

@Serializable
data class KeyValueStorage(
    val name: String,
    val purpose: String? = null,
    val risks: List<String> = emptyList()
)

@Serializable
data class Caching(
    val strategy: String? = null,
    val risks: List<String> = emptyList()
)

@Serializable
data class Testing(
    val summary: String,
    val frameworks: List<String> = emptyList(),
    val existingTests: List<ExistingTest> = emptyList(),
    val missingTestAreas: List<MissingTestArea> = emptyList(),
    val testabilityConcerns: List<String> = emptyList(),
    val recommendations: List<String> = emptyList()
)

@Serializable
data class ExistingTest(
    val name: String,
    val path: String? = null,
    val type: String,
    val covers: List<String> = emptyList(),
    val gaps: List<String> = emptyList()
)

@Serializable
data class MissingTestArea(
    val area: String,
    val priority: String,
    val suggestedTests: List<String> = emptyList()
)

@Serializable
data class Risk(
    val id: String,
    val severity: String,
    val category: String,
    val title: String,
    val affectedModules: List<String> = emptyList(),
    val affectedFiles: List<String> = emptyList(),
    val whyItMatters: String? = null,
    val recommendation: String? = null,
    val suggestedTest: String? = null,
    val confidence: Double? = null
)

@Serializable
data class PrReadiness(
    val score: Int,
    val summary: String,
    val readyItems: List<String> = emptyList(),
    val attentionItems: List<String> = emptyList(),
    val suggestedPrDescription: String? = null,
    val qaChecklist: List<String> = emptyList(),
    val reviewerNotes: List<String> = emptyList(),
    val rollbackPlan: List<String> = emptyList()
)

@Serializable
data class Documentation(
    val existingDocs: List<ExistingDoc> = emptyList(),
    val suggestedDocs: List<SuggestedDoc> = emptyList()
)

@Serializable
data class ExistingDoc(
    val name: String,
    val path: String? = null,
    val quality: String? = null,
    val gaps: List<String> = emptyList()
)

@Serializable
data class SuggestedDoc(
    val title: String,
    val reason: String? = null
)

@Serializable
data class BobUsage(
    val usedFor: List<String> = emptyList(),
    val bobIdeSessions: List<BobIdeSession> = emptyList(),
    val limitations: List<String> = emptyList()
)

@Serializable
data class BobIdeSession(
    val title: String,
    val expectedExportPath: String? = null

@Serializable
data class PerformanceMetrics(
    val buildTimeSeconds: Int,
    val appSizeMB: Double,
    val methodCount: Int,
    val largeFiles: List<LargeFile> = emptyList(),
    val complexMethods: List<ComplexMethod> = emptyList(),
    val slowModules: List<SlowModule> = emptyList()
)

@Serializable
data class LargeFile(
    val path: String,
    val sizeKB: Int,
    val linesOfCode: Int
)

@Serializable
data class ComplexMethod(
    val name: String,
    val filePath: String,
    val complexity: Int,
    val linesOfCode: Int
)

@Serializable
data class SlowModule(
    val name: String,
    val buildTimeSeconds: Int
)

@Serializable
data class SmartRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val reasoning: String,
    val impact: ImpactLevel,
    val effort: EffortLevel,
    val category: String,
    val codeExample: String? = null,
    val learnMoreUrl: String? = null
)

enum class ImpactLevel { LOW, MEDIUM, HIGH }
enum class EffortLevel { SMALL, MEDIUM, LARGE }

@Serializable
data class ArchitectureDiagram(
    val nodes: List<DiagramNode> = emptyList(),
    val connections: List<DiagramConnection> = emptyList()
)

@Serializable
data class DiagramNode(
    val id: String,
    val label: String,
    val type: String,
    val x: Float,
    val y: Float
)

@Serializable
data class DiagramConnection(
    val from: String,
    val to: String,
    val label: String
)
)

// Made with Bob
