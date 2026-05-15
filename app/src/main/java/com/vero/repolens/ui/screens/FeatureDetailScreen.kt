package com.vero.repolens.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vero.repolens.data.models.Feature
import com.vero.repolens.data.models.Risk
import com.vero.repolens.data.models.StateModel
import com.vero.repolens.ui.components.ComplexityBadge
import com.vero.repolens.ui.components.ConfidenceBadge
import com.vero.repolens.ui.components.FilePathChip
import com.vero.repolens.ui.components.SeverityBadge
import kotlinx.coroutines.launch

private enum class FeatureImplementationPage(val title: String) {
    ARCHITECTURE("Architecture"),
    UI("UI Components"),
    STATE("State Model")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureDetailScreen(
    feature: Feature,
    onNavigateBack: () -> Unit,
    onOpenVisualizer: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(feature.name) },
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
                FeatureHeroCard(
                    feature = feature,
                    onOpenVisualizer = onOpenVisualizer
                )
            }

            item {
                FeatureOverviewStrip(feature = feature)
            }

            if (feature.entryPoints.isNotEmpty()) {
                item {
                    FeatureSectionCard(title = "Entry Points") {
                        FileChipGrid(feature.entryPoints)
                    }
                }
            }

            if (feature.coreFiles.isNotEmpty()) {
                item {
                    FeatureSectionCard(title = "Core Files") {
                        FileChipGrid(feature.coreFiles)
                    }
                }
            }

            if (
                feature.viewModels.isNotEmpty() ||
                feature.useCases.isNotEmpty() ||
                feature.repositories.isNotEmpty() ||
                feature.uiComponents.isNotEmpty() ||
                feature.stateModel != null
            ) {
                item {
                    FeatureImplementationPager(feature = feature)
                }
            }

            item {
                TestingCard(feature = feature)
            }

            if (feature.risks.isNotEmpty()) {
                item {
                    Text(
                        text = "Risks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(feature.risks) { risk ->
                    FeatureRiskCard(risk = risk)
                }
            }

            if (feature.suggestedPrChecklist.isNotEmpty()) {
                item {
                    FeatureSectionCard(title = "PR Checklist") {
                        ChecklistCard(feature.suggestedPrChecklist)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeatureImplementationPager(feature: Feature) {
    val pages = remember(feature) {
        buildList {
            if (
                feature.viewModels.isNotEmpty() ||
                feature.useCases.isNotEmpty() ||
                feature.repositories.isNotEmpty()
            ) {
                add(FeatureImplementationPage.ARCHITECTURE)
            }
            if (feature.uiComponents.isNotEmpty()) {
                add(FeatureImplementationPage.UI)
            }
            if (feature.stateModel != null) {
                add(FeatureImplementationPage.STATE)
            }
        }
    }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    FeatureSectionCard(title = "Implementation View") {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pages.forEachIndexed { index, page ->
                    FilterChip(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        label = { Text(page.title) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { pageIndex ->
                when (pages[pageIndex]) {
                    FeatureImplementationPage.ARCHITECTURE -> ArchitectureComponentsPage(feature = feature)
                    FeatureImplementationPage.UI -> UiComponentsPage(feature = feature)
                    FeatureImplementationPage.STATE -> StateModelPage(stateModel = feature.stateModel)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                pages.indices.forEach { index ->
                    val selected = pagerState.currentPage == index
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = if (selected) 20.dp else 8.dp, height = 8.dp),
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                        },
                        shape = MaterialTheme.shapes.small
                    ) {}
                }
            }
        }
    }
}

@Composable
private fun ArchitectureComponentsPage(feature: Feature) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (feature.viewModels.isNotEmpty()) {
            FeatureTagGroup(title = "ViewModels", items = feature.viewModels)
        }
        if (feature.useCases.isNotEmpty()) {
            FeatureTagGroup(title = "Use Cases", items = feature.useCases)
        }
        if (feature.repositories.isNotEmpty()) {
            FeatureTagGroup(title = "Repositories", items = feature.repositories)
        }
    }
}

@Composable
private fun UiComponentsPage(feature: Feature) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Reusable and screen-level UI pieces connected to this feature.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FeatureTagCloud(feature.uiComponents)
    }
}

@Composable
private fun StateModelPage(stateModel: StateModel?) {
    if (stateModel == null) return

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        stateModel.stateClass?.let {
            DetailChip("State class: $it")
        }
        if (stateModel.states.isNotEmpty()) {
            FeatureTagGroup(title = "States", items = stateModel.states)
        }
        if (stateModel.events.isNotEmpty()) {
            FeatureTagGroup(title = "Events", items = stateModel.events)
        }
        if (stateModel.sideEffects.isNotEmpty()) {
            FeatureTagGroup(title = "Side Effects", items = stateModel.sideEffects)
        }
    }
}

@Composable
private fun FeatureHeroCard(
    feature: Feature,
    onOpenVisualizer: () -> Unit
) {
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
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.58f),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = feature.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        feature.businessPurpose?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                            )
                        }
                    }
                }
                SeverityBadge(severity = feature.riskLevel)
            }

            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ComplexityBadge(
                        complexity = feature.complexity,
                        label = "Complexity"
                    )
                    ConfidenceBadge(
                        confidence = feature.confidence,
                        label = "Confidence"
                    )
                }

                if (feature.navigationRoutes.isNotEmpty() || feature.analyticsEvents.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (feature.navigationRoutes.isNotEmpty()) {
                            DetailChip("${feature.navigationRoutes.size} routes")
                        }
                        if (feature.analyticsEvents.isNotEmpty()) {
                            DetailChip("${feature.analyticsEvents.size} analytics")
                        }
                    }
                }
            }

            Button(onClick = onOpenVisualizer) {
                Text("Open Visualizer")
            }
        }
    }
}

@Composable
private fun FeatureOverviewStrip(feature: Feature) {
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
            DetailChip("${feature.entryPoints.size} entries")
            DetailChip("${feature.coreFiles.size} files")
            DetailChip("${feature.existingTests.size} tests")
            if (feature.missingTests.isNotEmpty()) {
                DetailChip("${feature.missingTests.size} missing")
            }
            if (feature.viewModels.isNotEmpty()) {
                DetailChip("${feature.viewModels.size} viewmodels")
            }
            if (feature.useCases.isNotEmpty()) {
                DetailChip("${feature.useCases.size} use cases")
            }
        }
    }
}

@Composable
private fun FeatureSectionCard(
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
private fun FileChipGrid(items: List<String>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            FilePathChip(path = item)
        }
    }
}

@Composable
private fun FeatureTagGroup(
    title: String,
    items: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FeatureTagCloud(items)
    }
}

@Composable
private fun FeatureTagCloud(items: List<String>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            DetailChip(item)
        }
    }
}

@Composable
private fun StateModelCard(stateModel: StateModel) {
    FeatureSectionCard(title = "State Model") {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            stateModel.stateClass?.let {
                DetailChip("State class: $it")
            }
            if (stateModel.states.isNotEmpty()) {
                FeatureTagGroup(title = "States", items = stateModel.states)
            }
            if (stateModel.events.isNotEmpty()) {
                FeatureTagGroup(title = "Events", items = stateModel.events)
            }
            if (stateModel.sideEffects.isNotEmpty()) {
                FeatureTagGroup(title = "Side Effects", items = stateModel.sideEffects)
            }
        }
    }
}

@Composable
private fun TestingCard(feature: Feature) {
    FeatureSectionCard(title = "Testing") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailChip("${feature.existingTests.size} existing")
                if (feature.missingTests.isNotEmpty()) {
                    DetailChip("${feature.missingTests.size} missing")
                }
            }

            if (feature.existingTests.isNotEmpty()) {
                FeatureListPanel(
                    title = "Existing Tests",
                    items = feature.existingTests,
                    accent = MaterialTheme.colorScheme.primary,
                    tone = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                )
            }

            if (feature.missingTests.isNotEmpty()) {
                FeatureListPanel(
                    title = "Missing Tests",
                    items = feature.missingTests,
                    accent = MaterialTheme.colorScheme.error,
                    tone = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                )
            }
        }
    }
}

@Composable
private fun FeatureListPanel(
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
private fun FeatureRiskCard(risk: Risk) {
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
        }
    }
}

@Composable
private fun ChecklistCard(items: List<String>) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items.forEach { item ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "[]",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
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
private fun DetailChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f),
        shape = MaterialTheme.shapes.large
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Made with Bob
