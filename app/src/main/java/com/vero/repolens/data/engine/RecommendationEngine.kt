package com.vero.repolens.data.engine

import com.vero.repolens.data.models.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Engine for generating smart recommendations based on report analysis
 */
@Singleton
class RecommendationEngine @Inject constructor() {

    fun generateRecommendations(report: RepoLensReport): List<SmartRecommendation> {
        val recommendations = mutableListOf<SmartRecommendation>()

        // Analyze test coverage
        if (report.metrics.missingTestsCount > report.metrics.existingTestsCount) {
            recommendations.add(
                SmartRecommendation(
                    id = "rec_test_coverage",
                    title = "Improve Test Coverage",
                    description = "Your project has ${report.metrics.missingTestsCount} missing tests compared to ${report.metrics.existingTestsCount} existing tests.",
                    reasoning = "Good test coverage (>80%) helps catch bugs early, improves code quality, and makes refactoring safer.",
                    impact = ImpactLevel.HIGH,
                    effort = EffortLevel.LARGE,
                    category = "Testing",
                    codeExample = """
                        @Test
                        fun `test feature behavior`() {
                            // Arrange
                            val viewModel = MyViewModel()
                            
                            // Act
                            viewModel.performAction()
                            
                            // Assert
                            assertEquals(expected, viewModel.state.value)
                        }
                    """.trimIndent(),
                    learnMoreUrl = "https://developer.android.com/training/testing"
                )
            )
        }

        // Analyze critical risks
        if (report.metrics.criticalRiskAreasCount > 0) {
            recommendations.add(
                SmartRecommendation(
                    id = "rec_critical_risks",
                    title = "Address Critical Risks",
                    description = "Found ${report.metrics.criticalRiskAreasCount} critical risk areas that need immediate attention.",
                    reasoning = "Critical risks can lead to app crashes, data loss, or security vulnerabilities in production.",
                    impact = ImpactLevel.HIGH,
                    effort = EffortLevel.MEDIUM,
                    category = "Risk Management"
                )
            )
        }

        // Analyze high risk areas
        if (report.metrics.highRiskAreasCount > 3) {
            recommendations.add(
                SmartRecommendation(
                    id = "rec_high_risks",
                    title = "Reduce High Risk Areas",
                    description = "Your project has ${report.metrics.highRiskAreasCount} high risk areas.",
                    reasoning = "High risk areas are prone to bugs and make the codebase harder to maintain.",
                    impact = ImpactLevel.MEDIUM,
                    effort = EffortLevel.LARGE,
                    category = "Code Quality"
                )
            )
        }

        // Analyze architecture
        if (report.architecture.concerns.isNotEmpty()) {
            recommendations.add(
                SmartRecommendation(
                    id = "rec_architecture",
                    title = "Address Architecture Concerns",
                    description = "Found ${report.architecture.concerns.size} architecture concerns.",
                    reasoning = "Addressing architecture concerns early prevents technical debt and improves maintainability.",
                    impact = ImpactLevel.MEDIUM,
                    effort = EffortLevel.LARGE,
                    category = "Architecture"
                )
            )
        }

        // Analyze module count
        if (report.metrics.modulesCount > 20) {
            recommendations.add(
                SmartRecommendation(
                    id = "rec_module_organization",
                    title = "Consider Module Organization",
                    description = "Your project has ${report.metrics.modulesCount} modules.",
                    reasoning = "Large number of modules can increase build times and complexity. Consider consolidating related modules.",
                    impact = ImpactLevel.LOW,
                    effort = EffortLevel.LARGE,
                    category = "Project Structure"
                )
            )
        }

        // Analyze concurrency
        report.concurrency?.let { concurrency ->
            if (concurrency.sharedMutableState.isNotEmpty()) {
                recommendations.add(
                    SmartRecommendation(
                        id = "rec_concurrency",
                        title = "Review Shared Mutable State",
                        description = "Found ${concurrency.sharedMutableState.size} instances of shared mutable state.",
                        reasoning = "Shared mutable state can lead to race conditions and hard-to-debug concurrency issues.",
                        impact = ImpactLevel.HIGH,
                        effort = EffortLevel.MEDIUM,
                        category = "Concurrency",
                        codeExample = """
                            // Instead of:
                            var sharedCounter = 0
                            
                            // Use:
                            private val _counter = MutableStateFlow(0)
                            val counter: StateFlow<Int> = _counter.asStateFlow()
                        """.trimIndent()
                    )
                )
            }
        }

        // Analyze dependency injection
        report.dependencyInjection?.let { di ->
            if (di.risks.isNotEmpty()) {
                recommendations.add(
                    SmartRecommendation(
                        id = "rec_di",
                        title = "Improve Dependency Injection",
                        description = "Found ${di.risks.size} DI-related risks.",
                        reasoning = "Proper DI setup improves testability and reduces coupling between components.",
                        impact = ImpactLevel.MEDIUM,
                        effort = EffortLevel.SMALL,
                        category = "Dependency Injection"
                    )
                )
            }
        }

        // Analyze UI layer
        report.uiLayer?.let { ui ->
            if (ui.recompositionRisks.isNotEmpty()) {
                recommendations.add(
                    SmartRecommendation(
                        id = "rec_recomposition",
                        title = "Optimize Recomposition",
                        description = "Found ${ui.recompositionRisks.size} potential recomposition issues.",
                        reasoning = "Unnecessary recompositions can cause performance issues and battery drain.",
                        impact = ImpactLevel.MEDIUM,
                        effort = EffortLevel.SMALL,
                        category = "Performance",
                        codeExample = """
                            // Use remember and derivedStateOf:
                            val expensiveValue = remember(key) {
                                computeExpensiveValue()
                            }
                        """.trimIndent()
                    )
                )
            }
        }

        // Analyze documentation
        report.documentation?.let { docs ->
            if (docs.suggestedDocs.isNotEmpty()) {
                recommendations.add(
                    SmartRecommendation(
                        id = "rec_documentation",
                        title = "Add Missing Documentation",
                        description = "Consider adding ${docs.suggestedDocs.size} documentation files.",
                        reasoning = "Good documentation helps new team members onboard faster and reduces knowledge silos.",
                        impact = ImpactLevel.LOW,
                        effort = EffortLevel.SMALL,
                        category = "Documentation"
                    )
                )
            }
        }

        // Analyze PR readiness
        report.prReadiness?.let { pr ->
            if (pr.score < 70) {
                recommendations.add(
                    SmartRecommendation(
                        id = "rec_pr_readiness",
                        title = "Improve PR Readiness",
                        description = "PR readiness score is ${pr.score}/100.",
                        reasoning = "Higher PR readiness scores lead to faster reviews and fewer issues in production.",
                        impact = ImpactLevel.MEDIUM,
                        effort = EffortLevel.MEDIUM,
                        category = "Code Review"
                    )
                )
            }
        }

        return recommendations.sortedByDescending { 
            it.impact.ordinal * 10 + (3 - it.effort.ordinal)
        }
    }
}

// Made with Bob