package com.vero.repolens.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vero.repolens.data.models.Module
import com.vero.repolens.data.models.Risk
import com.vero.repolens.data.models.TestCoverage
import com.vero.repolens.ui.components.FilePathChip
import com.vero.repolens.ui.components.SeverityBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleDetailScreen(
    module: Module,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(module.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ModuleHeroCard(module = module)
            }

            item {
                ModuleOverviewStrip(module = module)
            }

            if (module.keyPackages.isNotEmpty()) {
                item {
                    DetailSectionCard(title = "Key Packages") {
                        TagList(items = module.keyPackages)
                    }
                }
            }

            if (module.importantFiles.isNotEmpty()) {
                item {
                    DetailSectionCard(title = "Important Files") {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            module.importantFiles.forEach { file ->
                                FilePathChip(path = file)
                            }
                        }
                    }
                }
            }

            if (module.dependencies.isNotEmpty()) {
                item {
                    DetailSectionCard(title = "Dependencies") {
                        TagList(items = module.dependencies)
                    }
                }
            }

            if (module.publicApis.isNotEmpty()) {
                item {
                    DetailSectionCard(title = "Public APIs") {
                        TagList(items = module.publicApis)
                    }
                }
            }

            module.testCoverage?.let { coverage ->
                item {
                    TestCoverageCard(coverage = coverage)
                }
            }

            if (module.risks.isNotEmpty()) {
                item {
                    Text(
                        text = "Risks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(module.risks) { risk ->
                    RiskDetailCard(risk = risk)
                }
            }
        }
    }
}

@Composable
private fun ModuleHeroCard(module: Module) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = module.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = module.type.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                SeverityBadge(severity = module.riskLevel)
            }

            Text(
                text = module.responsibility,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                module.path?.let { ModuleInfoChip(text = it) }
                module.stability?.let {
                    ModuleInfoChip(text = "Stability: ${it.replaceFirstChar { value -> value.uppercase() }}")
                }
                ModuleInfoChip(text = "${module.dependencies.size} dependencies")
                if (module.publicApis.isNotEmpty()) {
                    ModuleInfoChip(text = "${module.publicApis.size} APIs")
                }
            }
        }
    }
}

@Composable
private fun ModuleOverviewStrip(module: Module) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModuleInfoChip(text = "${module.keyPackages.size} packages")
            ModuleInfoChip(text = "${module.importantFiles.size} files")
            module.testCoverage?.let { ModuleInfoChip(text = "Tests: ${it.status}") }
            if (module.testCoverage?.missingAreas?.isNotEmpty() == true) {
                ModuleInfoChip(text = "${module.testCoverage.missingAreas.size} gaps")
            }
            if (module.risks.isNotEmpty()) {
                ModuleInfoChip(text = "${module.risks.size} risks")
            }
        }
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}

@Composable
private fun TagList(items: List<String>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            ModuleInfoChip(text = item)
        }
    }
}

@Composable
private fun TestCoverageCard(coverage: TestCoverage) {
    DetailSectionCard(title = "Test Coverage") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModuleInfoChip(text = "Status: ${coverage.status}")
                if (coverage.existingTests.isNotEmpty()) {
                    ModuleInfoChip(text = "${coverage.existingTests.size} tests")
                }
                if (coverage.missingAreas.isNotEmpty()) {
                    ModuleInfoChip(text = "${coverage.missingAreas.size} gaps")
                }
            }

            if (coverage.existingTests.isNotEmpty()) {
                CoverageListCard(
                    title = "Existing Tests",
                    items = coverage.existingTests,
                    accent = MaterialTheme.colorScheme.primary,
                    tone = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                )
            }

            if (coverage.missingAreas.isNotEmpty()) {
                CoverageListCard(
                    title = "Missing Test Areas",
                    items = coverage.missingAreas,
                    accent = MaterialTheme.colorScheme.error,
                    tone = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                )
            }
        }
    }
}

@Composable
private fun CoverageListCard(
    title: String,
    items: List<String>,
    accent: Color,
    tone: Color
) {
    Surface(
        color = tone,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = accent
            )
            items.forEach { item ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = accent
                    )
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RiskDetailCard(risk: Risk) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.92f)
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = risk.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = risk.category.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.78f)
                        )
                    }
                }
                SeverityBadge(severity = risk.severity)
            }

            risk.whyItMatters?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            risk.recommendation?.let {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (risk.affectedFiles.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Affected Files",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        risk.affectedFiles.forEach { file ->
                            FilePathChip(path = file)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuleInfoChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(10.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ) {}
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Made with Bob
