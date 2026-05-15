package com.vero.repolens.data.export

import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.vero.repolens.data.models.RepoLensReport
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates PDF format reports using iText
 */
@Singleton
class PdfGenerator @Inject constructor() {

    suspend fun generate(report: RepoLensReport, outputFile: File): Result<File> {
        return try {
            val writer = PdfWriter(outputFile)
            val pdfDoc = PdfDocument(writer)
            val document = Document(pdfDoc)

            // Title
            document.add(
                Paragraph(report.repository.name)
                    .setFontSize(24f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
            )

            // Summary
            document.add(
                Paragraph("Summary")
                    .setFontSize(18f)
                    .setBold()
                    .setMarginTop(20f)
            )
            document.add(Paragraph(report.repository.summary))

            // Metadata
            document.add(
                Paragraph("Report Information")
                    .setFontSize(18f)
                    .setBold()
                    .setMarginTop(20f)
            )
            document.add(Paragraph("Generated: ${report.metadata.generatedAt}"))
            document.add(Paragraph("Analysis Type: ${report.metadata.analysisType}"))
            document.add(Paragraph("Confidence: ${(report.metadata.confidence * 100).toInt()}%"))

            // Repository Details
            document.add(
                Paragraph("Repository Details")
                    .setFontSize(18f)
                    .setBold()
                    .setMarginTop(20f)
            )
            document.add(Paragraph("Language: ${report.repository.mainLanguage}"))
            document.add(Paragraph("Platform: ${report.repository.platform}"))
            document.add(Paragraph("Architecture: ${report.repository.architectureStyle}"))
            report.repository.buildSystem?.let {
                document.add(Paragraph("Build System: $it"))
            }

            // Metrics Table
            document.add(
                Paragraph("Metrics")
                    .setFontSize(18f)
                    .setBold()
                    .setMarginTop(20f)
            )
            
            val metricsTable = Table(UnitValue.createPercentArray(floatArrayOf(70f, 30f)))
                .setWidth(UnitValue.createPercentValue(100f))
            
            metricsTable.addHeaderCell("Metric")
            metricsTable.addHeaderCell("Count")
            
            metricsTable.addCell("Modules")
            metricsTable.addCell(report.metrics.modulesCount.toString())
            metricsTable.addCell("Features")
            metricsTable.addCell(report.metrics.featuresCount.toString())
            metricsTable.addCell("Screens")
            metricsTable.addCell(report.metrics.screensCount.toString())
            metricsTable.addCell("ViewModels")
            metricsTable.addCell(report.metrics.viewModelsCount.toString())
            metricsTable.addCell("Existing Tests")
            metricsTable.addCell(report.metrics.existingTestsCount.toString())
            metricsTable.addCell("Missing Tests")
            metricsTable.addCell(report.metrics.missingTestsCount.toString())
            metricsTable.addCell("High Risk Areas")
            metricsTable.addCell(report.metrics.highRiskAreasCount.toString())
            metricsTable.addCell("Critical Risk Areas")
            metricsTable.addCell(report.metrics.criticalRiskAreasCount.toString())
            
            document.add(metricsTable)

            // Architecture
            document.add(
                Paragraph("Architecture")
                    .setFontSize(18f)
                    .setBold()
                    .setMarginTop(20f)
            )
            document.add(Paragraph("Style: ${report.architecture.style}").setBold())
            document.add(Paragraph(report.architecture.summary))

            // Modules
            if (report.modules.isNotEmpty()) {
                document.add(
                    Paragraph("Modules")
                        .setFontSize(18f)
                        .setBold()
                        .setMarginTop(20f)
                )
                report.modules.take(10).forEach { module ->
                    document.add(
                        Paragraph(module.name)
                            .setFontSize(14f)
                            .setBold()
                            .setMarginTop(10f)
                    )
                    document.add(Paragraph("Type: ${module.type}"))
                    document.add(Paragraph("Risk Level: ${module.riskLevel}"))
                    document.add(Paragraph(module.responsibility))
                }
                if (report.modules.size > 10) {
                    document.add(Paragraph("... and ${report.modules.size - 10} more modules"))
                }
            }

            // Risks
            if (report.risks.isNotEmpty()) {
                document.add(
                    Paragraph("Risks")
                        .setFontSize(18f)
                        .setBold()
                        .setMarginTop(20f)
                )
                
                val criticalRisks = report.risks.filter { it.severity.equals("critical", true) }
                val highRisks = report.risks.filter { it.severity.equals("high", true) }
                
                if (criticalRisks.isNotEmpty()) {
                    document.add(
                        Paragraph("Critical Risks")
                            .setFontSize(16f)
                            .setBold()
                            .setFontColor(ColorConstants.RED)
                            .setMarginTop(10f)
                    )
                    criticalRisks.forEach { risk ->
                        document.add(
                            Paragraph(risk.title)
                                .setFontSize(14f)
                                .setBold()
                                .setMarginTop(8f)
                        )
                        document.add(Paragraph("Category: ${risk.category}"))
                        risk.whyItMatters?.let { document.add(Paragraph(it)) }
                        risk.recommendation?.let { 
                            document.add(Paragraph("Recommendation: $it").setItalic())
                        }
                    }
                }
                
                if (highRisks.isNotEmpty()) {
                    document.add(
                        Paragraph("High Risks")
                            .setFontSize(16f)
                            .setBold()
                            .setFontColor(ColorConstants.ORANGE)
                            .setMarginTop(10f)
                    )
                    highRisks.take(5).forEach { risk ->
                        document.add(
                            Paragraph(risk.title)
                                .setFontSize(14f)
                                .setBold()
                                .setMarginTop(8f)
                        )
                        document.add(Paragraph("Category: ${risk.category}"))
                    }
                }
            }

            // PR Readiness
            report.prReadiness?.let { pr ->
                document.add(
                    Paragraph("PR Readiness")
                        .setFontSize(18f)
                        .setBold()
                        .setMarginTop(20f)
                )
                document.add(Paragraph("Score: ${pr.score}/100").setBold())
                document.add(Paragraph(pr.summary))
            }

            // Footer
            document.add(
                Paragraph("Generated by RepoLens")
                    .setFontSize(10f)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30f)
            )

            document.close()
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Made with Bob