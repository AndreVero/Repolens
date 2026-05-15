package com.vero.repolens.data.export

import com.vero.repolens.data.models.RepoLensReport
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates Markdown format reports
 */
@Singleton
class MarkdownGenerator @Inject constructor() {

    suspend fun generate(report: RepoLensReport, outputFile: File): Result<File> {
        return try {
            val markdown = buildString {
                appendLine("# ${report.repository.name}")
                appendLine()
                appendLine("## Summary")
                appendLine(report.repository.summary)
                appendLine()
                
                // Metadata
                appendLine("## Report Information")
                appendLine("- **Generated**: ${report.metadata.generatedAt}")
                appendLine("- **Analysis Type**: ${report.metadata.analysisType}")
                appendLine("- **Confidence**: ${(report.metadata.confidence * 100).toInt()}%")
                appendLine()
                
                // Repository Info
                appendLine("## Repository Details")
                appendLine("- **Language**: ${report.repository.mainLanguage}")
                appendLine("- **Platform**: ${report.repository.platform}")
                appendLine("- **Architecture**: ${report.repository.architectureStyle}")
                appendLine("- **Build System**: ${report.repository.buildSystem ?: "N/A"}")
                appendLine()
                
                // Metrics
                appendLine("## Metrics")
                appendLine("| Metric | Count |")
                appendLine("|--------|-------|")
                appendLine("| Modules | ${report.metrics.modulesCount} |")
                appendLine("| Features | ${report.metrics.featuresCount} |")
                appendLine("| Screens | ${report.metrics.screensCount} |")
                appendLine("| ViewModels | ${report.metrics.viewModelsCount} |")
                appendLine("| Existing Tests | ${report.metrics.existingTestsCount} |")
                appendLine("| Missing Tests | ${report.metrics.missingTestsCount} |")
                appendLine("| High Risk Areas | ${report.metrics.highRiskAreasCount} |")
                appendLine("| Critical Risk Areas | ${report.metrics.criticalRiskAreasCount} |")
                appendLine()
                
                // Architecture
                appendLine("## Architecture")
                appendLine("**Style**: ${report.architecture.style}")
                appendLine()
                appendLine(report.architecture.summary)
                appendLine()
                
                if (report.architecture.layers.isNotEmpty()) {
                    appendLine("### Layers")
                    report.architecture.layers.forEach { layer ->
                        appendLine("#### ${layer.name}")
                        appendLine(layer.responsibility)
                        appendLine()
                    }
                }
                
                // Modules
                if (report.modules.isNotEmpty()) {
                    appendLine("## Modules")
                    report.modules.forEach { module ->
                        appendLine("### ${module.name}")
                        appendLine("- **Type**: ${module.type}")
                        appendLine("- **Risk Level**: ${module.riskLevel}")
                        appendLine("- **Responsibility**: ${module.responsibility}")
                        appendLine()
                    }
                }
                
                // Features
                if (report.features.isNotEmpty()) {
                    appendLine("## Features")
                    report.features.forEach { feature ->
                        appendLine("### ${feature.name}")
                        appendLine(feature.description)
                        appendLine()
                        appendLine("- **Complexity**: ${feature.complexity}")
                        appendLine("- **Risk Level**: ${feature.riskLevel}")
                        appendLine("- **Confidence**: ${(feature.confidence * 100).toInt()}%")
                        appendLine()
                    }
                }
                
                // Risks
                if (report.risks.isNotEmpty()) {
                    appendLine("## Risks")
                    val criticalRisks = report.risks.filter { it.severity.equals("critical", true) }
                    val highRisks = report.risks.filter { it.severity.equals("high", true) }
                    val mediumRisks = report.risks.filter { it.severity.equals("medium", true) }
                    val lowRisks = report.risks.filter { it.severity.equals("low", true) }
                    
                    if (criticalRisks.isNotEmpty()) {
                        appendLine("### Critical Risks")
                        criticalRisks.forEach { risk ->
                            appendLine("#### ${risk.title}")
                            appendLine("**Category**: ${risk.category}")
                            appendLine()
                            risk.whyItMatters?.let { appendLine(it); appendLine() }
                            risk.recommendation?.let { appendLine("**Recommendation**: $it"); appendLine() }
                        }
                    }
                    
                    if (highRisks.isNotEmpty()) {
                        appendLine("### High Risks")
                        highRisks.forEach { risk ->
                            appendLine("#### ${risk.title}")
                            appendLine("**Category**: ${risk.category}")
                            appendLine()
                        }
                    }
                }
                
                // Testing
                report.testing?.let { testing ->
                    appendLine("## Testing")
                    appendLine(testing.summary)
                    appendLine()
                    
                    if (testing.existingTests.isNotEmpty()) {
                        appendLine("### Existing Tests")
                        testing.existingTests.forEach { test ->
                            appendLine("- **${test.name}** (${test.type})")
                        }
                        appendLine()
                    }
                    
                    if (testing.missingTestAreas.isNotEmpty()) {
                        appendLine("### Missing Test Areas")
                        testing.missingTestAreas.forEach { area ->
                            appendLine("- **${area.area}** (Priority: ${area.priority})")
                        }
                        appendLine()
                    }
                }
                
                // PR Readiness
                report.prReadiness?.let { pr ->
                    appendLine("## PR Readiness")
                    appendLine("**Score**: ${pr.score}/100")
                    appendLine()
                    appendLine(pr.summary)
                    appendLine()
                }
                
                appendLine("---")
                appendLine("*Generated by RepoLens*")
            }
            
            outputFile.writeText(markdown)
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Made with Bob