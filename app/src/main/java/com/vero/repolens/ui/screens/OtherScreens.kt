package com.vero.repolens.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vero.repolens.data.models.Concurrency
import com.vero.repolens.data.models.DependencyInjection
import com.vero.repolens.data.models.Dispatcher
import com.vero.repolens.data.models.ExistingTest
import com.vero.repolens.data.models.FlowInfo
import com.vero.repolens.data.models.MissingTestArea
import com.vero.repolens.data.models.PrReadiness
import com.vero.repolens.data.models.Risk
import com.vero.repolens.data.models.Screen
import com.vero.repolens.data.models.StateManagement
import com.vero.repolens.data.models.Testing
import com.vero.repolens.data.models.UiLayer
import com.vero.repolens.ui.components.FilePathChip
import com.vero.repolens.ui.components.SeverityBadge
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestingScreen(
    testing: Testing,
    onNavigateBack: () -> Unit
) {
    DetailScaffold(title = "Testing", onNavigateBack = onNavigateBack) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeroInfoCard(
                    title = "Testing Health",
                    summary = testing.summary,
                    icon = Icons.Default.CheckCircle,
                    chips = buildList {
                        if (testing.frameworks.isNotEmpty()) add("${testing.frameworks.size} frameworks")
                        if (testing.existingTests.isNotEmpty()) add("${testing.existingTests.size} tests")
                        if (testing.missingTestAreas.isNotEmpty()) add("${testing.missingTestAreas.size} gaps")
                    },
                    footer = testing.frameworks.takeIf { it.isNotEmpty() }?.joinToString(", ")
                )
            }

            if (testing.existingTests.isNotEmpty()) {
                item {
                    ScreenSectionTitle("Existing Tests")
                }
                item {
                    PagerSection(
                        items = testing.existingTests,
                        titleFor = { it.name }
                    ) { test ->
                        ExistingTestCard(test = test, embedded = true)
                    }
                }
            }

            if (testing.missingTestAreas.isNotEmpty()) {
                item { ScreenSectionTitle("Missing Test Areas") }
                items(testing.missingTestAreas) { area ->
                    MissingTestAreaCard(area = area)
                }
            }

            if (testing.testabilityConcerns.isNotEmpty()) {
                item {
                    DetailSectionCard(title = "Testability Concerns") {
                        HighlightBulletList(
                            items = testing.testabilityConcerns,
                            accent = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (testing.recommendations.isNotEmpty()) {
                item {
                    DetailSectionCard(title = "Recommendations") {
                        HighlightBulletList(
                            items = testing.recommendations,
                            accent = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrReadinessScreen(
    prReadiness: PrReadiness,
    onNavigateBack: () -> Unit
) {
    DetailScaffold(title = "PR Readiness", onNavigateBack = onNavigateBack) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ScoreHeroCard(
                    score = prReadiness.score,
                    title = "PR Readiness Score",
                    summary = prReadiness.summary,
                    chips = buildList {
                        if (prReadiness.readyItems.isNotEmpty()) add("${prReadiness.readyItems.size} ready")
                        if (prReadiness.attentionItems.isNotEmpty()) add("${prReadiness.attentionItems.size} attention")
                        if (prReadiness.qaChecklist.isNotEmpty()) add("${prReadiness.qaChecklist.size} QA checks")
                    }
                )
            }

            if (prReadiness.readyItems.isNotEmpty()) {
                item {
                    DetailSectionCard(title = "Ready Items") {
                        HighlightBulletList(
                            items = prReadiness.readyItems,
                            accent = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (prReadiness.attentionItems.isNotEmpty()) {
                item {
                    DetailSectionCard(title = "Needs Attention") {
                        HighlightBulletList(
                            items = prReadiness.attentionItems,
                            accent = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            prReadiness.suggestedPrDescription?.let { description ->
                item {
                    DetailSectionCard(title = "Suggested PR Description") {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (prReadiness.qaChecklist.isNotEmpty()) {
                item {
                    DetailSectionCard(title = "QA Checklist") {
                        ChecklistList(items = prReadiness.qaChecklist)
                    }
                }
            }

            if (prReadiness.reviewerNotes.isNotEmpty()) {
                item {
                    DetailSectionCard(title = "Reviewer Notes") {
                        HighlightBulletList(
                            items = prReadiness.reviewerNotes,
                            accent = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            if (prReadiness.rollbackPlan.isNotEmpty()) {
                item {
                    DetailSectionCard(title = "Rollback Plan") {
                        HighlightBulletList(
                            items = prReadiness.rollbackPlan,
                            accent = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DependencyInjectionScreen(
    di: DependencyInjection,
    onNavigateBack: () -> Unit
) {
    DetailScaffold(title = "Dependency Injection", onNavigateBack = onNavigateBack) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeroInfoCard(
                    title = di.framework,
                    summary = di.summary,
                    icon = Icons.Default.Link,
                    chips = buildList {
                        if (di.modules.isNotEmpty()) add("${di.modules.size} modules")
                        if (di.risks.isNotEmpty()) add("${di.risks.size} risks")
                    }
                )
            }

            if (di.modules.isNotEmpty()) {
                item {
                    ScreenSectionTitle("DI Modules")
                }
                item {
                    PagerSection(
                        items = di.modules,
                        titleFor = { it.name }
                    ) { module ->
                        DetailSectionCard(title = module.name, embedded = true) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                module.scope?.let {
                                    DetailChip("Scope: $it")
                                }
                                if (module.provides.isNotEmpty()) {
                                    FeatureLikeTagCloud(module.provides)
                                }
                            }
                        }
                    }
                }
            }

            if (di.risks.isNotEmpty()) {
                item { ScreenSectionTitle("Risks") }
                items(di.risks) { risk ->
                    RiskInsightCard(risk = risk)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcurrencyScreen(
    concurrency: Concurrency,
    onNavigateBack: () -> Unit
) {
    DetailScaffold(title = "Concurrency", onNavigateBack = onNavigateBack) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeroInfoCard(
                    title = "Technologies",
                    summary = concurrency.summary,
                    icon = Icons.Default.Sync,
                    chips = concurrency.technologies
                )
            }

            if (concurrency.dispatchers.isNotEmpty()) {
                item {
                    ScreenSectionTitle("Dispatchers")
                }
                item {
                    PagerSection(
                        items = concurrency.dispatchers,
                        titleFor = { it.name }
                    ) { dispatcher ->
                        DispatcherCard(dispatcher = dispatcher, embedded = true)
                    }
                }
            }

            if (concurrency.flows.isNotEmpty()) {
                item {
                    ScreenSectionTitle("Flows")
                }
                item {
                    PagerSection(
                        items = concurrency.flows,
                        titleFor = { it.name }
                    ) { flow ->
                        FlowInfoCard(flow = flow, embedded = true)
                    }
                }
            }

            if (concurrency.backgroundWork.isNotEmpty()) {
                item {
                    DetailSectionCard(title = "Background Work") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            concurrency.backgroundWork.forEach { work ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                                    shape = MaterialTheme.shapes.large
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = work.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        work.framework?.let {
                                            Text(
                                                text = it,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        work.purpose?.let {
                                            Text(
                                                text = it,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (concurrency.recommendations.isNotEmpty()) {
                item {
                    DetailSectionCard(title = "Recommendations") {
                        HighlightBulletList(
                            items = concurrency.recommendations,
                            accent = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (concurrency.risks.isNotEmpty()) {
                item { ScreenSectionTitle("Risks") }
                items(concurrency.risks) { risk ->
                    RiskInsightCard(risk = risk)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UiLayerScreen(
    uiLayer: UiLayer,
    onNavigateBack: () -> Unit
) {
    DetailScaffold(title = "UI Layer", onNavigateBack = onNavigateBack) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeroInfoCard(
                    title = uiLayer.framework,
                    summary = uiLayer.summary,
                    icon = Icons.Default.Smartphone,
                    chips = buildList {
                        if (uiLayer.screens.isNotEmpty()) add("${uiLayer.screens.size} screens")
                        uiLayer.navigation?.routes?.takeIf { it.isNotEmpty() }?.let { add("${it.size} routes") }
                        uiLayer.recompositionRisks.takeIf { it.isNotEmpty() }?.let { add("${it.size} risks") }
                    }
                )
            }

            if (uiLayer.screens.isNotEmpty()) {
                item {
                    ScreenSectionTitle("Screens (${uiLayer.screens.size})")
                }
                item {
                    PagerSection(
                        items = uiLayer.screens,
                        titleFor = { it.name }
                    ) { screen ->
                        UiScreenCard(screen = screen, embedded = true)
                    }
                }
            }

            uiLayer.stateManagement?.let { stateManagement ->
                item {
                    StateManagementCard(stateManagement = stateManagement)
                }
            }

            if (uiLayer.navigation?.routes?.isNotEmpty() == true) {
                item {
                    DetailSectionCard(title = "Navigation") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            uiLayer.navigation.routes.forEach { route ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                                    shape = MaterialTheme.shapes.large
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = route.route,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        route.screen?.let {
                                            Text(
                                                text = it,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (route.arguments.isNotEmpty()) {
                                            FeatureLikeTagCloud(route.arguments)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (uiLayer.recompositionRisks.isNotEmpty()) {
                item { ScreenSectionTitle("Recomposition Risks") }
                items(uiLayer.recompositionRisks) { risk ->
                    DetailSectionCard(title = risk.title) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (risk.affectedFiles.isNotEmpty()) {
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
                            risk.recommendation?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailScaffold(
    title: String,
    onNavigateBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
        },
        content = content
    )
}

@Composable
private fun HeroInfoCard(
    title: String,
    summary: String,
    icon: ImageVector,
    chips: List<String> = emptyList(),
    footer: String? = null
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.58f),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            if (chips.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chips.forEach { chip ->
                        DetailChip(chip)
                    }
                }
            }
            footer?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                )
            }
        }
    }
}

@Composable
private fun ScoreHeroCard(
    score: Int,
    title: String,
    summary: String,
    chips: List<String>
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "$score%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chips.forEach { chip ->
                    DetailChip(chip)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T> PagerSection(
    items: List<T>,
    titleFor: (T) -> String,
    pageContent: @Composable (T) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { items.size })
    val coroutineScope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEachIndexed { index, item ->
                FilterChip(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    label = {
                        Text(
                            text = titleFor(item),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            pageContent(items[page])
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            items.indices.forEach { index ->
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

@Composable
private fun ScreenSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun DetailSectionCard(
    title: String,
    embedded: Boolean = false,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (embedded) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = if (embedded) 0.16f else 0.24f)
        ),
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}

@Composable
private fun ExistingTestCard(
    test: ExistingTest,
    embedded: Boolean = false
) {
    DetailSectionCard(title = test.name, embedded = embedded) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailChip("Type: ${test.type}")
                test.path?.let { DetailChip(it) }
            }
            if (test.covers.isNotEmpty()) {
                InfoSurface("Covers", test.covers, MaterialTheme.colorScheme.primary)
            }
            if (test.gaps.isNotEmpty()) {
                InfoSurface("Gaps", test.gaps, MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun MissingTestAreaCard(area: MissingTestArea) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = area.area,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
                SeverityBadge(severity = area.priority)
            }
            if (area.suggestedTests.isNotEmpty()) {
                HighlightBulletList(
                    items = area.suggestedTests,
                    accent = MaterialTheme.colorScheme.onErrorContainer,
                    textColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun DispatcherCard(
    dispatcher: Dispatcher,
    embedded: Boolean = false
) {
    DetailSectionCard(title = dispatcher.name, embedded = embedded) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = dispatcher.usage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (dispatcher.relatedFiles.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dispatcher.relatedFiles.forEach { file ->
                        FilePathChip(path = file)
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowInfoCard(
    flow: FlowInfo,
    embedded: Boolean = false
) {
    DetailSectionCard(title = flow.name, embedded = embedded) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailChip(flow.type)
                flow.owner?.let { DetailChip("Owner: $it") }
            }
            flow.purpose?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (flow.risks.isNotEmpty()) {
                InfoSurface("Risks", flow.risks, MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun UiScreenCard(
    screen: Screen,
    embedded: Boolean = false
) {
    DetailSectionCard(title = screen.name, embedded = embedded) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                screen.route?.let { DetailChip("Route: $it") }
                screen.stateSource?.let { DetailChip("State: $it") }
                screen.uiState?.let { DetailChip(it) }
            }
            if (screen.reusableComponents.isNotEmpty()) {
                FeatureLikeTagCloud(screen.reusableComponents)
            }
            if (screen.userActions.isNotEmpty()) {
                InfoSurface("User Actions", screen.userActions, MaterialTheme.colorScheme.primary)
            }
            if (screen.risks.isNotEmpty()) {
                InfoSurface("Risks", screen.risks, MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun StateManagementCard(stateManagement: StateManagement) {
    DetailSectionCard(title = "State Management") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            stateManagement.pattern?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (stateManagement.stateClasses.isNotEmpty()) {
                FeatureLikeTagCloud(stateManagement.stateClasses)
            }
            if (stateManagement.sideEffectHandling.isNotEmpty()) {
                InfoSurface(
                    title = "Side Effects",
                    items = stateManagement.sideEffectHandling,
                    accent = MaterialTheme.colorScheme.secondary
                )
            }
            if (stateManagement.risks.isNotEmpty()) {
                InfoSurface(
                    title = "Risks",
                    items = stateManagement.risks,
                    accent = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun RiskInsightCard(risk: Risk) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
private fun InfoSurface(
    title: String,
    items: List<String>,
    accent: Color
) {
    Surface(
        color = accent.copy(alpha = 0.08f),
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
private fun HighlightBulletList(
    items: List<String>,
    accent: Color,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ChecklistList(items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

@Composable
private fun FeatureLikeTagCloud(items: List<String>) {
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
private fun DetailChip(text: String) {
    Surface(
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
