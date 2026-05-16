**User:**

<task>Command 'review' (see below for command content) Git commit 'acfb3b608d6f8eed67dceddbd0c7fd0cb831ec2c' (see below for commit info)
</task>

<git_commit hash="acfb3b608d6f8eed67dceddbd0c7fd0cb831ec2c">
Commit: acfb3b6 (acfb3b608d6f8eed67dceddbd0c7fd0cb831ec2c)
Author: Andrii
Date: Fri May 15 21:50:35 2026 +0100

Message: Update UI components


Files Changed:
.../vero/repolens/ui/screens/ActionItemsScreen.kt  | 441 +++++++++++++--------
 .../com/vero/repolens/ui/screens/OverviewScreen.kt |  33 +-
 .../repolens/ui/screens/RecommendationsScreen.kt   | 331 ++++++++++------
 .../com/vero/repolens/ui/screens/RisksScreen.kt    | 348 ++++++++++++----
 .../com/vero/repolens/ui/screens/SearchScreen.kt   | 422 ++++++++++++--------
 5 files changed, 1023 insertions(+), 552 deletions(-)

Full Changes:

diff --git a/app/src/main/java/com/vero/repolens/ui/screens/ActionItemsScreen.kt b/app/src/main/java/com/vero/repolens/ui/screens/ActionItemsScreen.kt
index d6b59eb..496f0f0 100644
--- a/app/src/main/java/com/vero/repolens/ui/screens/ActionItemsScreen.kt
+++ b/app/src/main/java/com/vero/repolens/ui/screens/ActionItemsScreen.kt
@@ -1,21 +1,66 @@
 package com.vero.repolens.ui.screens
 
-import androidx.compose.foundation.layout.*
+import androidx.compose.foundation.BorderStroke
+import androidx.compose.foundation.layout.Arrangement
+import androidx.compose.foundation.layout.Box
+import androidx.compose.foundation.layout.Column
+import androidx.compose.foundation.layout.PaddingValues
+import androidx.compose.foundation.layout.Row
+import androidx.compose.foundation.layout.Spacer
+import androidx.compose.foundation.layout.fillMaxSize
+import androidx.compose.foundation.layout.fillMaxWidth
+import androidx.compose.foundation.layout.height
+import androidx.compose.foundation.layout.padding
+import androidx.compose.foundation.layout.size
+import androidx.compose.foundation.layout.width
+import androidx.compose.foundation.shape.RoundedCornerShape
 import androidx.compose.foundation.lazy.LazyColumn
 import androidx.compose.foundation.lazy.LazyRow
 import androidx.compose.foundation.lazy.items
 import androidx.compose.material.icons.Icons
-import androidx.compose.material.icons.filled.*
-import androidx.compose.material3.*
-import androidx.compose.runtime.*
+import androidx.compose.material.icons.automirrored.filled.ArrowBack
+import androidx.compose.material.icons.filled.Add
+import androidx.compose.material.icons.filled.CheckBox
+import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
+import androidx.compose.material.icons.filled.FilterAltOff
+import androidx.compose.material.icons.filled.HourglassEmpty
+import androidx.compose.material.icons.filled.TaskAlt
+import androidx.compose.material3.Button
+import androidx.compose.material3.Divider
+import androidx.compose.material3.ExperimentalMaterial3Api
+import androidx.compose.material3.ExposedDropdownMenuBox
+import androidx.compose.material3.ExposedDropdownMenuDefaults
+import androidx.compose.material3.DropdownMenuItem
+import androidx.compose.material3.FilterChip
+import androidx.compose.material3.FloatingActionButton
+import androidx.compose.material3.Icon
+import androidx.compose.material3.IconButton
+import androidx.compose.material3.MaterialTheme
+import androidx.compose.material3.ModalBottomSheet
+import androidx.compose.material3.OutlinedTextField
+import androidx.compose.material3.Scaffold
+import androidx.compose.material3.Surface
+import androidx.compose.material3.Text
+import androidx.compose.material3.TextButton
+import androidx.compose.material3.TopAppBar
+import androidx.compose.material3.rememberModalBottomSheetState
+import androidx.compose.runtime.Composable
+import androidx.compose.runtime.collectAsState
+import androidx.compose.runtime.getValue
+import androidx.compose.runtime.mutableStateOf
+import androidx.compose.runtime.remember
+import androidx.compose.runtime.setValue
 import androidx.compose.ui.Alignment
 import androidx.compose.ui.Modifier
 import androidx.compose.ui.text.font.FontWeight
+import androidx.compose.ui.text.style.TextAlign
 import androidx.compose.ui.unit.dp
 import androidx.hilt.navigation.compose.hiltViewModel
-import com.vero.repolens.data.models.*
+import com.vero.repolens.data.models.ActionCategory
+import com.vero.repolens.data.models.ActionItem
+import com.vero.repolens.data.models.ActionStatus
+import com.vero.repolens.data.models.Priority
 import com.vero.repolens.ui.components.ActionItemCard
-import com.vero.repolens.ui.components.LoadingState
 import com.vero.repolens.viewmodel.ActionItemsViewModel
 
 @OptIn(ExperimentalMaterial3Api::class)
@@ -31,7 +76,7 @@ fun ActionItemsScreen(
     val inProgressCount by viewModel.inProgressCount.collectAsState()
     val doneCount by viewModel.doneCount.collectAsState()
 
-    var showAddDialog by remember { mutableStateOf(false) }
+    var showEditorSheet by remember { mutableStateOf(false) }

[...1595 lines omitted...]

-                                contentDescription = null,
-                                modifier = Modifier.size(18.dp)
-                            )
-                        }
-                    )
+
+            if (isLoading) {
+                LoadingState()
+            } else if (searchResults.isEmpty() && searchQuery.isNotEmpty()) {
+                EmptySearchState()
+            } else {
+                LazyColumn(
+                    verticalArrangement = Arrangement.spacedBy(10.dp),
+                    contentPadding = PaddingValues(16.dp)
+                ) {
+                    item {
+                        Text(
+                            text = "${searchResults.size} results",
+                            style = MaterialTheme.typography.labelLarge,
+                            color = MaterialTheme.colorScheme.onSurfaceVariant
+                        )
+                    }
+
+                    items(searchResults) { item ->
+                        SearchResultCard(
+                            item = item,
+                            onClick = { onItemClick(item) }
+                        )
+                    }
                 }
             }
+        }
+    }
+}
 
-            // Severity Filters
-            if (availableSeverities.isNotEmpty()) {
+@Composable
+private fun SearchFieldCard(
+    query: String,
+    onQueryChange: (String) -> Unit
+) {
+    Card(
+        modifier = Modifier
+            .fillMaxWidth()
+            .padding(horizontal = 16.dp, vertical = 16.dp),
+        colors = CardDefaults.cardColors(
+            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
+        ),
+        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.26f)),
+        shape = RoundedCornerShape(24.dp)
+    ) {
+        OutlinedTextField(
+            value = query,
+            onValueChange = onQueryChange,
+            modifier = Modifier
+                .fillMaxWidth()
+                .padding(12.dp),
+            placeholder = { Text("Search modules, features, risks...") },
+            leadingIcon = {
+                Icon(Icons.Default.Search, contentDescription = null)
+            },
+            trailingIcon = {
+                if (query.isNotEmpty()) {
+                    IconButton(onClick = { onQueryChange("") }) {
+                        Icon(Icons.Default.Clear, contentDescription = "Clear")
+                    }
+                }
+            },
+            singleLine = true
+        )
+    }
+}
+
+@Composable
+private fun FilterGroupCard(
+    activeFilter: SearchFilter,
+    availableSeverities: List<String>,
+    onTypeToggle: (SearchItemType) -> Unit,
+    onSeverityToggle: (String) -> Unit
+) {
+    Card(
+        modifier = Modifier
+            .fillMaxWidth()
+            .padding(horizontal = 16.dp),
+        colors = CardDefaults.cardColors(
+            containerColor = MaterialTheme.colorScheme.surface
+        ),
+        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
+        shape = RoundedCornerShape(24.dp)
+    ) {
+        Column(
+            modifier = Modifier
+                .fillMaxWidth()
+                .padding(16.dp),
+            verticalArrangement = Arrangement.spacedBy(16.dp)
+        ) {
+            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                 Text(
-                    text = "Filter by Severity",
+                    text = "Filter by Type",
                     style = MaterialTheme.typography.labelLarge,
-                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
+                    color = MaterialTheme.colorScheme.onSurfaceVariant
                 )
-                LazyRow(
+                FlowRow(
                     horizontalArrangement = Arrangement.spacedBy(8.dp),
-                    contentPadding = PaddingValues(horizontal = 16.dp),
-                    modifier = Modifier.padding(bottom = 8.dp)
+                    verticalArrangement = Arrangement.spacedBy(8.dp)
                 ) {
-                    items(availableSeverities.toList()) { severity ->
+                    SearchItemType.values().forEach { type ->
                         FilterChip(
-                            selected = activeFilter.severities.contains(severity),
-                            onClick = { viewModel.toggleSeverityFilter(severity) },
-                            label = { Text(severity) }
+                            selected = activeFilter.types.contains(type),
+                            onClick = { onTypeToggle(type) },
+                            label = { Text(type.name.replace('_', ' ')) },
+                            leadingIcon = {
+                                Icon(
+                                    imageVector = getIconForType(type),
+                                    contentDescription = null,
+                                    modifier = Modifier.size(18.dp)
+                                )
+                            }
                         )
                     }
                 }
             }
 
-            Divider(modifier = Modifier.padding(vertical = 8.dp))
-
-            // Results
-            if (isLoading) {
-                LoadingState()
-            } else {
-                if (searchResults.isEmpty() && searchQuery.isNotEmpty()) {
-                    EmptySearchState()
-                } else {
-                    LazyColumn(
-                        verticalArrangement = Arrangement.spacedBy(8.dp),
-                        contentPadding = PaddingValues(16.dp)
-                    ) {
-                        item {
-                            Text(
-                                text = "${searchResults.size} results",
-                                style = MaterialTheme.typography.labelMedium,
-                                color = MaterialTheme.colorScheme.onSurfaceVariant
-                            )
-                            Spacer(modifier = Modifier.height(8.dp))
-                        }
-
-                        items(searchResults) { item ->
-                            SearchResultCard(
-                                item = item,
-                                onClick = { onItemClick(item) }
+            if (availableSeverities.isNotEmpty()) {
+                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
+                    Text(
+                        text = "Filter by Severity",
+                        style = MaterialTheme.typography.labelLarge,
+                        color = MaterialTheme.colorScheme.onSurfaceVariant
+                    )
+                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
+                        items(availableSeverities) { severity ->
+                            FilterChip(
+                                selected = activeFilter.severities.contains(severity),
+                                onClick = { onSeverityToggle(severity) },
+                                label = { Text(severity) }
                             )
                         }
                     }
@@ -154,31 +253,6 @@ fun SearchScreen(
     }
 }
 
-@Composable
-private fun SearchBar(
-    query: String,
-    onQueryChange: (String) -> Unit,
-    modifier: Modifier = Modifier
-) {
-    OutlinedTextField(
-        value = query,
-        onValueChange = onQueryChange,
-        modifier = modifier,
-        placeholder = { Text("Search modules, features, risks...") },
-        leadingIcon = {
-            Icon(Icons.Default.Search, contentDescription = null)
-        },
-        trailingIcon = {
-            if (query.isNotEmpty()) {
-                IconButton(onClick = { onQueryChange("") }) {
-                    Icon(Icons.Default.Clear, contentDescription = "Clear")
-                }
-            }
-        },
-        singleLine = true
-    )
-}
-
 @Composable
 private fun SearchResultCard(
     item: SearchableItem,
@@ -187,55 +261,68 @@ private fun SearchResultCard(
     Card(
         modifier = Modifier
             .fillMaxWidth()
-            .clickable(onClick = onClick)
+            .clickable(onClick = onClick),
+        colors = CardDefaults.cardColors(
+            containerColor = MaterialTheme.colorScheme.surface
+        ),
+        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
+        shape = MaterialTheme.shapes.extraLarge
     ) {
         Row(
             modifier = Modifier
                 .fillMaxWidth()
-                .padding(16.dp),
+                .padding(18.dp),
             verticalAlignment = Alignment.CenterVertically
         ) {
-            Icon(
-                imageVector = getIconForType(item.type),
-                contentDescription = null,
-                modifier = Modifier.size(40.dp),
-                tint = MaterialTheme.colorScheme.primary
-            )
-            
-            Spacer(modifier = Modifier.width(16.dp))
-            
-            Column(modifier = Modifier.weight(1f)) {
+            Surface(
+                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
+                shape = MaterialTheme.shapes.large
+            ) {
+                Icon(
+                    imageVector = getIconForType(item.type),
+                    contentDescription = null,
+                    modifier = Modifier.padding(10.dp),
+                    tint = MaterialTheme.colorScheme.primary
+                )
+            }
+
+            Spacer(modifier = Modifier.width(14.dp))
+
+            Column(
+                modifier = Modifier.weight(1f),
+                verticalArrangement = Arrangement.spacedBy(6.dp)
+            ) {
                 Text(
                     text = item.title,
-                    style = MaterialTheme.typography.titleMedium,
-                    fontWeight = FontWeight.Bold
+                    style = MaterialTheme.typography.titleLarge,
+                    fontWeight = FontWeight.SemiBold,
+                    maxLines = 1,
+                    overflow = TextOverflow.Ellipsis
                 )
                 Text(
                     text = item.subtitle,
                     style = MaterialTheme.typography.bodyMedium,
                     color = MaterialTheme.colorScheme.onSurfaceVariant,
-                    maxLines = 2
+                    maxLines = 2,
+                    overflow = TextOverflow.Ellipsis
                 )
-                Spacer(modifier = Modifier.height(4.dp))
-                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
-                    AssistChip(
-                        onClick = { },
-                        label = { Text(item.type.name, style = MaterialTheme.typography.labelSmall) }
-                    )
-                    if (item.severity != null) {
-                        AssistChip(
-                            onClick = { },
-                            label = { Text(item.severity, style = MaterialTheme.typography.labelSmall) },
-                            colors = AssistChipDefaults.assistChipColors(
-                                containerColor = getSeverityColor(item.severity)
-                            )
+                FlowRow(
+                    horizontalArrangement = Arrangement.spacedBy(8.dp),
+                    verticalArrangement = Arrangement.spacedBy(8.dp)
+                ) {
+                    SearchMetaChip(item.type.name.replace('_', ' '))
+                    item.severity?.let {
+                        SearchMetaChip(
+                            text = it,
+                            containerColor = getSeverityColor(it).copy(alpha = 0.22f),
+                            contentColor = getSeverityColor(it)
                         )
                     }
                 }
             }
-            
+
             Icon(
-                imageVector = Icons.Default.ChevronRight,
+                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                 contentDescription = null,
                 tint = MaterialTheme.colorScheme.onSurfaceVariant
             )
@@ -243,33 +330,52 @@ private fun SearchResultCard(
     }
 }
 
+@Composable
+private fun SearchMetaChip(
+    text: String,
+    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
+    contentColor: Color = MaterialTheme.colorScheme.onSurface
+) {
+    Surface(
+        color = containerColor,
+        shape = MaterialTheme.shapes.large
+    ) {
+        Text(
+            text = text,
+            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
+            style = MaterialTheme.typography.labelLarge,
+            color = contentColor
+        )
+    }
+}
+
 @Composable
 private fun EmptySearchState() {
-    Box(
-        modifier = Modifier.fillMaxSize(),
-        contentAlignment = Alignment.Center
+    Column(
+        modifier = Modifier
+            .fillMaxSize()
+            .padding(24.dp),
+        horizontalAlignment = Alignment.CenterHorizontally,
+        verticalArrangement = Arrangement.Center
     ) {
-        Column(
-            horizontalAlignment = Alignment.CenterHorizontally,
-            verticalArrangement = Arrangement.spacedBy(8.dp)
-        ) {
-            Icon(
-                imageVector = Icons.Default.SearchOff,
-                contentDescription = null,
-                modifier = Modifier.size(64.dp),
-                tint = MaterialTheme.colorScheme.onSurfaceVariant
-            )
-            Text(
-                text = "No results found",
-                style = MaterialTheme.typography.titleMedium,
-                color = MaterialTheme.colorScheme.onSurfaceVariant
-            )
-            Text(
-                text = "Try adjusting your search or filters",
-                style = MaterialTheme.typography.bodyMedium,
-                color = MaterialTheme.colorScheme.onSurfaceVariant
-            )
-        }
+        Icon(
+            imageVector = Icons.Default.SearchOff,
+            contentDescription = null,
+            modifier = Modifier.size(64.dp),
+            tint = MaterialTheme.colorScheme.onSurfaceVariant
+        )
+        Spacer(modifier = Modifier.height(12.dp))
+        Text(
+            text = "No results found",
+            style = MaterialTheme.typography.titleLarge,
+            color = MaterialTheme.colorScheme.onSurfaceVariant
+        )
+        Spacer(modifier = Modifier.height(6.dp))
+        Text(
+            text = "Try adjusting your search or filters",
+            style = MaterialTheme.typography.bodyMedium,
+            color = MaterialTheme.colorScheme.onSurfaceVariant
+        )
     }
 }
 
@@ -284,11 +390,11 @@ private fun getIconForType(type: SearchItemType) = when (type) {
 
 @Composable
 private fun getSeverityColor(severity: String) = when (severity.lowercase()) {
-    "critical" -> MaterialTheme.colorScheme.errorContainer
-    "high" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
-    "medium" -> MaterialTheme.colorScheme.tertiaryContainer
-    "low" -> MaterialTheme.colorScheme.secondaryContainer
-    else -> MaterialTheme.colorScheme.surfaceVariant
+    "critical" -> MaterialTheme.colorScheme.error
+    "high" -> MaterialTheme.colorScheme.error.copy(alpha = 0.86f)
+    "medium" -> MaterialTheme.colorScheme.tertiary
+    "low" -> MaterialTheme.colorScheme.secondary
+    else -> MaterialTheme.colorScheme.onSurfaceVariant
 }
 
 // Made with Bob
</git_commit>


<environment_details>
# VSCode Visible Files
app\src\main\java\com\vero\repolens\ui\theme\Theme.kt

# VSCode Open Tabs
app/src/main/java/com/vero/repolens/ui/screens/ModulesScreen.kt,app/src/main/java/com/vero/repolens/ui/screens/ModuleDetailScreen.kt,app/src/main/java/com/vero/repolens/ui/screens/FeaturesScreen.kt,app/src/main/java/com/vero/repolens/ui/screens/FeatureDetailScreen.kt,app/src/main/java/com/vero/repolens/data/models/SearchModels.kt,app/src/main/java/com/vero/repolens/ui/screens/SearchScreen.kt,app/src/main/java/com/vero/repolens/viewmodel/SearchViewModel.kt,app/src/main/java/com/vero/repolens/ui/screens/ActionItemsScreen.kt,app/src/main/java/com/vero/repolens/data/export/ExportService.kt,app/src/main/java/com/vero/repolens/data/export/MarkdownGenerator.kt,app/src/main/java/com/vero/repolens/data/export/JsonExporter.kt,app/src/main/java/com/vero/repolens/data/export/PdfGenerator.kt,app/src/main/java/com/vero/repolens/viewmodel/ExportViewModel.kt,app/src/main/java/com/vero/repolens/ui/components/ExportDialog.kt,app/src/main/AndroidManifest.xml,app/src/main/res/xml/file_paths.xml,app/src/main/java/com/vero/repolens/ui/screens/PerformanceScreen.kt,app/src/main/java/com/vero/repolens/data/engine/RecommendationEngine.kt,app/src/main/java/com/vero/repolens/viewmodel/RecommendationsViewModel.kt,app/src/main/java/com/vero/repolens/ui/components/RecommendationCard.kt

# Current Time
Current time in ISO 8601 UTC format: 2026-05-15T20:50:40.253Z
User time zone: Europe/London, UTC+1:00

# Current Cost
$0.00

# Review Exclusions
The following file patterns are excluded from code reviews:
- .vscode/**

# Current Mode
<slug>advanced</slug>
<name>🛠️ Advanced</name>


# Current Workspace Directory (c:/Users/andri/AndroidStudioProjects/Repolens) Files
.gitignore
build.gradle.kts
gradle.properties
gradlew
gradlew.bat
README.md
settings.gradle.kts
.idea/
.kotlin/
app/
app/.gitignore
app/build.gradle.kts
app/proguard-rules.pro
app/src/
app/src/androidTest/
app/src/androidTest/java/
app/src/androidTest/java/com/
app/src/androidTest/java/com/vero/
app/src/androidTest/java/com/vero/repolens/
app/src/androidTest/java/com/vero/repolens/ExampleInstrumentedTest.kt
app/src/main/
app/src/main/AndroidManifest.xml
app/src/main/assets/
app/src/main/assets/home_assistant_android_analysis_report.json
app/src/main/assets/ivy_wallet_analysis_report.json
app/src/main/assets/repolens_sample_report.json
app/src/main/java/
app/src/main/java/com/
app/src/main/java/com/vero/
app/src/main/java/com/vero/repolens/
app/src/main/java/com/vero/repolens/MainActivity.kt
app/src/main/java/com/vero/repolens/RepoLensApplication.kt
app/src/main/java/com/vero/repolens/data/
app/src/main/java/com/vero/repolens/data/engine/
app/src/main/java/com/vero/repolens/data/engine/RecommendationEngine.kt
app/src/main/java/com/vero/repolens/data/export/
app/src/main/java/com/vero/repolens/data/export/ExportService.kt
app/src/main/java/com/vero/repolens/data/export/JsonExporter.kt
app/src/main/java/com/vero/repolens/data/export/MarkdownGenerator.kt
app/src/main/java/com/vero/repolens/data/export/PdfGenerator.kt
app/src/main/java/com/vero/repolens/data/local/
app/src/main/java/com/vero/repolens/data/local/ActionItemDao.kt
app/src/main/java/com/vero/repolens/data/local/AppDatabase.kt
app/src/main/java/com/vero/repolens/data/models/
app/src/main/java/com/vero/repolens/data/models/ActionItem.kt
app/src/main/java/com/vero/repolens/data/models/RepoLensReport.kt
app/src/main/java/com/vero/repolens/data/models/ReportSource.kt
app/src/main/java/com/vero/repolens/data/models/SearchModels.kt
app/src/main/java/com/vero/repolens/data/repository/
app/src/main/java/com/vero/repolens/data/repository/RepoLensRepository.kt
app/src/main/java/com/vero/repolens/di/
app/src/main/java/com/vero/repolens/di/DatabaseModule.kt
app/src/main/java/com/vero/repolens/ui/
app/src/main/java/com/vero/repolens/ui/components/
app/src/main/java/com/vero/repolens/ui/components/ActionItemCard.kt
app/src/main/java/com/vero/repolens/ui/components/ArchitectureDiagram.kt
app/src/main/java/com/vero/repolens/ui/components/CommonComponents.kt
app/src/main/java/com/vero/repolens/ui/components/ExportDialog.kt
app/src/main/java/com/vero/repolens/ui/components/RecommendationCard.kt
app/src/main/java/com/vero/repolens/ui/navigation/
app/src/main/java/com/vero/repolens/ui/navigation/NavGraph.kt
app/src/main/java/com/vero/repolens/ui/navigation/NavRoutes.kt
app/src/main/java/com/vero/repolens/ui/screens/
app/src/main/java/com/vero/repolens/ui/screens/ActionItemsScreen.kt
app/src/main/java/com/vero/repolens/ui/screens/ArchitectureScreen.kt
app/src/main/java/com/vero/repolens/ui/screens/FeatureDetailScreen.kt
app/src/main/java/com/vero/repolens/ui/screens/FeaturesScreen.kt
app/src/main/java/com/vero/repolens/ui/screens/IntroScreen.kt
app/src/main/java/com/vero/repolens/ui/screens/ModuleDetailScreen.kt
app/src/main/java/com/vero/repolens/ui/screens/ModulesScreen.kt
app/src/main/java/com/vero/repolens/ui/screens/OtherScreens.kt
app/src/main/java/com/vero/repolens/ui/screens/OverviewScreen.kt
app/src/main/java/com/vero/repolens/ui/screens/PerformanceScreen.kt
app/src/main/java/com/vero/repolens/ui/screens/RecommendationsScreen.kt
app/src/main/java/com/vero/repolens/ui/screens/RisksScreen.kt
app/src/main/java/com/vero/repolens/ui/screens/SearchScreen.kt
app/src/main/java/com/vero/repolens/ui/theme/
app/src/main/java/com/vero/repolens/ui/theme/Color.kt
app/src/main/java/com/vero/repolens/ui/theme/Theme.kt
app/src/main/java/com/vero/repolens/ui/theme/Type.kt
app/src/main/java/com/vero/repolens/viewmodel/
app/src/main/java/com/vero/repolens/viewmodel/ActionItemsViewModel.kt
app/src/main/java/com/vero/repolens/viewmodel/ExportViewModel.kt
app/src/main/java/com/vero/repolens/viewmodel/IntroViewModel.kt
app/src/main/java/com/vero/repolens/viewmodel/RecommendationsViewModel.kt
app/src/main/java/com/vero/repolens/viewmodel/RepoLensViewModel.kt
app/src/main/java/com/vero/repolens/viewmodel/SearchViewModel.kt
app/src/main/res/
app/src/main/res/drawable/
app/src/main/res/drawable/ic_launcher_background.xml
app/src/main/res/drawable/ic_launcher_foreground.xml
app/src/main/res/mipmap-anydpi/
app/src/main/res/mipmap-anydpi/ic_launcher_round.xml
app/src/main/res/mipmap-anydpi/ic_launcher.xml
app/src/main/res/mipmap-hdpi/
app/src/main/res/mipmap-hdpi/ic_launcher_round.webp
app/src/main/res/mipmap-hdpi/ic_launcher.webp
app/src/main/res/mipmap-mdpi/
app/src/main/res/mipmap-mdpi/ic_launcher_round.webp
app/src/main/res/mipmap-mdpi/ic_launcher.webp
app/src/main/res/mipmap-xhdpi/
app/src/main/res/mipmap-xhdpi/ic_launcher_round.webp
app/src/main/res/mipmap-xhdpi/ic_launcher.webp
app/src/main/res/mipmap-xxhdpi/
app/src/main/res/mipmap-xxhdpi/ic_launcher_round.webp
app/src/main/res/mipmap-xxhdpi/ic_launcher.webp
app/src/main/res/mipmap-xxxhdpi/
app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp
app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp
app/src/main/res/values/
app/src/main/res/values/colors.xml
app/src/main/res/values/strings.xml
app/src/main/res/values/themes.xml
app/src/main/res/xml/
app/src/main/res/xml/backup_rules.xml
app/src/main/res/xml/data_extraction_rules.xml
app/src/main/res/xml/file_paths.xml
app/src/test/
app/src/test/java/
app/src/test/java/com/
app/src/test/java/com/vero/
app/src/test/java/com/vero/repolens/
app/src/test/java/com/vero/repolens/ExampleUnitTest.kt
gradle/
gradle/gradle-daemon-jvm.properties
gradle/libs.versions.toml
gradle/wrapper/
gradle/wrapper/gradle-wrapper.jar
gradle/wrapper/gradle-wrapper.properties
You have not created a todo list yet. Create one with `update_todo_list` if your task is complicated or involves multiple steps.
</environment_details>

---

**Assistant:**

I'll review the git commit changes following the review workflow. Let me start by switching to advanced mode and then examining the changes.

<switch_mode>
<mode_slug>advanced</mode_slug>
<reason>Starting code review workflow</reason>
</switch_mode>

---

**User:**

[switch_mode to 'advanced' because: Starting code review workflow] Result:
Already in 🛠️ Advanced mode.
<environment_details>
# VSCode Visible Files
app\src\main\java\com\vero\repolens\ui\theme\Theme.kt

# VSCode Open Tabs
app/src/main/java/com/vero/repolens/ui/screens/ModulesScreen.kt,app/src/main/java/com/vero/repolens/ui/screens/ModuleDetailScreen.kt,app/src/main/java/com/vero/repolens/ui/screens/FeaturesScreen.kt,app/src/main/java/com/vero/repolens/ui/screens/FeatureDetailScreen.kt,app/src/main/java/com/vero/repolens/data/models/SearchModels.kt,app/src/main/java/com/vero/repolens/ui/screens/SearchScreen.kt,app/src/main/java/com/vero/repolens/viewmodel/SearchViewModel.kt,app/src/main/java/com/vero/repolens/ui/screens/ActionItemsScreen.kt,app/src/main/java/com/vero/repolens/data/export/ExportService.kt,app/src/main/java/com/vero/repolens/data/export/MarkdownGenerator.kt,app/src/main/java/com/vero/repolens/data/export/JsonExporter.kt,app/src/main/java/com/vero/repolens/data/export/PdfGenerator.kt,app/src/main/java/com/vero/repolens/viewmodel/ExportViewModel.kt,app/src/main/java/com/vero/repolens/ui/components/ExportDialog.kt,app/src/main/AndroidManifest.xml,app/src/main/res/xml/file_paths.xml,app/src/main/java/com/vero/repolens/ui/screens/PerformanceScreen.kt,app/src/main/java/com/vero/repolens/data/engine/RecommendationEngine.kt,app/src/main/java/com/vero/repolens/viewmodel/RecommendationsViewModel.kt,app/src/main/java/com/vero/repolens/ui/components/RecommendationCard.kt

# Current Time
Current time in ISO 8601 UTC format: 2026-05-15T20:50:44.907Z
User time zone: Europe/London, UTC+1:00

# Current Cost
$0.06

# Review Exclusions
The following file patterns are excluded from code reviews:
- .vscode/**

# Current Mode
<slug>advanced</slug>
<name>🛠️ Advanced</name>
You have not created a todo list yet. Create one with `update_todo_list` if your task is complicated or involves multiple steps.
</environment_details>

---

**Assistant:**

Now let me read the changed files to understand the full context of the modifications.

<read_file>
<args>
  <file>
    <path>app/src/main/java/com/vero/repolens/ui/screens/ActionItemsScreen.kt</path>
  </file>
  <file>
    <path>app/src/main/java/com/vero/repolens/ui/screens/OverviewScreen.kt</path>
  </file>
  <file>
    <path>app/src/main/java/com/vero/repolens/ui/screens/RecommendationsScreen.kt</path>
  </file>
  <file>
    <path>app/src/main/java/com/vero/repolens/ui/screens/RisksScreen.kt</path>
  </file>
  <file>
    <path>app/src/main/java/com/vero/repolens/ui/screens/SearchScreen.kt</path>
  </file>
</args>
</read_file>

---

**User:**

[read_file for 5 files] Result:
<files>
<file><path>app/src/main/java/com/vero/repolens/ui/screens/ActionItemsScreen.kt</path>
<content lines="1-479">
  1 | package com.vero.repolens.ui.screens
  2 | 
  3 | import androidx.compose.foundation.BorderStroke
  4 | import androidx.compose.foundation.layout.Arrangement
  5 | import androidx.compose.foundation.layout.Box
  6 | import androidx.compose.foundation.layout.Column
  7 | import androidx.compose.foundation.layout.PaddingValues
  8 | import androidx.compose.foundation.layout.Row
  9 | import androidx.compose.foundation.layout.Spacer
 10 | import androidx.compose.foundation.layout.fillMaxSize
 11 | import androidx.compose.foundation.layout.fillMaxWidth
 12 | import androidx.compose.foundation.layout.height
 13 | import androidx.compose.foundation.layout.padding
 14 | import androidx.compose.foundation.layout.size
 15 | import androidx.compose.foundation.layout.width
 16 | import androidx.compose.foundation.shape.RoundedCornerShape
 17 | import androidx.compose.foundation.lazy.LazyColumn
 18 | import androidx.compose.foundation.lazy.LazyRow
 19 | import androidx.compose.foundation.lazy.items
 20 | import androidx.compose.material.icons.Icons
 21 | import androidx.compose.material.icons.automirrored.filled.ArrowBack
 22 | import androidx.compose.material.icons.filled.Add
 23 | import androidx.compose.material.icons.filled.CheckBox
 24 | import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
 25 | import androidx.compose.material.icons.filled.FilterAltOff
 26 | import androidx.compose.material.icons.filled.HourglassEmpty
 27 | import androidx.compose.material.icons.filled.TaskAlt
 28 | import androidx.compose.material3.Button
 29 | import androidx.compose.material3.Divider
 30 | import androidx.compose.material3.ExperimentalMaterial3Api
 31 | import androidx.compose.material3.ExposedDropdownMenuBox
 32 | import androidx.compose.material3.ExposedDropdownMenuDefaults
 33 | import androidx.compose.material3.DropdownMenuItem
 34 | import androidx.compose.material3.FilterChip
 35 | import androidx.compose.material3.FloatingActionButton
 36 | import androidx.compose.material3.Icon
 37 | import androidx.compose.material3.IconButton
 38 | import androidx.compose.material3.MaterialTheme
 39 | import androidx.compose.material3.ModalBottomSheet
 40 | import androidx.compose.material3.OutlinedTextField
 41 | import androidx.compose.material3.Scaffold
 42 | import androidx.compose.material3.Surface
 43 | import androidx.compose.material3.Text
 44 | import androidx.compose.material3.TextButton
 45 | import androidx.compose.material3.TopAppBar
 46 | import androidx.compose.material3.rememberModalBottomSheetState
 47 | import androidx.compose.runtime.Composable
 48 | import androidx.compose.runtime.collectAsState
 49 | import androidx.compose.runtime.getValue
 50 | import androidx.compose.runtime.mutableStateOf
 51 | import androidx.compose.runtime.remember
 52 | import androidx.compose.runtime.setValue
 53 | import androidx.compose.ui.Alignment
 54 | import androidx.compose.ui.Modifier
 55 | import androidx.compose.ui.text.font.FontWeight
 56 | import androidx.compose.ui.text.style.TextAlign
 57 | import androidx.compose.ui.unit.dp
 58 | import androidx.hilt.navigation.compose.hiltViewModel
 59 | import com.vero.repolens.data.models.ActionCategory
 60 | import com.vero.repolens.data.models.ActionItem
 61 | import com.vero.repolens.data.models.ActionStatus
 62 | import com.vero.repolens.data.models.Priority
 63 | import com.vero.repolens.ui.components.ActionItemCard
 64 | import com.vero.repolens.viewmodel.ActionItemsViewModel
 65 | 
 66 | @OptIn(ExperimentalMaterial3Api::class)
 67 | @Composable
 68 | fun ActionItemsScreen(
 69 |     onNavigateBack: () -> Unit,
 70 |     viewModel: ActionItemsViewModel = hiltViewModel()
 71 | ) {
 72 |     val filteredItems by viewModel.filteredActionItems.collectAsState()
 73 |     val filterStatus by viewModel.filterStatus.collectAsState()
 74 |     val filterPriority by viewModel.filterPriority.collectAsState()
 75 |     val todoCount by viewModel.todoCount.collectAsState()
 76 |     val inProgressCount by viewModel.inProgressCount.collectAsState()
 77 |     val doneCount by viewModel.doneCount.collectAsState()
 78 | 
 79 |     var showEditorSheet by remember { mutableStateOf(false) }
 80 |     var editingItem by remember { mutableStateOf<ActionItem?>(null) }
 81 | 
 82 |     Scaffold(
 83 |         topBar = {
 84 |             TopAppBar(
 85 |                 title = { Text("Action Items") },
 86 |                 navigationIcon = {
 87 |                     IconButton(onClick = onNavigateBack) {
 88 |                         Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
 89 |                     }
 90 |                 },
 91 |                 actions = {
 92 |                     IconButton(onClick = { viewModel.clearFilters() }) {
 93 |                         Icon(Icons.Default.FilterAltOff, contentDescription = "Clear filters")
 94 |                     }
 95 |                 }
 96 |             )
 97 |         },
 98 |         floatingActionButton = {
 99 |             FloatingActionButton(
100 |                 onClick = {
101 |                     editingItem = null
102 |                     showEditorSheet = true
103 |                 }
104 |             ) {
105 |                 Icon(Icons.Default.Add, contentDescription = "Add action item")
106 |             }
107 |         }
108 |     ) { paddingValues ->
109 |         Column(
110 |             modifier = Modifier
111 |                 .fillMaxSize()
112 |                 .padding(paddingValues)
113 |         ) {
114 |             ActionItemsOverviewCard(
115 |                 todoCount = todoCount,
116 |                 inProgressCount = inProgressCount,
117 |                 doneCount = doneCount,
118 |                 filterStatus = filterStatus,
119 |                 onStatusSelected = { selectedStatus ->
120 |                     viewModel.setStatusFilter(
121 |                         if (filterStatus == selectedStatus) null else selectedStatus
122 |                     )
123 |                 }
124 |             )
125 | 
126 |             Text(
127 |                 text = "Filter by Priority",
128 |                 style = MaterialTheme.typography.labelLarge,
129 |                 modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
130 |             )
131 |             LazyRow(
132 |                 horizontalArrangement = Arrangement.spacedBy(8.dp),
133 |                 contentPadding = PaddingValues(horizontal = 16.dp),
134 |                 modifier = Modifier.padding(bottom = 16.dp)
135 |             ) {
136 |                 items(Priority.values()) { priority ->
137 |                     FilterChip(
138 |                         selected = filterPriority == priority,
139 |                         onClick = {
140 |                             viewModel.setPriorityFilter(
141 |                                 if (filterPriority == priority) null else priority
142 |                             )
143 |                         },
144 |                         label = { Text(priority.name) }
145 |                     )
146 |                 }
147 |             }
148 | 
149 |             Divider()
150 | 
151 |             if (filteredItems.isEmpty()) {
152 |                 EmptyActionItemsState(
153 |                     onAddClick = {
154 |                         editingItem = null
155 |                         showEditorSheet = true
156 |                     }
157 |                 )
158 |             } else {
159 |                 LazyColumn(
160 |                     verticalArrangement = Arrangement.spacedBy(8.dp),
161 |                     contentPadding = PaddingValues(16.dp)
162 |                 ) {
163 |                     items(filteredItems, key = { it.id }) { item ->
164 |                         ActionItemCard(
165 |                             actionItem = item,
166 |                             onStatusToggle = { viewModel.toggleActionItemStatus(item) },
167 |                             onEdit = {
168 |                                 editingItem = item
169 |                                 showEditorSheet = true
170 |                             },
171 |                             onDelete = { viewModel.deleteActionItem(item) }
172 |                         )
173 |                     }
174 |                 }
175 |             }
176 |         }
177 |     }
178 | 
179 |     if (showEditorSheet || editingItem != null) {
180 |         ActionItemBottomSheet(
181 |             actionItem = editingItem,
182 |             onDismiss = {
183 |                 showEditorSheet = false
184 |                 editingItem = null
185 |             },
186 |             onSave = { title, description, priority, category ->
187 |                 if (editingItem != null) {
188 |                     viewModel.updateActionItem(
189 |                         editingItem!!.copy(
190 |                             title = title,
191 |                             description = description,
192 |                             priority = priority,
193 |                             category = category
194 |                         )
195 |                     )
196 |                 } else {
197 |                     viewModel.createActionItem(
198 |                         title = title,
199 |                         description = description,
200 |                         priority = priority,
201 |                         category = category
202 |                     )
203 |                 }
204 |                 showEditorSheet = false
205 |                 editingItem = null
206 |             }
207 |         )
208 |     }
209 | }
210 | 
211 | @Composable
212 | private fun ActionItemsOverviewCard(
213 |     todoCount: Int,
214 |     inProgressCount: Int,
215 |     doneCount: Int,
216 |     filterStatus: ActionStatus?,
217 |     onStatusSelected: (ActionStatus) -> Unit
218 | ) {
219 |     androidx.compose.material3.Card(
220 |         modifier = Modifier
221 |             .fillMaxWidth()
222 |             .padding(16.dp),
223 |         shape = RoundedCornerShape(24.dp)
224 |     ) {
225 |         Row(
226 |             modifier = Modifier
227 |                 .fillMaxWidth()
228 |                 .padding(16.dp),
229 |             horizontalArrangement = Arrangement.spacedBy(12.dp)
230 |         ) {
231 |             StatusCountChip(
232 |                 label = "To Do",
233 |                 count = todoCount,
234 |                 icon = Icons.Default.CheckBoxOutlineBlank,
235 |                 isSelected = filterStatus == ActionStatus.TODO,
236 |                 onClick = { onStatusSelected(ActionStatus.TODO) }
237 |             )
238 |             StatusCountChip(
239 |                 label = "In Progress",
240 |                 count = inProgressCount,
241 |                 icon = Icons.Default.HourglassEmpty,
242 |                 isSelected = filterStatus == ActionStatus.IN_PROGRESS,
243 |                 onClick = { onStatusSelected(ActionStatus.IN_PROGRESS) }
244 |             )
245 |             StatusCountChip(
246 |                 label = "Done",
247 |                 count = doneCount,
248 |                 icon = Icons.Default.CheckBox,
249 |                 isSelected = filterStatus == ActionStatus.DONE,
250 |                 onClick = { onStatusSelected(ActionStatus.DONE) }
251 |             )
252 |         }
253 |     }
254 | }
255 | 
256 | @Composable
257 | private fun StatusCountChip(
258 |     label: String,
259 |     count: Int,
260 |     icon: androidx.compose.ui.graphics.vector.ImageVector,
261 |     isSelected: Boolean,
262 |     onClick: () -> Unit
263 | ) {
264 |     Surface(
265 |         modifier = Modifier.weight(1f),
266 |         onClick = onClick,
267 |         shape = RoundedCornerShape(18.dp),
268 |         color = if (isSelected) {
269 |             MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
270 |         } else {
271 |             MaterialTheme.colorScheme.surface.copy(alpha = 0.42f)
272 |         },
273 |         border = BorderStroke(
274 |             1.dp,
275 |             if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.42f)
276 |             else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
277 |         )
278 |     ) {
279 |         Column(
280 |             modifier = Modifier
281 |                 .fillMaxWidth()
282 |                 .padding(horizontal = 10.dp, vertical = 12.dp),
283 |             horizontalAlignment = Alignment.CenterHorizontally,
284 |             verticalArrangement = Arrangement.spacedBy(6.dp)
285 |         ) {
286 |             Icon(
287 |                 icon,
288 |                 contentDescription = null,
289 |                 modifier = Modifier.size(18.dp),
290 |                 tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
291 |             )
292 |             Text(
293 |                 count.toString(),
294 |                 style = MaterialTheme.typography.titleLarge,
295 |                 fontWeight = FontWeight.Bold,
296 |                 color = MaterialTheme.colorScheme.onSurface
297 |             )
298 |             Text(
299 |                 text = label,
300 |                 style = MaterialTheme.typography.labelSmall,
301 |                 color = MaterialTheme.colorScheme.onSurfaceVariant,
302 |                 textAlign = TextAlign.Center
303 |             )
304 |         }
305 |     }
306 | }
307 | 
308 | @Composable
309 | private fun EmptyActionItemsState(onAddClick: () -> Unit) {
310 |     Box(
311 |         modifier = Modifier.fillMaxSize(),
312 |         contentAlignment = Alignment.Center
313 |     ) {
314 |         Column(
315 |             horizontalAlignment = Alignment.CenterHorizontally,
316 |             verticalArrangement = Arrangement.spacedBy(16.dp)
317 |         ) {
318 |             Icon(
319 |                 imageVector = Icons.Default.TaskAlt,
320 |                 contentDescription = null,
321 |                 modifier = Modifier.size(64.dp),
322 |                 tint = MaterialTheme.colorScheme.onSurfaceVariant
323 |             )
324 |             Text(
325 |                 text = "No action items yet",
326 |                 style = MaterialTheme.typography.titleMedium,
327 |                 color = MaterialTheme.colorScheme.onSurfaceVariant
328 |             )
329 |             Text(
330 |                 text = "Create your first action item to get started",
331 |                 style = MaterialTheme.typography.bodyMedium,
332 |                 color = MaterialTheme.colorScheme.onSurfaceVariant
333 |             )
334 |             Button(onClick = onAddClick) {
335 |                 Icon(Icons.Default.Add, contentDescription = null)
336 |                 Spacer(modifier = Modifier.width(8.dp))
337 |                 Text("Add Action Item")
338 |             }
339 |         }
340 |     }
341 | }
342 | 
343 | @OptIn(ExperimentalMaterial3Api::class)
344 | @Composable
345 | private fun ActionItemBottomSheet(
346 |     actionItem: ActionItem?,
347 |     onDismiss: () -> Unit,
348 |     onSave: (String, String, Priority, ActionCategory) -> Unit
349 | ) {
350 |     var title by remember(actionItem?.id) { mutableStateOf(actionItem?.title ?: "") }
351 |     var description by remember(actionItem?.id) { mutableStateOf(actionItem?.description ?: "") }
352 |     var priority by remember(actionItem?.id) { mutableStateOf(actionItem?.priority ?: Priority.MEDIUM) }
353 |     var category by remember(actionItem?.id) { mutableStateOf(actionItem?.category ?: ActionCategory.OTHER) }
354 |     var expandedPriority by remember { mutableStateOf(false) }
355 |     var expandedCategory by remember { mutableStateOf(false) }
356 |     val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
357 | 
358 |     ModalBottomSheet(
359 |         onDismissRequest = onDismiss,
360 |         sheetState = sheetState
361 |     ) {
362 |         Column(
363 |             modifier = Modifier
364 |                 .fillMaxWidth()
365 |                 .padding(horizontal = 20.dp)
366 |                 .padding(bottom = 24.dp),
367 |             verticalArrangement = Arrangement.spacedBy(16.dp)
368 |         ) {
369 |             Text(
370 |                 text = if (actionItem == null) "New Action Item" else "Edit Action Item",
371 |                 style = MaterialTheme.typography.headlineSmall,
372 |                 fontWeight = FontWeight.SemiBold
373 |             )
374 | 
375 |             OutlinedTextField(
376 |                 value = title,
377 |                 onValueChange = { title = it },
378 |                 label = { Text("Title") },
379 |                 modifier = Modifier.fillMaxWidth(),
380 |                 singleLine = true,
381 |                 shape = RoundedCornerShape(18.dp)
382 |             )
383 | 
384 |             OutlinedTextField(
385 |                 value = description,
386 |                 onValueChange = { description = it },
387 |                 label = { Text("Description") },
388 |                 modifier = Modifier.fillMaxWidth(),
389 |                 minLines = 3,
390 |                 maxLines = 5,
391 |                 shape = RoundedCornerShape(18.dp)
392 |             )
393 | 
394 |             ExposedDropdownMenuBox(
395 |                 expanded = expandedPriority,
396 |                 onExpandedChange = { expandedPriority = it }
397 |             ) {
398 |                 OutlinedTextField(
399 |                     value = priority.name,
400 |                     onValueChange = {},
401 |                     readOnly = true,
402 |                     label = { Text("Priority") },
403 |                     trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority) },
404 |                     modifier = Modifier
405 |                         .fillMaxWidth()
406 |                         .menuAnchor(),
407 |                     shape = RoundedCornerShape(18.dp)
408 |                 )
409 |                 ExposedDropdownMenu(
410 |                     expanded = expandedPriority,
411 |                     onDismissRequest = { expandedPriority = false }
412 |                 ) {
413 |                     Priority.values().forEach { value ->
414 |                         DropdownMenuItem(
415 |                             text = { Text(value.name) },
416 |                             onClick = {
417 |                                 priority = value
418 |                                 expandedPriority = false
419 |                             }
420 |                         )
421 |                     }
422 |                 }
423 |             }
424 | 
425 |             ExposedDropdownMenuBox(
426 |                 expanded = expandedCategory,
427 |                 onExpandedChange = { expandedCategory = it }
428 |             ) {
429 |                 OutlinedTextField(
430 |                     value = category.name.replace('_', ' '),
431 |                     onValueChange = {},
432 |                     readOnly = true,
433 |                     label = { Text("Category") },
434 |                     trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
435 |                     modifier = Modifier
436 |                         .fillMaxWidth()
437 |                         .menuAnchor(),
438 |                     shape = RoundedCornerShape(18.dp)
439 |                 )
440 |                 ExposedDropdownMenu(
441 |                     expanded = expandedCategory,
442 |                     onDismissRequest = { expandedCategory = false }
443 |                 ) {
444 |                     ActionCategory.values().forEach { value ->
445 |                         DropdownMenuItem(
446 |                             text = { Text(value.name.replace('_', ' ')) },
447 |                             onClick = {
448 |                                 category = value
449 |                                 expandedCategory = false
450 |                             }
451 |                         )
452 |                     }
453 |                 }
454 |             }
455 | 
456 |             Row(
457 |                 modifier = Modifier.fillMaxWidth(),
458 |                 horizontalArrangement = Arrangement.End
459 |             ) {
460 |                 TextButton(onClick = onDismiss) {
461 |                     Text("Cancel")
462 |                 }
463 |                 Spacer(modifier = Modifier.width(8.dp))
464 |                 TextButton(
465 |                     onClick = {
466 |                         if (title.isNotBlank()) {
467 |                             onSave(title, description, priority, category)
468 |                         }
469 |                     },
470 |                     enabled = title.isNotBlank()
471 |                 ) {
472 |                     Text("Save")
473 |                 }
474 |             }
475 |         }
476 |     }
477 | }
478 | 
479 | // Made with Bob
</content>
</file>
<file><path>app/src/main/java/com/vero/repolens/ui/screens/OverviewScreen.kt</path>
<content lines="1-698">
  1 | package com.vero.repolens.ui.screens
  2 | 
  3 | import androidx.compose.foundation.BorderStroke
  4 | import androidx.compose.foundation.layout.Arrangement
  5 | import androidx.compose.foundation.layout.Box
  6 | import androidx.compose.foundation.layout.Column
  7 | import androidx.compose.foundation.layout.PaddingValues
  8 | import androidx.compose.foundation.layout.Row
  9 | import androidx.compose.foundation.layout.Spacer
 10 | import androidx.compose.foundation.layout.fillMaxSize
 11 | import androidx.compose.foundation.layout.fillMaxWidth
 12 | import androidx.compose.foundation.layout.padding
 13 | import androidx.compose.foundation.layout.size
 14 | import androidx.compose.foundation.layout.width
 15 | import androidx.compose.foundation.lazy.LazyColumn
 16 | import androidx.compose.material.icons.Icons
 17 | import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
 18 | import androidx.compose.material.icons.filled.Architecture
 19 | import androidx.compose.material.icons.filled.AutoAwesome
 20 | import androidx.compose.material.icons.filled.BugReport
 21 | import androidx.compose.material.icons.filled.CheckCircle
 22 | import androidx.compose.material.icons.filled.Code
 23 | import androidx.compose.material.icons.filled.Folder
 24 | import androidx.compose.material.icons.filled.Link
 25 | import androidx.compose.material.icons.filled.PhoneAndroid
 26 | import androidx.compose.material.icons.filled.Science
 27 | import androidx.compose.material.icons.filled.Search
 28 | import androidx.compose.material.icons.filled.Share
 29 | import androidx.compose.material.icons.filled.Smartphone
 30 | import androidx.compose.material.icons.filled.Speed
 31 | import androidx.compose.material.icons.filled.Star
 32 | import androidx.compose.material.icons.filled.Sync
 33 | import androidx.compose.material.icons.filled.Task
 34 | import androidx.compose.material.icons.filled.Warning
 35 | import androidx.compose.material3.AssistChip
 36 | import androidx.compose.material3.AssistChipDefaults
 37 | import androidx.compose.material3.Button
 38 | import androidx.compose.material3.Card
 39 | import androidx.compose.material3.CardDefaults
 40 | import androidx.compose.material3.ExperimentalMaterial3Api
 41 | import androidx.compose.material3.Icon
 42 | import androidx.compose.material3.IconButton
 43 | import androidx.compose.material3.MaterialTheme
 44 | import androidx.compose.material3.Scaffold
 45 | import androidx.compose.material3.Surface
 46 | import androidx.compose.material3.Text
 47 | import androidx.compose.material3.TopAppBar
 48 | import androidx.compose.material3.TopAppBarDefaults
 49 | import androidx.compose.runtime.Composable
 50 | import androidx.compose.runtime.getValue
 51 | import androidx.compose.runtime.mutableStateOf
 52 | import androidx.compose.runtime.remember
 53 | import androidx.compose.runtime.setValue
 54 | import androidx.compose.ui.Alignment
 55 | import androidx.compose.ui.Modifier
 56 | import androidx.compose.ui.graphics.Color
 57 | import androidx.compose.ui.graphics.vector.ImageVector
 58 | import androidx.compose.ui.text.font.FontWeight
 59 | import androidx.compose.ui.text.style.TextOverflow
 60 | import androidx.compose.ui.unit.dp
 61 | import com.vero.repolens.data.engine.RecommendationEngine
 62 | import com.vero.repolens.data.models.RepoLensReport
 63 | import com.vero.repolens.ui.components.ExportDialog
 64 | import java.text.SimpleDateFormat
 65 | import java.util.Date
 66 | import java.util.Locale
 67 | 
 68 | @OptIn(ExperimentalMaterial3Api::class)
 69 | @Composable
 70 | fun OverviewScreen(
 71 |     report: RepoLensReport,
 72 |     onNavigateToArchitecture: () -> Unit,
 73 |     onNavigateToModules: () -> Unit,
 74 |     onNavigateToFeatures: () -> Unit,
 75 |     onNavigateToDI: () -> Unit,
 76 |     onNavigateToUI: () -> Unit,
 77 |     onNavigateToConcurrency: () -> Unit,
 78 |     onNavigateToTesting: () -> Unit,
 79 |     onNavigateToRisks: () -> Unit,
 80 |     onNavigateToPR: () -> Unit,
 81 |     onNavigateToSearch: () -> Unit = {},
 82 |     onNavigateToActionItems: () -> Unit = {},
 83 |     onNavigateToPerformance: () -> Unit = {},
 84 |     onNavigateToRecommendations: () -> Unit = {},
 85 |     onNavigateToExport: () -> Unit = {}
 86 | ) {
 87 |     val generatedRecommendations = remember(report) {
 88 |         RecommendationEngine().generateRecommendations(report)
 89 |     }
 90 |     var showExportDialog by remember { mutableStateOf(false) }
 91 |     if (showExportDialog) {
 92 |         ExportDialog(
 93 |             report = report,
 94 |             onDismiss = { showExportDialog = false }
 95 |         )
 96 |     }
 97 | 
 98 |     Scaffold(
 99 |         topBar = {
100 |             TopAppBar(
101 |                 title = { Text("RepoLens") },
102 |                 actions = {
103 |                     IconButton(onClick = onNavigateToSearch) {
104 |                         Icon(
105 |                             imageVector = Icons.Default.Search,
106 |                             contentDescription = "Search"
107 |                         )
108 |                     }
109 |                     IconButton(onClick = { showExportDialog = true }) {
110 |                         Icon(
111 |                             imageVector = Icons.Default.Share,
112 |                             contentDescription = "Export"
113 |                         )
114 |                     }
115 |                 },
116 |                 colors = TopAppBarDefaults.topAppBarColors(
117 |                     containerColor = MaterialTheme.colorScheme.primaryContainer,
118 |                     titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
119 |                 )
120 |             )
121 |         }
122 |     ) { paddingValues ->
123 |         LazyColumn(
124 |             modifier = Modifier
125 |                 .fillMaxSize()
126 |                 .padding(paddingValues),
127 |             contentPadding = PaddingValues(16.dp),
128 |             verticalArrangement = Arrangement.spacedBy(18.dp)
129 |         ) {
130 |             item {
131 |                 OverviewHeroCard(report = report)
132 |             }
133 | 
134 |             item {
135 |                 FeaturedScoresRow(report = report)
136 |             }
137 | 
138 |             item {
139 |                 OverviewLabel(text = "Snapshot")
140 |             }
141 | 
142 |             item {
143 |                 MetricsGrid(report = report)
144 |             }
145 | 
146 |             item {
147 |                 OverviewLabel(text = "Architecture")
148 |             }
149 | 
150 |             item {
151 |                 OverviewActionCard(
152 |                     title = "Architecture",
153 |                     subtitle = report.architecture.style,
154 |                     supporting = architectureSummary(report),
155 |                     icon = Icons.Default.Architecture,
156 |                     onClick = onNavigateToArchitecture
157 |                 )
158 |             }
159 | 
160 |             item {
161 |                 OverviewActionCard(
162 |                     title = "Modules",
163 |                     subtitle = "${report.modules.size} modules",
164 |                     supporting = report.modules.take(3).joinToString(", ") { it.name }.ifBlank { "Project structure overview" },
165 |                     icon = Icons.Default.Folder,
166 |                     onClick = onNavigateToModules
167 |                 )
168 |             }
169 | 
170 |             item {
171 |                 OverviewActionCard(
172 |                     title = "Features",
173 |                     subtitle = "${report.features.size} detected",
174 |                     supporting = report.features.take(3).joinToString(", ") { it.name }.ifBlank { "Feature landscape" },
175 |                     icon = Icons.Default.Star,
176 |                     onClick = onNavigateToFeatures
177 |                 )
178 |             }
179 | 
180 |             item {
181 |                 OverviewLabel(text = "Code Health")
182 |             }
183 | 
184 |             report.dependencyInjection?.let { di ->
185 |                 item {
186 |                     OverviewActionCard(
187 |                         title = "Dependency Injection",
188 |                         subtitle = di.framework,
189 |                         supporting = di.summary,
190 |                         icon = Icons.Default.Link,
191 |                         onClick = onNavigateToDI
192 |                     )
193 |                 }
194 |             }
195 | 
196 |             report.uiLayer?.let { uiLayer ->
197 |                 item {
198 |                     OverviewActionCard(
199 |                         title = "UI Layer",
200 |                         subtitle = uiLayer.framework,
201 |                         supporting = uiLayer.summary,
202 |                         icon = Icons.Default.Smartphone,
203 |                         onClick = onNavigateToUI
204 |                     )
205 |                 }
206 |             }
207 | 
208 |             report.concurrency?.let { concurrency ->
209 |                 item {
210 |                     OverviewActionCard(
211 |                         title = "Concurrency",
212 |                         subtitle = concurrency.technologies.take(2).joinToString(", "),
213 |                         supporting = concurrency.summary,
214 |                         icon = Icons.Default.Sync,
215 |                         onClick = onNavigateToConcurrency
216 |                     )
217 |                 }
218 |             }
219 | 
220 |             report.testing?.let {
221 |                 item {
222 |                     OverviewActionCard(
223 |                         title = "Testing",
224 |                         subtitle = "${report.metrics.existingTestsCount} tests",
225 |                         supporting = "${report.metrics.missingTestsCount} gaps need attention",
226 |                         icon = Icons.Default.Science,
227 |                         onClick = onNavigateToTesting
228 |                     )
229 |                 }
230 |             }
231 | 
232 |             item {
233 |                 OverviewLabel(text = "Workflow")
234 |             }
235 | 
236 |             item {
237 |                 OverviewActionCard(
238 |                     title = "Risks",
239 |                     subtitle = "${report.risks.size} identified",
240 |                     supporting = riskSummary(report),
241 |                     icon = Icons.Default.Warning,
242 |                     onClick = onNavigateToRisks,
243 |                     accentColor = MaterialTheme.colorScheme.tertiary
244 |                 )
245 |             }
246 | 
247 |             report.prReadiness?.let { prReadiness ->
248 |                 item {
249 |                     OverviewActionCard(
250 |                         title = "PR Readiness",
251 |                         subtitle = "Score ${prReadiness.score}%",
252 |                         supporting = prReadiness.summary,
253 |                         icon = Icons.Default.CheckCircle,
254 |                         onClick = onNavigateToPR,
255 |                         accentColor = MaterialTheme.colorScheme.secondary
256 |                     )
257 |                 }
258 |             }
259 | 
260 |             item {
261 |                 OverviewActionCard(
262 |                     title = "Action Items",
263 |                     subtitle = "Track follow-up work",
264 |                     supporting = "Manage development tasks and implementation next steps",
265 |                     icon = Icons.Default.Task,
266 |                     onClick = onNavigateToActionItems
267 |                 )
268 |             }
269 | 
270 |             item {
271 |                 OverviewLabel(text = "Insights")
272 |             }
273 | 
274 |             report.performance?.let { performance ->
275 |                 item {
276 |                     OverviewActionCard(
277 |                         title = "Performance Metrics",
278 |                         subtitle = "${performance.buildTimeSeconds}s build time",
279 |                         supporting = "${performance.methodCount} methods, ${performance.appSizeMB} MB app size",
280 |                         icon = Icons.Default.Speed,
281 |                         onClick = onNavigateToPerformance
282 |                     )
283 |                 }
284 |             }
285 | 
286 |             item {
287 |                 OverviewActionCard(
288 |                     title = "Smart Recommendations",
289 |                     subtitle = "${generatedRecommendations.size} recommendations",
290 |                     supporting = "AI-guided opportunities across testing, architecture, and performance",
291 |                     icon = Icons.Default.AutoAwesome,
292 |                     onClick = onNavigateToRecommendations,
293 |                     accentColor = MaterialTheme.colorScheme.primary
294 |                 )
295 |             }
296 | 
297 |             item {
298 |                 Text(
299 |                     text = "Generated by ${report.metadata.generatedBy}",
300 |                     style = MaterialTheme.typography.bodySmall,
301 |                     color = MaterialTheme.colorScheme.onSurfaceVariant
302 |                 )
303 |             }
304 |         }
305 |     }
306 | }
307 | 
308 | @Composable
309 | private fun OverviewHeroCard(report: RepoLensReport) {
310 |     var summaryExpanded by remember(report.repository.summary) { mutableStateOf(false) }
311 | 
312 |     Card(
313 |         colors = CardDefaults.cardColors(
314 |             containerColor = MaterialTheme.colorScheme.primaryContainer
315 |         ),
316 |         shape = MaterialTheme.shapes.extraLarge
317 |     ) {
318 |         Column(
319 |             modifier = Modifier
320 |                 .fillMaxWidth()
321 |                 .padding(22.dp),
322 |             verticalArrangement = Arrangement.spacedBy(14.dp)
323 |         ) {
324 |             Text(
325 |                 text = report.repository.name,
326 |                 style = MaterialTheme.typography.headlineMedium,
327 |                 fontWeight = FontWeight.Bold,
328 |                 color = MaterialTheme.colorScheme.onPrimaryContainer
329 |             )
330 |             Text(
331 |                 text = report.repository.summary,
332 |                 style = MaterialTheme.typography.bodyLarge,
333 |                 color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.86f),
334 |                 maxLines = if (summaryExpanded) Int.MAX_VALUE else 4,
335 |                 overflow = TextOverflow.Ellipsis
336 |             )
337 |             if (report.repository.summary.length > 140) {
338 |                 TextButton(
339 |                     onClick = { summaryExpanded = !summaryExpanded },
340 |                     contentPadding = PaddingValues(0.dp)
341 |                 ) {
342 |                     Text(
343 |                         text = if (summaryExpanded) "Show less" else "Read more",
344 |                         color = MaterialTheme.colorScheme.onPrimaryContainer
345 |                     )
346 |                 }
347 |             }
348 |             Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
349 |                 HeroChip(
350 |                     icon = Icons.Default.Code,
351 |                     label = report.repository.mainLanguage
352 |                 )
353 |                 HeroChip(
354 |                     icon = Icons.Default.PhoneAndroid,
355 |                     label = report.repository.platform
356 |                 )
357 |             }
358 |             Surface(
359 |                 color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
360 |                 shape = MaterialTheme.shapes.large
361 |             ) {
362 |                 Column(
363 |                     modifier = Modifier
364 |                         .fillMaxWidth()
365 |                         .padding(14.dp),
366 |                     verticalArrangement = Arrangement.spacedBy(6.dp)
367 |                 ) {
368 |                     MetaLine(
369 |                         label = "Architecture",
370 |                         value = report.repository.architectureStyle
371 |                     )
372 |                     MetaLine(
373 |                         label = "Generated",
374 |                         value = formatDate(report.metadata.generatedAt)
375 |                     )
376 |                 }
377 |             }
378 |         }
379 |     }
380 | }
381 | 
382 | @Composable
383 | private fun HeroChip(
384 |     icon: ImageVector,
385 |     label: String
386 | ) {
387 |     AssistChip(
388 |         onClick = {},
389 |         label = { Text(label) },
390 |         leadingIcon = {
391 |             Icon(
392 |                 imageVector = icon,
393 |                 contentDescription = null,
394 |                 modifier = Modifier.size(16.dp)
395 |             )
396 |         },
397 |         colors = AssistChipDefaults.assistChipColors(
398 |             containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f),
399 |             labelColor = MaterialTheme.colorScheme.onSurface,
400 |             leadingIconContentColor = MaterialTheme.colorScheme.primary
401 |         )
402 |     )
403 | }
404 | 
405 | @Composable
406 | private fun MetaLine(
407 |     label: String,
408 |     value: String
409 | ) {
410 |     Row(
411 |         modifier = Modifier.fillMaxWidth(),
412 |         horizontalArrangement = Arrangement.SpaceBetween
413 |     ) {
414 |         Text(
415 |             text = label,
416 |             style = MaterialTheme.typography.labelMedium,
417 |             color = MaterialTheme.colorScheme.onSurfaceVariant
418 |         )
419 |         Spacer(modifier = Modifier.width(16.dp))
420 |         Text(
421 |             text = value,
422 |             style = MaterialTheme.typography.bodyMedium,
423 |             fontWeight = FontWeight.Medium,
424 |             color = MaterialTheme.colorScheme.onSurface,
425 |             maxLines = 1,
426 |             overflow = TextOverflow.Ellipsis
427 |         )
428 |     }
429 | }
430 | 
431 | @Composable
432 | private fun FeaturedScoresRow(report: RepoLensReport) {
433 |     Row(
434 |         modifier = Modifier.fillMaxWidth(),
435 |         horizontalArrangement = Arrangement.spacedBy(12.dp)
436 |     ) {
437 |         FeaturedScoreCard(
438 |             title = "PR Readiness",
439 |             value = "${report.metrics.prReadinessScore}%",
440 |             subtitle = "Review confidence",
441 |             icon = Icons.Default.CheckCircle,
442 |             accentColor = MaterialTheme.colorScheme.secondary,
443 |             modifier = Modifier.weight(1f)
444 |         )
445 |         FeaturedScoreCard(
446 |             title = "Architecture",
447 |             value = "${report.metrics.architectureConfidenceScore}%",
448 |             subtitle = "Structural confidence",
449 |             icon = Icons.Default.Architecture,
450 |             accentColor = MaterialTheme.colorScheme.primary,
451 |             modifier = Modifier.weight(1f)
452 |         )
453 |     }
454 | }
455 | 
456 | @Composable
457 | private fun FeaturedScoreCard(
458 |     title: String,
459 |     value: String,
460 |     subtitle: String,
461 |     icon: ImageVector,
462 |     accentColor: Color,
463 |     modifier: Modifier = Modifier
464 | ) {
465 |     Card(
466 |         modifier = modifier,
467 |         shape = MaterialTheme.shapes.extraLarge,
468 |         colors = CardDefaults.cardColors(
469 |             containerColor = MaterialTheme.colorScheme.surface
470 |         ),
471 |         border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
472 |     ) {
473 |         Column(
474 |             modifier = Modifier
475 |                 .fillMaxWidth()
476 |                 .padding(18.dp),
477 |             verticalArrangement = Arrangement.spacedBy(14.dp)
478 |         ) {
479 |             Surface(
480 |                 color = accentColor.copy(alpha = 0.14f),
481 |                 shape = MaterialTheme.shapes.medium
482 |             ) {
483 |                 Icon(
484 |                     imageVector = icon,
485 |                     contentDescription = null,
486 |                     tint = accentColor,
487 |                     modifier = Modifier.padding(10.dp)
488 |                 )
489 |             }
490 |             Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
491 |                 Text(
492 |                     text = title,
493 |                     style = MaterialTheme.typography.labelLarge,
494 |                     color = MaterialTheme.colorScheme.onSurfaceVariant
495 |                 )
496 |                 Text(
497 |                     text = value,
498 |                     style = MaterialTheme.typography.headlineLarge,
499 |                     fontWeight = FontWeight.Bold,
500 |                     color = MaterialTheme.colorScheme.onSurface
501 |                 )
502 |                 Text(
503 |                     text = subtitle,
504 |                     style = MaterialTheme.typography.bodySmall,
505 |                     color = MaterialTheme.colorScheme.onSurfaceVariant
506 |                 )
507 |             }
508 |         }
509 |     }
510 | }
511 | 
512 | @Composable
513 | private fun OverviewLabel(text: String) {
514 |     Text(
515 |         text = text,
516 |         style = MaterialTheme.typography.titleLarge,
517 |         fontWeight = FontWeight.Bold,
518 |         color = MaterialTheme.colorScheme.onBackground
519 |     )
520 | }
521 | 
522 | @Composable
523 | private fun MetricsGrid(report: RepoLensReport) {
524 |     val metrics = listOf(
525 |         SnapshotMetric("Modules", report.modules.size.toString(), Icons.Default.Folder),
526 |         SnapshotMetric("Features", report.features.size.toString(), Icons.Default.Star),
527 |         SnapshotMetric("High Risks", report.metrics.highRiskAreasCount.toString(), Icons.Default.Warning),
528 |         SnapshotMetric("Missing Tests", report.metrics.missingTestsCount.toString(), Icons.Default.BugReport)
529 |     )
530 | 
531 |     Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
532 |         metrics.chunked(2).forEach { rowMetrics ->
533 |             Row(
534 |                 modifier = Modifier.fillMaxWidth(),
535 |                 horizontalArrangement = Arrangement.spacedBy(12.dp)
536 |             ) {
537 |                 rowMetrics.forEach { metric ->
538 |                     SnapshotMetricCard(
539 |                         metric = metric,
540 |                         modifier = Modifier.weight(1f)
541 |                     )
542 |                 }
543 |                 if (rowMetrics.size == 1) {
544 |                     Spacer(modifier = Modifier.weight(1f))
545 |                 }
546 |             }
547 |         }
548 |     }
549 | }
550 | 
551 | @Composable
552 | private fun SnapshotMetricCard(
553 |     metric: SnapshotMetric,
554 |     modifier: Modifier = Modifier
555 | ) {
556 |     Card(
557 |         modifier = modifier,
558 |         shape = MaterialTheme.shapes.large,
559 |         colors = CardDefaults.cardColors(
560 |             containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
561 |         )
562 |     ) {
563 |         Column(
564 |             modifier = Modifier
565 |                 .fillMaxWidth()
566 |                 .padding(14.dp),
567 |             verticalArrangement = Arrangement.spacedBy(14.dp)
568 |         ) {
569 |             Surface(
570 |                 color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
571 |                 shape = MaterialTheme.shapes.medium
572 |             ) {
573 |                 Icon(
574 |                     imageVector = metric.icon,
575 |                     contentDescription = null,
576 |                     tint = MaterialTheme.colorScheme.primary,
577 |                     modifier = Modifier.padding(8.dp)
578 |                 )
579 |             }
580 |             Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
581 |                 Text(
582 |                     text = metric.value,
583 |                     style = MaterialTheme.typography.headlineSmall,
584 |                     fontWeight = FontWeight.Bold
585 |                 )
586 |                 Text(
587 |                     text = metric.title,
588 |                     style = MaterialTheme.typography.bodySmall,
589 |                     color = MaterialTheme.colorScheme.onSurfaceVariant
590 |                 )
591 |             }
592 |         }
593 |     }
594 | }
595 | 
596 | @Composable
597 | private fun OverviewActionCard(
598 |     title: String,
599 |     subtitle: String,
600 |     supporting: String,
601 |     icon: ImageVector,
602 |     onClick: () -> Unit,
603 |     accentColor: Color = MaterialTheme.colorScheme.primary
604 | ) {
605 |     Card(
606 |         onClick = onClick,
607 |         shape = MaterialTheme.shapes.extraLarge,
608 |         colors = CardDefaults.cardColors(
609 |             containerColor = MaterialTheme.colorScheme.surface
610 |         ),
611 |         border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
612 |     ) {
613 |         Row(
614 |             modifier = Modifier
615 |                 .fillMaxWidth()
616 |                 .padding(18.dp),
617 |             horizontalArrangement = Arrangement.spacedBy(14.dp),
618 |             verticalAlignment = Alignment.CenterVertically
619 |         ) {
620 |             Surface(
621 |                 color = accentColor.copy(alpha = 0.12f),
622 |                 shape = MaterialTheme.shapes.large
623 |             ) {
624 |                 Icon(
625 |                     imageVector = icon,
626 |                     contentDescription = null,
627 |                     tint = accentColor,
628 |                     modifier = Modifier.padding(12.dp)
629 |                 )
630 |             }
631 |             Column(
632 |                 modifier = Modifier.weight(1f),
633 |                 verticalArrangement = Arrangement.spacedBy(4.dp)
634 |             ) {
635 |                 Text(
636 |                     text = title,
637 |                     style = MaterialTheme.typography.titleMedium,
638 |                     fontWeight = FontWeight.SemiBold,
639 |                     color = MaterialTheme.colorScheme.onSurface
640 |                 )
641 |                 Text(
642 |                     text = subtitle,
643 |                     style = MaterialTheme.typography.bodyMedium,
644 |                     fontWeight = FontWeight.Medium,
645 |                     color = MaterialTheme.colorScheme.onSurfaceVariant,
646 |                     maxLines = 1,
647 |                     overflow = TextOverflow.Ellipsis
648 |                 )
649 |                 Text(
650 |                     text = supporting,
651 |                     style = MaterialTheme.typography.bodySmall,
652 |                     color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f),
653 |                     maxLines = 2,
654 |                     overflow = TextOverflow.Ellipsis
655 |                 )
656 |             }
657 |             Icon(
658 |                 imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
659 |                 contentDescription = "Open",
660 |                 tint = MaterialTheme.colorScheme.onSurfaceVariant,
661 |                 modifier = Modifier.size(16.dp)
662 |             )
663 |         }
664 |     }
665 | }
666 | 
667 | private data class SnapshotMetric(
668 |     val title: String,
669 |     val value: String,
670 |     val icon: ImageVector
671 | )
672 | 
673 | private fun architectureSummary(report: RepoLensReport): String {
674 |     return report.architecture.layers
675 |         .take(3)
676 |         .joinToString(" · ") { it.name }
677 |         .ifBlank { report.architecture.summary }
678 | }
679 | 
680 | private fun riskSummary(report: RepoLensReport): String {
681 |     return report.risks
682 |         .take(2)
683 |         .joinToString(" · ") { it.title }
684 |         .ifBlank { "Review the most important delivery risks" }
685 | }
686 | 
687 | private fun formatDate(isoDate: String): String {
688 |     return try {
689 |         val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
690 |         val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
691 |         val date = inputFormat.parse(isoDate)
692 |         outputFormat.format(date ?: Date())
693 |     } catch (e: Exception) {
694 |         isoDate
695 |     }
696 | }
697 | 
698 | // Made with Bob
</content>
</file>
<file><path>app/src/main/java/com/vero/repolens/ui/screens/RecommendationsScreen.kt</path>
<content lines="1-320">
  1 | package com.vero.repolens.ui.screens
  2 | 
  3 | import androidx.compose.foundation.BorderStroke
  4 | import androidx.compose.foundation.layout.Arrangement
  5 | import androidx.compose.foundation.layout.Box
  6 | import androidx.compose.foundation.layout.Column
  7 | import androidx.compose.foundation.layout.FlowRow
  8 | import androidx.compose.foundation.layout.PaddingValues
  9 | import androidx.compose.foundation.layout.fillMaxSize
 10 | import androidx.compose.foundation.layout.fillMaxWidth
 11 | import androidx.compose.foundation.layout.padding
 12 | import androidx.compose.foundation.lazy.LazyColumn
 13 | import androidx.compose.foundation.lazy.items
 14 | import androidx.compose.material.icons.Icons
 15 | import androidx.compose.material.icons.automirrored.filled.ArrowBack
 16 | import androidx.compose.material.icons.filled.AutoAwesome
 17 | import androidx.compose.material.icons.filled.CheckCircle
 18 | import androidx.compose.material.icons.filled.Clear
 19 | import androidx.compose.material.icons.filled.FilterList
 20 | import androidx.compose.material3.Card
 21 | import androidx.compose.material3.CardDefaults
 22 | import androidx.compose.material3.ExperimentalMaterial3Api
 23 | import androidx.compose.material3.FilterChip
 24 | import androidx.compose.material3.Icon
 25 | import androidx.compose.material3.IconButton
 26 | import androidx.compose.material3.MaterialTheme
 27 | import androidx.compose.material3.Scaffold
 28 | import androidx.compose.material3.Surface
 29 | import androidx.compose.material3.Text
 30 | import androidx.compose.material3.TextButton
 31 | import androidx.compose.material3.TopAppBar
 32 | import androidx.compose.material3.TopAppBarDefaults
 33 | import androidx.compose.runtime.Composable
 34 | import androidx.compose.runtime.collectAsState
 35 | import androidx.compose.runtime.getValue
 36 | import androidx.compose.runtime.mutableStateOf
 37 | import androidx.compose.runtime.remember
 38 | import androidx.compose.runtime.setValue
 39 | import androidx.compose.ui.Alignment
 40 | import androidx.compose.ui.Modifier
 41 | import androidx.compose.ui.text.font.FontWeight
 42 | import androidx.compose.ui.unit.dp
 43 | import androidx.hilt.navigation.compose.hiltViewModel
 44 | import com.vero.repolens.data.models.EffortLevel
 45 | import com.vero.repolens.data.models.ImpactLevel
 46 | import com.vero.repolens.data.models.SearchFilter
 47 | import com.vero.repolens.ui.components.RecommendationCard
 48 | import com.vero.repolens.viewmodel.RecommendationsViewModel
 49 | 
 50 | @OptIn(ExperimentalMaterial3Api::class)
 51 | @Composable
 52 | fun RecommendationsScreen(
 53 |     onNavigateBack: () -> Unit,
 54 |     viewModel: RecommendationsViewModel = hiltViewModel()
 55 | ) {
 56 |     val allRecommendations by viewModel.recommendations.collectAsState()
 57 |     val recommendations by viewModel.filteredRecommendations.collectAsState()
 58 |     val selectedCategory by viewModel.selectedCategory.collectAsState()
 59 |     val selectedImpact by viewModel.selectedImpact.collectAsState()
 60 |     val selectedEffort by viewModel.selectedEffort.collectAsState()
 61 | 
 62 |     var showFilters by remember { mutableStateOf(false) }
 63 |     val categories = remember(allRecommendations) {
 64 |         allRecommendations.map { it.category }.distinct().sorted()
 65 |     }
 66 |     val hasActiveFilters = selectedCategory != null || selectedImpact != null || selectedEffort != null
 67 | 
 68 |     Scaffold(
 69 |         topBar = {
 70 |             TopAppBar(
 71 |                 title = { Text("Smart Recommendations") },
 72 |                 navigationIcon = {
 73 |                     IconButton(onClick = onNavigateBack) {
 74 |                         Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
 75 |                     }
 76 |                 },
 77 |                 actions = {
 78 |                     IconButton(onClick = { showFilters = !showFilters }) {
 79 |                         Icon(Icons.Default.FilterList, contentDescription = "Filter")
 80 |                     }
 81 |                 },
 82 |                 colors = TopAppBarDefaults.topAppBarColors(
 83 |                     containerColor = MaterialTheme.colorScheme.surface,
 84 |                     titleContentColor = MaterialTheme.colorScheme.onSurface
 85 |                 )
 86 |             )
 87 |         }
 88 |     ) { padding ->
 89 |         Column(
 90 |             modifier = Modifier
 91 |                 .fillMaxSize()
 92 |                 .padding(padding)
 93 |         ) {
 94 |             Card(
 95 |                 modifier = Modifier
 96 |                     .fillMaxWidth()
 97 |                     .padding(horizontal = 16.dp, vertical = 12.dp),
 98 |                 colors = CardDefaults.cardColors(
 99 |                     containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
100 |                 ),
101 |                 shape = MaterialTheme.shapes.extraLarge
102 |             ) {
103 |                 Column(
104 |                     modifier = Modifier
105 |                         .fillMaxWidth()
106 |                         .padding(18.dp),
107 |                     verticalArrangement = Arrangement.spacedBy(14.dp)
108 |                 ) {
109 |                     androidx.compose.foundation.layout.Row(
110 |                         modifier = Modifier.fillMaxWidth(),
111 |                         horizontalArrangement = Arrangement.SpaceBetween,
112 |                         verticalAlignment = Alignment.CenterVertically
113 |                     ) {
114 |                         Column {
115 |                             Text(
116 |                                 text = "${recommendations.size} Recommendations",
117 |                                 style = MaterialTheme.typography.titleLarge,
118 |                                 fontWeight = FontWeight.SemiBold,
119 |                                 color = MaterialTheme.colorScheme.onPrimaryContainer
120 |                             )
121 |                             Text(
122 |                                 text = if (hasActiveFilters) {
123 |                                     "${allRecommendations.size} total generated recommendations"
124 |                                 } else {
125 |                                     "Based on your codebase analysis"
126 |                                 },
127 |                                 style = MaterialTheme.typography.bodyMedium,
128 |                                 color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
129 |                             )
130 |                         }
131 |                         Icon(
132 |                             imageVector = Icons.Default.AutoAwesome,
133 |                             contentDescription = null,
134 |                             modifier = Modifier.padding(start = 12.dp),
135 |                             tint = MaterialTheme.colorScheme.onPrimaryContainer
136 |                         )
137 |                     }
138 | 
139 |                     FlowRow(
140 |                         horizontalArrangement = Arrangement.spacedBy(8.dp),
141 |                         verticalArrangement = Arrangement.spacedBy(8.dp)
142 |                     ) {
143 |                         SummaryChip("${allRecommendations.count { it.impact == ImpactLevel.HIGH }} high impact")
144 |                         SummaryChip("${allRecommendations.count { it.effort == EffortLevel.SMALL }} quick wins")
145 |                         SummaryChip("${categories.size} categories")
146 |                     }
147 |                 }
148 |             }
149 | 
150 |             if (showFilters) {
151 |                 Card(
152 |                     modifier = Modifier
153 |                         .fillMaxWidth()
154 |                         .padding(horizontal = 16.dp, vertical = 4.dp),
155 |                     colors = CardDefaults.cardColors(
156 |                         containerColor = MaterialTheme.colorScheme.surface
157 |                     ),
158 |                     border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
159 |                     shape = MaterialTheme.shapes.extraLarge
160 |                 ) {
161 |                     Column(
162 |                         modifier = Modifier
163 |                             .fillMaxWidth()
164 |                             .padding(16.dp),
165 |                         verticalArrangement = Arrangement.spacedBy(16.dp)
166 |                     ) {
167 |                         FilterGroup(
168 |                             title = "Category",
169 |                             content = {
170 |                                 FilterChip(
171 |                                     selected = selectedCategory == null,
172 |                                     onClick = { viewModel.setCategory(null) },
173 |                                     label = { Text("All") }
174 |                                 )
175 |                                 categories.forEach { category ->
176 |                                     FilterChip(
177 |                                         selected = selectedCategory == category,
178 |                                         onClick = { viewModel.setCategory(category) },
179 |                                         label = { Text(category) }
180 |                                     )
181 |                                 }
182 |                             }
183 |                         )
184 | 
185 |                         FilterGroup(
186 |                             title = "Impact",
187 |                             content = {
188 |                                 FilterChip(
189 |                                     selected = selectedImpact == null,
190 |                                     onClick = { viewModel.setImpact(null) },
191 |                                     label = { Text("All") }
192 |                                 )
193 |                                 ImpactLevel.entries.forEach { impact ->
194 |                                     FilterChip(
195 |                                         selected = selectedImpact == impact,
196 |                                         onClick = { viewModel.setImpact(impact) },
197 |                                         label = { Text(impact.name) }
198 |                                     )
199 |                                 }
200 |                             }
201 |                         )
202 | 
203 |                         FilterGroup(
204 |                             title = "Effort",
205 |                             content = {
206 |                                 FilterChip(
207 |                                     selected = selectedEffort == null,
208 |                                     onClick = { viewModel.setEffort(null) },
209 |                                     label = { Text("All") }
210 |                                 )
211 |                                 EffortLevel.entries.forEach { effort ->
212 |                                     FilterChip(
213 |                                         selected = selectedEffort == effort,
214 |                                         onClick = { viewModel.setEffort(effort) },
215 |                                         label = { Text(effort.name) }
216 |                                     )
217 |                                 }
218 |                             }
219 |                         )
220 | 
221 |                         if (hasActiveFilters) {
222 |                             TextButton(
223 |                                 onClick = { viewModel.clearFilters() },
224 |                                 modifier = Modifier.align(Alignment.End)
225 |                             ) {
226 |                                 Icon(
227 |                                     imageVector = Icons.Default.Clear,
228 |                                     contentDescription = null
229 |                                 )
230 |                                 Text("Clear Filters")
231 |                             }
232 |                         }
233 |                     }
234 |                 }
235 |             }
236 | 
237 |             if (recommendations.isEmpty()) {
238 |                 Box(
239 |                     modifier = Modifier
240 |                         .fillMaxSize()
241 |                         .padding(16.dp),
242 |                     contentAlignment = Alignment.Center
243 |                 ) {
244 |                     Column(
245 |                         horizontalAlignment = Alignment.CenterHorizontally,
246 |                         verticalArrangement = Arrangement.spacedBy(10.dp)
247 |                     ) {
248 |                         Icon(
249 |                             imageVector = Icons.Default.CheckCircle,
250 |                             contentDescription = null,
251 |                             modifier = Modifier.padding(bottom = 4.dp),
252 |                             tint = MaterialTheme.colorScheme.primary
253 |                         )
254 |                         Text(
255 |                             text = if (allRecommendations.isEmpty()) {
256 |                                 "No smart recommendations available"
257 |                             } else {
258 |                                 "No recommendations match your filters"
259 |                             },
260 |                             style = MaterialTheme.typography.titleMedium,
261 |                             color = MaterialTheme.colorScheme.onSurfaceVariant
262 |                         )
263 |                         if (hasActiveFilters) {
264 |                             TextButton(onClick = { viewModel.clearFilters() }) {
265 |                                 Text("Clear Filters")
266 |                             }
267 |                         }
268 |                     }
269 |                 }
270 |             } else {
271 |                 LazyColumn(
272 |                     modifier = Modifier.fillMaxSize(),
273 |                     contentPadding = PaddingValues(16.dp),
274 |                     verticalArrangement = Arrangement.spacedBy(12.dp)
275 |                 ) {
276 |                     items(recommendations) { recommendation ->
277 |                         RecommendationCard(recommendation = recommendation)
278 |                     }
279 |                 }
280 |             }
281 |         }
282 |     }
283 | }
284 | 
285 | @Composable
286 | private fun FilterGroup(
287 |     title: String,
288 |     content: @Composable () -> Unit
289 | ) {
290 |     Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
291 |         Text(
292 |             text = title,
293 |             style = MaterialTheme.typography.labelLarge,
294 |             color = MaterialTheme.colorScheme.onSurfaceVariant
295 |         )
296 |         FlowRow(
297 |             horizontalArrangement = Arrangement.spacedBy(8.dp),
298 |             verticalArrangement = Arrangement.spacedBy(8.dp)
299 |         ) {
300 |             content()
301 |         }
302 |     }
303 | }
304 | 
305 | @Composable
306 | private fun SummaryChip(text: String) {
307 |     Surface(
308 |         color = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f),
309 |         shape = MaterialTheme.shapes.large
310 |     ) {
311 |         Text(
312 |             text = text,
313 |             modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
314 |             style = MaterialTheme.typography.labelLarge,
315 |             color = MaterialTheme.colorScheme.onSurface
316 |         )
317 |     }
318 | }
319 | 
320 | // Made with Bob
</content>
</file>
<file><path>app/src/main/java/com/vero/repolens/ui/screens/RisksScreen.kt</path>
<content lines="1-358">
  1 | package com.vero.repolens.ui.screens
  2 | 
  3 | import androidx.compose.foundation.BorderStroke
  4 | import androidx.compose.foundation.layout.Arrangement
  5 | import androidx.compose.foundation.layout.Column
  6 | import androidx.compose.foundation.layout.FlowRow
  7 | import androidx.compose.foundation.layout.PaddingValues
  8 | import androidx.compose.foundation.layout.Row
  9 | import androidx.compose.foundation.layout.fillMaxSize
 10 | import androidx.compose.foundation.layout.fillMaxWidth
 11 | import androidx.compose.foundation.layout.padding
 12 | import androidx.compose.foundation.lazy.LazyColumn
 13 | import androidx.compose.foundation.lazy.items
 14 | import androidx.compose.material.icons.Icons
 15 | import androidx.compose.material.icons.automirrored.filled.ArrowBack
 16 | import androidx.compose.material.icons.filled.Warning
 17 | import androidx.compose.material3.Card
 18 | import androidx.compose.material3.CardDefaults
 19 | import androidx.compose.material3.ExperimentalMaterial3Api
 20 | import androidx.compose.material3.Icon
 21 | import androidx.compose.material3.IconButton
 22 | import androidx.compose.material3.MaterialTheme
 23 | import androidx.compose.material3.Scaffold
 24 | import androidx.compose.material3.Surface
 25 | import androidx.compose.material3.Text
 26 | import androidx.compose.material3.TopAppBar
 27 | import androidx.compose.material3.TopAppBarDefaults
 28 | import androidx.compose.runtime.Composable
 29 | import androidx.compose.ui.Alignment
 30 | import androidx.compose.ui.Modifier
 31 | import androidx.compose.ui.graphics.Color
 32 | import androidx.compose.ui.text.font.FontWeight
 33 | import androidx.compose.ui.text.style.TextOverflow
 34 | import androidx.compose.ui.unit.dp
 35 | import com.vero.repolens.data.models.Risk
 36 | import com.vero.repolens.ui.components.ConfidenceBadge
 37 | import com.vero.repolens.ui.components.EmptyState
 38 | import com.vero.repolens.ui.components.FilePathChip
 39 | import com.vero.repolens.ui.components.SeverityBadge
 40 | 
 41 | @OptIn(ExperimentalMaterial3Api::class)
 42 | @Composable
 43 | fun RisksScreen(
 44 |     risks: List<Risk>,
 45 |     onNavigateBack: () -> Unit
 46 | ) {
 47 |     Scaffold(
 48 |         topBar = {
 49 |             TopAppBar(
 50 |                 title = { Text("Risks") },
 51 |                 navigationIcon = {
 52 |                     IconButton(onClick = onNavigateBack) {
 53 |                         Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
 54 |                     }
 55 |                 },
 56 |                 colors = TopAppBarDefaults.topAppBarColors(
 57 |                     containerColor = MaterialTheme.colorScheme.surface,
 58 |                     titleContentColor = MaterialTheme.colorScheme.onSurface
 59 |                 )
 60 |             )
 61 |         }
 62 |     ) { paddingValues ->
 63 |         if (risks.isEmpty()) {
 64 |             EmptyState(
 65 |                 message = "No risks identified",
 66 |                 modifier = Modifier.padding(paddingValues)
 67 |             )
 68 |         } else {
 69 |             val groupedRisks = risks.groupBy { it.severity.lowercase() }
 70 |             val severityOrder = listOf("critical", "high", "medium", "low")
 71 | 
 72 |             LazyColumn(
 73 |                 modifier = Modifier
 74 |                     .fillMaxSize()
 75 |                     .padding(paddingValues),
 76 |                 contentPadding = PaddingValues(16.dp),
 77 |                 verticalArrangement = Arrangement.spacedBy(16.dp)
 78 |             ) {
 79 |                 item {
 80 |                     RisksSummaryCard(risks = risks)
 81 |                 }
 82 | 
 83 |                 severityOrder.forEach { severity ->
 84 |                     val risksInCategory = groupedRisks[severity] ?: emptyList()
 85 |                     if (risksInCategory.isNotEmpty()) {
 86 |                         item {
 87 |                             SeveritySectionHeader(
 88 |                                 severity = severity,
 89 |                                 count = risksInCategory.size
 90 |                             )
 91 |                         }
 92 | 
 93 |                         items(risksInCategory) { risk ->
 94 |                             RiskCard(risk = risk)
 95 |                         }
 96 |                     }
 97 |                 }
 98 |             }
 99 |         }
100 |     }
101 | }
102 | 
103 | @Composable
104 | private fun RisksSummaryCard(risks: List<Risk>) {
105 |     val criticalCount = risks.count { it.severity.equals("critical", true) }
106 |     val highCount = risks.count { it.severity.equals("high", true) }
107 |     val mediumCount = risks.count { it.severity.equals("medium", true) }
108 |     val lowCount = risks.count { it.severity.equals("low", true) }
109 | 
110 |     Card(
111 |         colors = CardDefaults.cardColors(
112 |             containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
113 |         ),
114 |         border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)),
115 |         shape = MaterialTheme.shapes.extraLarge
116 |     ) {
117 |         Column(
118 |             modifier = Modifier
119 |                 .fillMaxWidth()
120 |                 .padding(18.dp),
121 |             verticalArrangement = Arrangement.spacedBy(14.dp)
122 |         ) {
123 |             Text(
124 |                 text = "${risks.size} risks identified",
125 |                 style = MaterialTheme.typography.headlineSmall,
126 |                 fontWeight = FontWeight.SemiBold
127 |             )
128 |             FlowRow(
129 |                 horizontalArrangement = Arrangement.spacedBy(8.dp),
130 |                 verticalArrangement = Arrangement.spacedBy(8.dp)
131 |             ) {
132 |                 if (criticalCount > 0) RiskInfoChip("$criticalCount critical")
133 |                 if (highCount > 0) RiskInfoChip("$highCount high")
134 |                 if (mediumCount > 0) RiskInfoChip("$mediumCount medium")
135 |                 if (lowCount > 0) RiskInfoChip("$lowCount low")
136 |                 RiskInfoChip("${risks.count { it.affectedFiles.isNotEmpty() }} with files")
137 |             }
138 |         }
139 |     }
140 | }
141 | 
142 | @Composable
143 | private fun SeveritySectionHeader(
144 |     severity: String,
145 |     count: Int
146 | ) {
147 |     Row(
148 |         modifier = Modifier.fillMaxWidth(),
149 |         horizontalArrangement = Arrangement.SpaceBetween,
150 |         verticalAlignment = Alignment.CenterVertically
151 |     ) {
152 |         Text(
153 |             text = "${severity.displayName()} ($count)",
154 |             style = MaterialTheme.typography.titleLarge,
155 |             fontWeight = FontWeight.SemiBold
156 |         )
157 |         Surface(
158 |             color = severityAccent(severity).copy(alpha = 0.12f),
159 |             shape = MaterialTheme.shapes.large
160 |         ) {
161 |             Text(
162 |                 text = severity.displayName(),
163 |                 modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
164 |                 style = MaterialTheme.typography.labelLarge,
165 |                 color = severityAccent(severity)
166 |             )
167 |         }
168 |     }
169 | }
170 | 
171 | @Composable
172 | private fun RiskCard(risk: Risk) {
173 |     val accent = severityAccent(risk.severity)
174 |     val container = when (risk.severity.lowercase()) {
175 |         "critical", "high" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.94f)
176 |         else -> MaterialTheme.colorScheme.surface
177 |     }
178 |     val contentColor = when (risk.severity.lowercase()) {
179 |         "critical", "high" -> MaterialTheme.colorScheme.onErrorContainer
180 |         else -> MaterialTheme.colorScheme.onSurface
181 |     }
182 |     val mutedColor = when (risk.severity.lowercase()) {
183 |         "critical", "high" -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.78f)
184 |         else -> MaterialTheme.colorScheme.onSurfaceVariant
185 |     }
186 | 
187 |     Card(
188 |         modifier = Modifier.fillMaxWidth(),
189 |         colors = CardDefaults.cardColors(containerColor = container),
190 |         shape = MaterialTheme.shapes.extraLarge
191 |     ) {
192 |         Column(
193 |             modifier = Modifier
194 |                 .fillMaxWidth()
195 |                 .padding(18.dp),
196 |             verticalArrangement = Arrangement.spacedBy(14.dp)
197 |         ) {
198 |             Row(
199 |                 modifier = Modifier.fillMaxWidth(),
200 |                 horizontalArrangement = Arrangement.SpaceBetween,
201 |                 verticalAlignment = Alignment.Top
202 |             ) {
203 |                 Row(
204 |                     modifier = Modifier.weight(1f),
205 |                     horizontalArrangement = Arrangement.spacedBy(10.dp),
206 |                     verticalAlignment = Alignment.Top
207 |                 ) {
208 |                     Icon(
209 |                         imageVector = Icons.Default.Warning,
210 |                         contentDescription = null,
211 |                         tint = contentColor
212 |                     )
213 |                     Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
214 |                         Text(
215 |                             text = risk.title,
216 |                             style = MaterialTheme.typography.titleLarge,
217 |                             fontWeight = FontWeight.SemiBold,
218 |                             color = contentColor
219 |                         )
220 |                         Text(
221 |                             text = "Category: ${risk.category}",
222 |                             style = MaterialTheme.typography.bodySmall,
223 |                             color = mutedColor
224 |                         )
225 |                     }
226 |                 }
227 |                 SeverityBadge(severity = risk.severity)
228 |             }
229 | 
230 |             FlowRow(
231 |                 horizontalArrangement = Arrangement.spacedBy(8.dp),
232 |                 verticalArrangement = Arrangement.spacedBy(8.dp)
233 |             ) {
234 |                 risk.confidence?.let { ConfidenceBadge(confidence = it) }
235 |                 if (risk.affectedModules.isNotEmpty()) {
236 |                     RiskInfoChip("${risk.affectedModules.size} modules", accent = accent)
237 |                 }
238 |                 if (risk.affectedFiles.isNotEmpty()) {
239 |                     RiskInfoChip("${risk.affectedFiles.size} files", accent = accent)
240 |                 }
241 |             }
242 | 
243 |             risk.whyItMatters?.let {
244 |                 RiskInfoPanel(
245 |                     title = "Why it matters",
246 |                     body = it,
247 |                     titleColor = accent,
248 |                     bodyColor = contentColor,
249 |                     tone = accent.copy(alpha = 0.08f)
250 |                 )
251 |             }
252 | 
253 |             risk.recommendation?.let {
254 |                 RiskInfoPanel(
255 |                     title = "Recommendation",
256 |                     body = it,
257 |                     titleColor = accent,
258 |                     bodyColor = contentColor,
259 |                     tone = MaterialTheme.colorScheme.surface.copy(alpha = 0.58f)
260 |                 )
261 |             }
262 | 
263 |             risk.suggestedTest?.let {
264 |                 RiskInfoPanel(
265 |                     title = "Suggested test",
266 |                     body = it,
267 |                     titleColor = accent,
268 |                     bodyColor = contentColor,
269 |                     tone = accent.copy(alpha = 0.08f)
270 |                 )
271 |             }
272 | 
273 |             if (risk.affectedFiles.isNotEmpty()) {
274 |                 Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
275 |                     Text(
276 |                         text = "Affected files",
277 |                         style = MaterialTheme.typography.labelLarge,
278 |                         fontWeight = FontWeight.SemiBold,
279 |                         color = contentColor
280 |                     )
281 |                     FlowRow(
282 |                         modifier = Modifier.fillMaxWidth(),
283 |                         horizontalArrangement = Arrangement.spacedBy(8.dp),
284 |                         verticalArrangement = Arrangement.spacedBy(8.dp)
285 |                     ) {
286 |                         risk.affectedFiles.forEach { file ->
287 |                             FilePathChip(path = file)
288 |                         }
289 |                     }
290 |                 }
291 |             }
292 |         }
293 |     }
294 | }
295 | 
296 | @Composable
297 | private fun RiskInfoPanel(
298 |     title: String,
299 |     body: String,
300 |     titleColor: Color,
301 |     bodyColor: Color,
302 |     tone: Color
303 | ) {
304 |     Surface(
305 |         color = tone,
306 |         shape = MaterialTheme.shapes.large
307 |     ) {
308 |         Column(
309 |             modifier = Modifier
310 |                 .fillMaxWidth()
311 |                 .padding(14.dp),
312 |             verticalArrangement = Arrangement.spacedBy(8.dp)
313 |         ) {
314 |             Text(
315 |                 text = title,
316 |                 style = MaterialTheme.typography.titleSmall,
317 |                 fontWeight = FontWeight.SemiBold,
318 |                 color = titleColor
319 |             )
320 |             Text(
321 |                 text = body,
322 |                 style = MaterialTheme.typography.bodyMedium,
323 |                 color = bodyColor
324 |             )
325 |         }
326 |     }
327 | }
328 | 
329 | @Composable
330 | private fun RiskInfoChip(
331 |     text: String,
332 |     accent: Color = MaterialTheme.colorScheme.primary
333 | ) {
334 |     Surface(
335 |         color = accent.copy(alpha = 0.12f),
336 |         shape = MaterialTheme.shapes.large
337 |     ) {
338 |         Text(
339 |             text = text,
340 |             modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
341 |             style = MaterialTheme.typography.labelLarge,
342 |             color = accent,
343 |             maxLines = 1,
344 |             overflow = TextOverflow.Ellipsis
345 |         )
346 |     }
347 | }
348 | 
349 | @Composable
350 | private fun severityAccent(severity: String): Color = when (severity.lowercase()) {
351 |     "critical", "high" -> MaterialTheme.colorScheme.error
352 |     "medium" -> MaterialTheme.colorScheme.tertiary
353 |     else -> MaterialTheme.colorScheme.primary
354 | }
355 | 
356 | private fun String.displayName(): String = replaceFirstChar { it.uppercase() }
357 | 
358 | // Made with Bob
</content>
</file>
<file><path>app/src/main/java/com/vero/repolens/ui/screens/SearchScreen.kt</path>
<content lines="1-400">
  1 | package com.vero.repolens.ui.screens
  2 | 
  3 | import androidx.compose.foundation.BorderStroke
  4 | import androidx.compose.foundation.clickable
  5 | import androidx.compose.foundation.layout.Arrangement
  6 | import androidx.compose.foundation.layout.Column
  7 | import androidx.compose.foundation.layout.FlowRow
  8 | import androidx.compose.foundation.layout.PaddingValues
  9 | import androidx.compose.foundation.layout.Row
 10 | import androidx.compose.foundation.layout.Spacer
 11 | import androidx.compose.foundation.layout.fillMaxSize
 12 | import androidx.compose.foundation.layout.fillMaxWidth
 13 | import androidx.compose.foundation.layout.height
 14 | import androidx.compose.foundation.layout.padding
 15 | import androidx.compose.foundation.layout.size
 16 | import androidx.compose.foundation.layout.width
 17 | import androidx.compose.foundation.lazy.LazyColumn
 18 | import androidx.compose.foundation.lazy.LazyRow
 19 | import androidx.compose.foundation.lazy.items
 20 | import androidx.compose.material.icons.Icons
 21 | import androidx.compose.material.icons.automirrored.filled.ArrowBack
 22 | import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
 23 | import androidx.compose.material.icons.filled.Clear
 24 | import androidx.compose.material.icons.filled.Description
 25 | import androidx.compose.material.icons.filled.Folder
 26 | import androidx.compose.material.icons.filled.Layers
 27 | import androidx.compose.material.icons.filled.Science
 28 | import androidx.compose.material.icons.filled.Search
 29 | import androidx.compose.material.icons.filled.SearchOff
 30 | import androidx.compose.material.icons.filled.Star
 31 | import androidx.compose.material.icons.filled.Warning
 32 | import androidx.compose.material3.Card
 33 | import androidx.compose.material3.CardDefaults
 34 | import androidx.compose.material3.ExperimentalMaterial3Api
 35 | import androidx.compose.material3.FilterChip
 36 | import androidx.compose.material3.Icon
 37 | import androidx.compose.material3.IconButton
 38 | import androidx.compose.material3.MaterialTheme
 39 | import androidx.compose.material3.OutlinedTextField
 40 | import androidx.compose.material3.Scaffold
 41 | import androidx.compose.material3.Surface
 42 | import androidx.compose.material3.Text
 43 | import androidx.compose.material3.TopAppBar
 44 | import androidx.compose.material3.TopAppBarDefaults
 45 | import androidx.compose.runtime.Composable
 46 | import androidx.compose.runtime.collectAsState
 47 | import androidx.compose.runtime.getValue
 48 | import androidx.compose.ui.Alignment
 49 | import androidx.compose.ui.Modifier
 50 | import androidx.compose.ui.graphics.Color
 51 | import androidx.compose.foundation.shape.RoundedCornerShape
 52 | import androidx.compose.ui.text.font.FontWeight
 53 | import androidx.compose.ui.text.style.TextOverflow
 54 | import androidx.compose.ui.unit.dp
 55 | import androidx.hilt.navigation.compose.hiltViewModel
 56 | import com.vero.repolens.data.models.SearchFilter
 57 | import com.vero.repolens.data.models.SearchItemType
 58 | import com.vero.repolens.data.models.SearchableItem
 59 | import com.vero.repolens.ui.components.LoadingState
 60 | import com.vero.repolens.viewmodel.SearchViewModel
 61 | 
 62 | @OptIn(ExperimentalMaterial3Api::class)
 63 | @Composable
 64 | fun SearchScreen(
 65 |     onNavigateBack: () -> Unit,
 66 |     onItemClick: (SearchableItem) -> Unit,
 67 |     viewModel: SearchViewModel = hiltViewModel()
 68 | ) {
 69 |     val searchQuery by viewModel.searchQuery.collectAsState()
 70 |     val searchResults by viewModel.searchResults.collectAsState()
 71 |     val activeFilter by viewModel.activeFilter.collectAsState()
 72 |     val isLoading by viewModel.isLoading.collectAsState()
 73 |     val availableSeverities by viewModel.availableSeverities.collectAsState()
 74 | 
 75 |     Scaffold(
 76 |         topBar = {
 77 |             TopAppBar(
 78 |                 title = { Text("Search") },
 79 |                 navigationIcon = {
 80 |                     IconButton(onClick = onNavigateBack) {
 81 |                         Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
 82 |                     }
 83 |                 },
 84 |                 actions = {
 85 |                     if (searchQuery.isNotEmpty() || activeFilter != SearchFilter.DEFAULT) {
 86 |                         IconButton(onClick = {
 87 |                             viewModel.clearSearch()
 88 |                             viewModel.clearFilters()
 89 |                         }) {
 90 |                             Icon(Icons.Default.Clear, contentDescription = "Clear all")
 91 |                         }
 92 |                     }
 93 |                 },
 94 |                 colors = TopAppBarDefaults.topAppBarColors(
 95 |                     containerColor = MaterialTheme.colorScheme.surface,
 96 |                     titleContentColor = MaterialTheme.colorScheme.onSurface
 97 |                 )
 98 |             )
 99 |         }
100 |     ) { paddingValues ->
101 |         Column(
102 |             modifier = Modifier
103 |                 .fillMaxSize()
104 |                 .padding(paddingValues)
105 |         ) {
106 |             SearchFieldCard(
107 |                 query = searchQuery,
108 |                 onQueryChange = { viewModel.updateSearchQuery(it) }
109 |             )
110 | 
111 |             FilterGroupCard(
112 |                 activeFilter = activeFilter,
113 |                 availableSeverities = availableSeverities.toList(),
114 |                 onTypeToggle = viewModel::toggleTypeFilter,
115 |                 onSeverityToggle = viewModel::toggleSeverityFilter
116 |             )
117 | 
118 |             if (isLoading) {
119 |                 LoadingState()
120 |             } else if (searchResults.isEmpty() && searchQuery.isNotEmpty()) {
121 |                 EmptySearchState()
122 |             } else {
123 |                 LazyColumn(
124 |                     verticalArrangement = Arrangement.spacedBy(10.dp),
125 |                     contentPadding = PaddingValues(16.dp)
126 |                 ) {
127 |                     item {
128 |                         Text(
129 |                             text = "${searchResults.size} results",
130 |                             style = MaterialTheme.typography.labelLarge,
131 |                             color = MaterialTheme.colorScheme.onSurfaceVariant
132 |                         )
133 |                     }
134 | 
135 |                     items(searchResults) { item ->
136 |                         SearchResultCard(
137 |                             item = item,
138 |                             onClick = { onItemClick(item) }
139 |                         )
140 |                     }
141 |                 }
142 |             }
143 |         }
144 |     }
145 | }
146 | 
147 | @Composable
148 | private fun SearchFieldCard(
149 |     query: String,
150 |     onQueryChange: (String) -> Unit
151 | ) {
152 |     Card(
153 |         modifier = Modifier
154 |             .fillMaxWidth()
155 |             .padding(horizontal = 16.dp, vertical = 16.dp),
156 |         colors = CardDefaults.cardColors(
157 |             containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
158 |         ),
159 |         border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.26f)),
160 |         shape = RoundedCornerShape(24.dp)
161 |     ) {
162 |         OutlinedTextField(
163 |             value = query,
164 |             onValueChange = onQueryChange,
165 |             modifier = Modifier
166 |                 .fillMaxWidth()
167 |                 .padding(12.dp),
168 |             placeholder = { Text("Search modules, features, risks...") },
169 |             leadingIcon = {
170 |                 Icon(Icons.Default.Search, contentDescription = null)
171 |             },
172 |             trailingIcon = {
173 |                 if (query.isNotEmpty()) {
174 |                     IconButton(onClick = { onQueryChange("") }) {
175 |                         Icon(Icons.Default.Clear, contentDescription = "Clear")
176 |                     }
177 |                 }
178 |             },
179 |             singleLine = true
180 |         )
181 |     }
182 | }
183 | 
184 | @Composable
185 | private fun FilterGroupCard(
186 |     activeFilter: SearchFilter,
187 |     availableSeverities: List<String>,
188 |     onTypeToggle: (SearchItemType) -> Unit,
189 |     onSeverityToggle: (String) -> Unit
190 | ) {
191 |     Card(
192 |         modifier = Modifier
193 |             .fillMaxWidth()
194 |             .padding(horizontal = 16.dp),
195 |         colors = CardDefaults.cardColors(
196 |             containerColor = MaterialTheme.colorScheme.surface
197 |         ),
198 |         border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
199 |         shape = RoundedCornerShape(24.dp)
200 |     ) {
201 |         Column(
202 |             modifier = Modifier
203 |                 .fillMaxWidth()
204 |                 .padding(16.dp),
205 |             verticalArrangement = Arrangement.spacedBy(16.dp)
206 |         ) {
207 |             Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
208 |                 Text(
209 |                     text = "Filter by Type",
210 |                     style = MaterialTheme.typography.labelLarge,
211 |                     color = MaterialTheme.colorScheme.onSurfaceVariant
212 |                 )
213 |                 FlowRow(
214 |                     horizontalArrangement = Arrangement.spacedBy(8.dp),
215 |                     verticalArrangement = Arrangement.spacedBy(8.dp)
216 |                 ) {
217 |                     SearchItemType.values().forEach { type ->
218 |                         FilterChip(
219 |                             selected = activeFilter.types.contains(type),
220 |                             onClick = { onTypeToggle(type) },
221 |                             label = { Text(type.name.replace('_', ' ')) },
222 |                             leadingIcon = {
223 |                                 Icon(
224 |                                     imageVector = getIconForType(type),
225 |                                     contentDescription = null,
226 |                                     modifier = Modifier.size(18.dp)
227 |                                 )
228 |                             }
229 |                         )
230 |                     }
231 |                 }
232 |             }
233 | 
234 |             if (availableSeverities.isNotEmpty()) {
235 |                 Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
236 |                     Text(
237 |                         text = "Filter by Severity",
238 |                         style = MaterialTheme.typography.labelLarge,
239 |                         color = MaterialTheme.colorScheme.onSurfaceVariant
240 |                     )
241 |                     LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
242 |                         items(availableSeverities) { severity ->
243 |                             FilterChip(
244 |                                 selected = activeFilter.severities.contains(severity),
245 |                                 onClick = { onSeverityToggle(severity) },
246 |                                 label = { Text(severity) }
247 |                             )
248 |                         }
249 |                     }
250 |                 }
251 |             }
252 |         }
253 |     }
254 | }
255 | 
256 | @Composable
257 | private fun SearchResultCard(
258 |     item: SearchableItem,
259 |     onClick: () -> Unit
260 | ) {
261 |     Card(
262 |         modifier = Modifier
263 |             .fillMaxWidth()
264 |             .clickable(onClick = onClick),
265 |         colors = CardDefaults.cardColors(
266 |             containerColor = MaterialTheme.colorScheme.surface
267 |         ),
268 |         border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
269 |         shape = MaterialTheme.shapes.extraLarge
270 |     ) {
271 |         Row(
272 |             modifier = Modifier
273 |                 .fillMaxWidth()
274 |                 .padding(18.dp),
275 |             verticalAlignment = Alignment.CenterVertically
276 |         ) {
277 |             Surface(
278 |                 color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
279 |                 shape = MaterialTheme.shapes.large
280 |             ) {
281 |                 Icon(
282 |                     imageVector = getIconForType(item.type),
283 |                     contentDescription = null,
284 |                     modifier = Modifier.padding(10.dp),
285 |                     tint = MaterialTheme.colorScheme.primary
286 |                 )
287 |             }
288 | 
289 |             Spacer(modifier = Modifier.width(14.dp))
290 | 
291 |             Column(
292 |                 modifier = Modifier.weight(1f),
293 |                 verticalArrangement = Arrangement.spacedBy(6.dp)
294 |             ) {
295 |                 Text(
296 |                     text = item.title,
297 |                     style = MaterialTheme.typography.titleLarge,
298 |                     fontWeight = FontWeight.SemiBold,
299 |                     maxLines = 1,
300 |                     overflow = TextOverflow.Ellipsis
301 |                 )
302 |                 Text(
303 |                     text = item.subtitle,
304 |                     style = MaterialTheme.typography.bodyMedium,
305 |                     color = MaterialTheme.colorScheme.onSurfaceVariant,
306 |                     maxLines = 2,
307 |                     overflow = TextOverflow.Ellipsis
308 |                 )
309 |                 FlowRow(
310 |                     horizontalArrangement = Arrangement.spacedBy(8.dp),
311 |                     verticalArrangement = Arrangement.spacedBy(8.dp)
312 |                 ) {
313 |                     SearchMetaChip(item.type.name.replace('_', ' '))
314 |                     item.severity?.let {
315 |                         SearchMetaChip(
316 |                             text = it,
317 |                             containerColor = getSeverityColor(it).copy(alpha = 0.22f),
318 |                             contentColor = getSeverityColor(it)
319 |                         )
320 |                     }
321 |                 }
322 |             }
323 | 
324 |             Icon(
325 |                 imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
326 |                 contentDescription = null,
327 |                 tint = MaterialTheme.colorScheme.onSurfaceVariant
328 |             )
329 |         }
330 |     }
331 | }
332 | 
333 | @Composable
334 | private fun SearchMetaChip(
335 |     text: String,
336 |     containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
337 |     contentColor: Color = MaterialTheme.colorScheme.onSurface
338 | ) {
339 |     Surface(
340 |         color = containerColor,
341 |         shape = MaterialTheme.shapes.large
342 |     ) {
343 |         Text(
344 |             text = text,
345 |             modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
346 |             style = MaterialTheme.typography.labelLarge,
347 |             color = contentColor
348 |         )
349 |     }
350 | }
351 | 
352 | @Composable
353 | private fun EmptySearchState() {
354 |     Column(
355 |         modifier = Modifier
356 |             .fillMaxSize()
357 |             .padding(24.dp),
358 |         horizontalAlignment = Alignment.CenterHorizontally,
359 |         verticalArrangement = Arrangement.Center
360 |     ) {
361 |         Icon(
362 |             imageVector = Icons.Default.SearchOff,
363 |             contentDescription = null,
364 |             modifier = Modifier.size(64.dp),
365 |             tint = MaterialTheme.colorScheme.onSurfaceVariant
366 |         )
367 |         Spacer(modifier = Modifier.height(12.dp))
368 |         Text(
369 |             text = "No results found",
370 |             style = MaterialTheme.typography.titleLarge,
371 |             color = MaterialTheme.colorScheme.onSurfaceVariant
372 |         )
373 |         Spacer(modifier = Modifier.height(6.dp))
374 |         Text(
375 |             text = "Try adjusting your search or filters",
376 |             style = MaterialTheme.typography.bodyMedium,
377 |             color = MaterialTheme.colorScheme.onSurfaceVariant
378 |         )
379 |     }
380 | }
381 | 
382 | private fun getIconForType(type: SearchItemType) = when (type) {
383 |     SearchItemType.MODULE -> Icons.Default.Folder
384 |     SearchItemType.FEATURE -> Icons.Default.Star
385 |     SearchItemType.RISK -> Icons.Default.Warning
386 |     SearchItemType.FILE -> Icons.Default.Description
387 |     SearchItemType.TEST -> Icons.Default.Science
388 |     SearchItemType.ARCHITECTURE_LAYER -> Icons.Default.Layers
389 | }
390 | 
391 | @Composable
392 | private fun getSeverityColor(severity: String) = when (severity.lowercase()) {
393 |     "critical" -> MaterialTheme.colorScheme.error
394 |     "high" -> MaterialTheme.colorScheme.error.copy(alpha = 0.86f)
395 |     "medium" -> MaterialTheme.colorScheme.tertiary
396 |     "low" -> MaterialTheme.colorScheme.secondary
397 |     else -> MaterialTheme.colorScheme.onSurfaceVariant
398 | }
399 | 
400 | // Made with Bob
</content>
</file>
</files>
<environment_details>
# VSCode Visible Files
app\src\main\java\com\vero\repolens\ui\theme\Theme.kt

# VSCode Open Tabs
app/src/main/java/com/vero/repolens/ui/screens/ModulesScreen.kt,app/src/main/java/com/vero/repolens/ui/screens/ModuleDetailScreen.kt,app/src/main/java/com/vero/repolens/ui/screens/FeaturesScreen.kt,app/src/main/java/com/vero/repolens/ui/screens/FeatureDetailScreen.kt,app/src/main/java/com/vero/repolens/data/models/SearchModels.kt,app/src/main/java/com/vero/repolens/ui/screens/SearchScreen.kt,app/src/main/java/com/vero/repolens/viewmodel/SearchViewModel.kt,app/src/main/java/com/vero/repolens/ui/screens/ActionItemsScreen.kt,app/src/main/java/com/vero/repolens/data/export/ExportService.kt,app/src/main/java/com/vero/repolens/data/export/MarkdownGenerator.kt,app/src/main/java/com/vero/repolens/data/export/JsonExporter.kt,app/src/main/java/com/vero/repolens/data/export/PdfGenerator.kt,app/src/main/java/com/vero/repolens/viewmodel/ExportViewModel.kt,app/src/main/java/com/vero/repolens/ui/components/ExportDialog.kt,app/src/main/AndroidManifest.xml,app/src/main/res/xml/file_paths.xml,app/src/main/java/com/vero/repolens/ui/screens/PerformanceScreen.kt,app/src/main/java/com/vero/repolens/data/engine/RecommendationEngine.kt,app/src/main/java/com/vero/repolens/viewmodel/RecommendationsViewModel.kt,app/src/main/java/com/vero/repolens/ui/components/RecommendationCard.kt

# Current Time
Current time in ISO 8601 UTC format: 2026-05-15T20:50:49.986Z
User time zone: Europe/London, UTC+1:00

# Current Cost
$0.13

# Review Exclusions
The following file patterns are excluded from code reviews:
- .vscode/**

# Current Mode
<slug>advanced</slug>
<name>🛠️ Advanced</name>
You have not created a todo list yet. Create one with `update_todo_list` if your task is complicated or involves multiple steps.
</environment_details>

---

**Assistant:**

Now let me search for related code to understand the context better, particularly looking for imports and usage patterns.

<search_files>
<path>app/src/main/java/com/vero/repolens</path>
<regex>import androidx\.compose\.material3\.TextButton</regex>
<file_pattern>*.kt</file_pattern>
</search_files>

---

**User:**

[search_files for 'import androidx\.compose\.material3\.TextButton' in '*.kt'] Result:
Found 3 results.

# app/src/main/java/com/vero/repolens/ui/screens/RecommendationsScreen.kt
 29 | import androidx.compose.material3.Text
 30 | import androidx.compose.material3.TextButton
 31 | import androidx.compose.material3.TopAppBar
----

# app/src/main/java/com/vero/repolens/ui/screens/ActionItemsScreen.kt
 43 | import androidx.compose.material3.Text
 44 | import androidx.compose.material3.TextButton
 45 | import androidx.compose.material3.TopAppBar
----

# app/src/main/java/com/vero/repolens/ui/components/RecommendationCard.kt
 31 | import androidx.compose.material3.Text
 32 | import androidx.compose.material3.TextButton
 33 | import androidx.compose.runtime.Composable
----
<environment_details>
# VSCode Visible Files
app\src\main\java\com\vero\repolens\ui\theme\Theme.kt

# VSCode Open Tabs
app/src/main/java/com/vero/repolens/ui/screens/ModulesScreen.kt,app/src/main/java/com/vero/repolens/ui/screens/ModuleDetailScreen.kt,app/src/main/java/com/vero/repolens/ui/screens/FeaturesScreen.kt,app/src/main/java/com/vero/repolens/ui/screens/FeatureDetailScreen.kt,app/src/main/java/com/vero/repolens/data/models/SearchModels.kt,app/src/main/java/com/vero/repolens/ui/screens/SearchScreen.kt,app/src/main/java/com/vero/repolens/viewmodel/SearchViewModel.kt,app/src/main/java/com/vero/repolens/ui/screens/ActionItemsScreen.kt,app/src/main/java/com/vero/repolens/data/export/ExportService.kt,app/src/main/java/com/vero/repolens/data/export/MarkdownGenerator.kt,app/src/main/java/com/vero/repolens/data/export/JsonExporter.kt,app/src/main/java/com/vero/repolens/data/export/PdfGenerator.kt,app/src/main/java/com/vero/repolens/viewmodel/ExportViewModel.kt,app/src/main/java/com/vero/repolens/ui/components/ExportDialog.kt,app/src/main/AndroidManifest.xml,app/src/main/res/xml/file_paths.xml,app/src/main/java/com/vero/repolens/ui/screens/PerformanceScreen.kt,app/src/main/java/com/vero/repolens/data/engine/RecommendationEngine.kt,app/src/main/java/com/vero/repolens/viewmodel/RecommendationsViewModel.kt,app/src/main/java/com/vero/repolens/ui/components/RecommendationCard.kt

# Current Time
Current time in ISO 8601 UTC format: 2026-05-15T20:50:54.425Z
User time zone: Europe/London, UTC+1:00

# Current Cost
$0.27

# Review Exclusions
The following file patterns are excluded from code reviews:
- .vscode/**

# Current Mode
<slug>advanced</slug>
<name>🛠️ Advanced</name>
You have not created a todo list yet. Create one with `update_todo_list` if your task is complicated or involves multiple steps.
</environment_details>

---

**Assistant:**

<search_files>
<path>app/src/main/java/com/vero/repolens</path>
<regex>FlowRow</regex>
<file_pattern>*.kt</file_pattern>
</search_files>

---

**User:**

[search_files for 'FlowRow' in '*.kt'] Result:
Found 43 results.

# app/src/main/java/com/vero/repolens/ui/screens/SearchScreen.kt
  6 | import androidx.compose.foundation.layout.Column
  7 | import androidx.compose.foundation.layout.FlowRow
  8 | import androidx.compose.foundation.layout.PaddingValues
----
212 |                 )
213 |                 FlowRow(
214 |                     horizontalArrangement = Arrangement.spacedBy(8.dp),
----
308 |                 )
309 |                 FlowRow(
310 |                     horizontalArrangement = Arrangement.spacedBy(8.dp),
----

# app/src/main/java/com/vero/repolens/ui/screens/RisksScreen.kt
  5 | import androidx.compose.foundation.layout.Column
  6 | import androidx.compose.foundation.layout.FlowRow
  7 | import androidx.compose.foundation.layout.PaddingValues
----
127 |             )
128 |             FlowRow(
129 |                 horizontalArrangement = Arrangement.spacedBy(8.dp),
----
229 | 
230 |             FlowRow(
231 |                 horizontalArrangement = Arrangement.spacedBy(8.dp),
----
280 |                     )
281 |                     FlowRow(
282 |                         modifier = Modifier.fillMaxWidth(),
----

# app/src/main/java/com/vero/repolens/ui/screens/RecommendationsScreen.kt
  6 | import androidx.compose.foundation.layout.Column
  7 | import androidx.compose.foundation.layout.FlowRow
  8 | import androidx.compose.foundation.layout.PaddingValues
----
138 | 
139 |                     FlowRow(
140 |                         horizontalArrangement = Arrangement.spacedBy(8.dp),
----
295 |         )
296 |         FlowRow(
297 |             horizontalArrangement = Arrangement.spacedBy(8.dp),
----

# app/src/main/java/com/vero/repolens/ui/screens/OtherScreens.kt
  5 | import androidx.compose.foundation.layout.Column
  6 | import androidx.compose.foundation.layout.FlowRow
  7 | import androidx.compose.foundation.layout.PaddingValues
----
455 |                             if (risk.affectedFiles.isNotEmpty()) {
456 |                                 FlowRow(
457 |                                     modifier = Modifier.fillMaxWidth(),
----
559 |             if (chips.isNotEmpty()) {
560 |                 FlowRow(
561 |                     horizontalArrangement = Arrangement.spacedBy(8.dp),
----
615 |             )
616 |             FlowRow(
617 |                 horizontalArrangement = Arrangement.spacedBy(8.dp),
----
669 |         Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
670 |             FlowRow(
671 |                 horizontalArrangement = Arrangement.spacedBy(8.dp),
----
735 |             if (dispatcher.relatedFiles.isNotEmpty()) {
736 |                 FlowRow(
737 |                     modifier = Modifier.fillMaxWidth(),
----
753 |         Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
754 |             FlowRow(
755 |                 horizontalArrangement = Arrangement.spacedBy(8.dp),
----
778 |         Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
779 |             FlowRow(
780 |                 horizontalArrangement = Arrangement.spacedBy(8.dp),
----
998 | private fun FeatureLikeTagCloud(items: List<String>) {
999 |     FlowRow(
1000 |         modifier = Modifier.fillMaxWidth(),
----

# app/src/main/java/com/vero/repolens/ui/screens/ModulesScreen.kt
  7 | import androidx.compose.foundation.layout.Column
  8 | import androidx.compose.foundation.layout.FlowRow
  9 | import androidx.compose.foundation.layout.PaddingValues
----
120 |             )
121 |             FlowRow(
122 |                 horizontalArrangement = Arrangement.spacedBy(8.dp),
----
223 | 
224 |             FlowRow(
225 |                 horizontalArrangement = Arrangement.spacedBy(8.dp),
----

# app/src/main/java/com/vero/repolens/ui/screens/ModuleDetailScreen.kt
  5 | import androidx.compose.foundation.layout.Column
  6 | import androidx.compose.foundation.layout.FlowRow
  7 | import androidx.compose.foundation.layout.PaddingValues
----
 90 |                     DetailSectionCard(title = "Important Files") {
 91 |                         FlowRow(
 92 |                             modifier = Modifier.fillMaxWidth(),
----
185 | 
186 |             FlowRow(
187 |                 horizontalArrangement = Arrangement.spacedBy(8.dp),
----
211 |     ) {
212 |         FlowRow(
213 |             modifier = Modifier
----
261 | private fun TagList(items: List<String>) {
262 |     FlowRow(
263 |         modifier = Modifier.fillMaxWidth(),
----
276 |         Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
277 |             FlowRow(
278 |                 horizontalArrangement = Arrangement.spacedBy(8.dp),
----
431 |                     )
432 |                     FlowRow(
433 |                         modifier = Modifier.fillMaxWidth(),
----

# app/src/main/java/com/vero/repolens/ui/screens/FeaturesScreen.kt
  6 | import androidx.compose.foundation.layout.Column
  7 | import androidx.compose.foundation.layout.FlowRow
  8 | import androidx.compose.foundation.layout.PaddingValues
----
112 |             )
113 |             FlowRow(
114 |                 horizontalArrangement = Arrangement.spacedBy(8.dp),
----
207 | 
208 |             FlowRow(
209 |                 horizontalArrangement = Arrangement.spacedBy(8.dp),
----

# app/src/main/java/com/vero/repolens/ui/screens/FeatureDetailScreen.kt
  5 | import androidx.compose.foundation.layout.Column
  6 | import androidx.compose.foundation.layout.FlowRow
  7 | import androidx.compose.foundation.layout.PaddingValues
----
220 | 
221 |             FlowRow(
222 |                 horizontalArrangement = Arrangement.spacedBy(8.dp),
----
246 |     ) {
247 |         FlowRow(
248 |             modifier = Modifier
----
299 | private fun FileChipGrid(items: List<String>) {
300 |     FlowRow(
301 |         modifier = Modifier.fillMaxWidth(),
----
328 | private fun FeatureTagCloud(items: List<String>) {
329 |     FlowRow(
330 |         modifier = Modifier.fillMaxWidth(),
----
363 |         Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
364 |             FlowRow(
365 |                 horizontalArrangement = Arrangement.spacedBy(8.dp),
----

# app/src/main/java/com/vero/repolens/ui/screens/ArchitectureScreen.kt
 12 | import androidx.compose.foundation.layout.Column
 13 | import androidx.compose.foundation.layout.FlowRow
 14 | import androidx.compose.foundation.layout.PaddingValues
----
212 |                 )
213 |                 FlowRow(
214 |                     horizontalArrangement = Arrangement.spacedBy(8.dp),
----
353 | 
354 |             FlowRow(
355 |                 horizontalArrangement = Arrangement.spacedBy(8.dp),
----
405 |                 DetailSectionCard(title = "Related Files") {
406 |                     FlowRow(
407 |                         modifier = Modifier.fillMaxWidth(),
----
901 |                         ArchitectureSubsection(title = "Related Files")
902 |                         FlowRow(
903 |                             modifier = Modifier.fillMaxWidth(),
----
<environment_details>
# VSCode Visible Files
app\src\main\java\com\vero\repolens\ui\theme\Theme.kt

# VSCode Open Tabs
app/src/main/java/com/vero/repolens/ui/screens/ModulesScreen.kt,app/src/main/java/com/vero/repolens/ui/screens/ModuleDetailScreen.kt,app/src/main/java/com/vero/repolens/ui/screens/FeaturesScreen.kt,app/src/main/java/com/vero/repolens/ui/screens/FeatureDetailScreen.kt,app/src/main/java/com/vero/repolens/data/models/SearchModels.kt,app/src/main/java/com/vero/repolens/ui/screens/SearchScreen.kt,app/src/main/java/com/vero/repolens/viewmodel/SearchViewModel.kt,app/src/main/java/com/vero/repolens/ui/screens/ActionItemsScreen.kt,app/src/main/java/com/vero/repolens/data/export/ExportService.kt,app/src/main/java/com/vero/repolens/data/export/MarkdownGenerator.kt,app/src/main/java/com/vero/repolens/data/export/JsonExporter.kt,app/src/main/java/com/vero/repolens/data/export/PdfGenerator.kt,app/src/main/java/com/vero/repolens/viewmodel/ExportViewModel.kt,app/src/main/java/com/vero/repolens/ui/components/ExportDialog.kt,app/src/main/AndroidManifest.xml,app/src/main/res/xml/file_paths.xml,app/src/main/java/com/vero/repolens/ui/screens/PerformanceScreen.kt,app/src/main/java/com/vero/repolens/data/engine/RecommendationEngine.kt,app/src/main/java/com/vero/repolens/viewmodel/RecommendationsViewModel.kt,app/src/main/java/com/vero/repolens/ui/components/RecommendationCard.kt

# Current Time
Current time in ISO 8601 UTC format: 2026-05-15T20:50:57.348Z
User time zone: Europe/London, UTC+1:00

# Current Cost
$0.41

# Review Exclusions
The following file patterns are excluded from code reviews:
- .vscode/**

# Current Mode
<slug>advanced</slug>
<name>🛠️ Advanced</name>
You have not created a todo list yet. Create one with `update_todo_list` if your task is complicated or involves multiple steps.
</environment_details>

---

**Assistant:**

<submit_review_findings>
<issues>
[
  {
    "category": "maintainability",
    "type": "missing-documentation",
    "severity": "low",
    "title": "Missing import for TextButton in OverviewScreen",
    "message": "The OverviewScreen.kt file uses TextButton component at line 338 but does not import androidx.compose.material3.TextButton. This will cause a compilation error.",
    "path": "app/src/main/java/com/vero/repolens/ui/screens/OverviewScreen.kt",
    "line": 338,
    "endLine": 347,
    "issueScope": "Single File",
    "suggestion": "Add the missing import: import androidx.compose.material3.TextButton"
  },
  {
    "category": "functionality",
    "type": "edge-case-handling",
    "severity": "medium",
    "title": "Potential null pointer exception in ActionItemBottomSheet",
    "message": "In ActionItemBottomSheet at line 189, the code uses editingItem!! (non-null assertion operator) without proper null safety. While there's a null check at line 187, using !! is risky and could lead to crashes if the state changes unexpectedly between the check and usage.",
    "path": "app/src/main/java/com/vero/repolens/ui/screens/ActionItemsScreen.kt",
    "line": 189,
    "endLine": 189,
    "issueScope": "Single File",
    "suggestion": "Use safe call operator with let block instead: editingItem?.let { item -> viewModel.updateActionItem(item.copy(...)) }"
  },
  {
    "category": "maintainability",
    "type": "magic-numbers-strings",
    "severity": "low",
    "title": "Magic number for summary length threshold",
    "message": "The value 140 at line 337 in OverviewScreen.kt is used as a threshold for showing 'Read more' button but lacks context. This magic number should be extracted to a named constant for better maintainability.",
    "path": "app/src/main/java/com/vero/repolens/ui/screens/OverviewScreen.kt",
    "line": 337,
    "endLine": 337,
    "issueScope": "Single File",
    "suggestion": "Extract to a constant: private const val SUMMARY_EXPAND_THRESHOLD = 140"
  },
  {
    "category": "performance",
    "type": "unnecessary-computation",
    "severity": "low",
    "title": "Redundant toList() conversion in SearchScreen",
    "message": "At line 113 in SearchScreen.kt, availableSeverities.toList() is called but availableSeverities is already collected as a StateFlow. The toList() conversion is unnecessary if the StateFlow already emits a List type.",
    "path": "app/src/main/java/com/vero/repolens/ui/screens/SearchScreen.kt",
    "line": 113,
    "endLine": 113,
    "issueScope": "Single File",
    "suggestion": "Remove .toList() if availableSeverities already emits a List, or verify the actual type from the ViewModel"
  }
]
</issues>
</submit_review_findings>

---

**User:**

[submit_review_findings({"issues":"[\n  {\n    \"category\": \"maintainability\",\n    \"type\": \"missing-documentation\",\n    \"severity\": \"low\",\n    \"title\": \"Missing import for TextButton in OverviewScreen\",\n    \"message\": \"The OverviewScreen.kt file uses TextButton component at line 338 but does not import androidx.compose.material3.TextButton. This will cause a compilation error.\",\n    \"path\": \"app/src/main/java/com/vero/repolens/ui/screens/OverviewScreen.kt\",\n    \"line\": 338,\n    \"endLine\": 347,\n    \"issueScope\": \"Single File\",\n    \"suggestion\": \"Add the missing import: import androidx.compose.material3.TextButton\"\n  },\n  {\n    \"category\": \"functionality\",\n    \"type\": \"edge-case-handling\",\n    \"severity\": \"medium\",\n    \"title\": \"Potential null pointer exception in ActionItemBottomSheet\",\n    \"message\": \"In ActionItemBottomSheet at line 189, the code uses editingItem!! (non-null assertion operator) without proper null safety. While there's a null check at line 187, using !! is risky and could lead to crashes if the state changes unexpectedly between the check and usage.\",\n    \"path\": \"app/src/main/java/com/vero/repolens/ui/screens/ActionItemsScreen.kt\",\n    \"line\": 189,\n    \"endLine\": 189,\n    \"issueScope\": \"Single File\",\n    \"suggestion\": \"Use safe call operator with let block instead: editingItem?.let { item -> viewModel.updateActionItem(item.copy(...)) }\"\n  },\n  {\n    \"category\": \"maintainability\",\n    \"type\": \"magic-numbers-strings\",\n    \"severity\": \"low\",\n    \"title\": \"Magic number for summary length threshold\",\n    \"message\": \"The value 140 at line 337 in OverviewScreen.kt is used as a threshold for showing 'Read more' button but lacks context. This magic number should be extracted to a named constant for better maintainability.\",\n    \"path\": \"app/src/main/java/com/vero/repolens/ui/screens/OverviewScreen.kt\",\n    \"line\": 337,\n    \"endLine\": 337,\n    \"issueScope\": \"Single File\",\n    \"suggestion\": \"Extract to a constant: private const val SUMMARY_EXPAND_THRESHOLD = 140\"\n  },\n  {\n    \"category\": \"performance\",\n    \"type\": \"unnecessary-computation\",\n    \"severity\": \"low\",\n    \"title\": \"Redundant toList() conversion in SearchScreen\",\n    \"message\": \"At line 113 in SearchScreen.kt, availableSeverities.toList() is called but availableSeverities is already collected as a StateFlow. The toList() conversion is unnecessary if the StateFlow already emits a List type.\",\n    \"path\": \"app/src/main/java/com/vero/repolens/ui/screens/SearchScreen.kt\",\n    \"line\": 113,\n    \"endLine\": 113,\n    \"issueScope\": \"Single File\",\n    \"suggestion\": \"Remove .toList() if availableSeverities already emits a List, or verify the actual type from the ViewModel\"\n  }\n]"})] Result:
**4 Review Issues Created**

low **Missing import for TextButton in OverviewScreen** `@issue-maintainability-missing-documentation-app/src/main/java/com/vero/repolens/ui/screens/OverviewScreen.kt-338-1778878272058`
app/src/main/java/com/vero/repolens/ui/screens/OverviewScreen.kt:337-346
maintainability | missing-documentation | low

medium **Potential null pointer exception in ActionItemBottomSheet** `@issue-functionality-edge-case-handling-app/src/main/java/com/vero/repolens/ui/screens/ActionItemsScreen.kt-189-1778878272059`
app/src/main/java/com/vero/repolens/ui/screens/ActionItemsScreen.kt:188
functionality | edge-case-handling | medium

low **Magic number for summary length threshold** `@issue-maintainability-magic-numbers-strings-app/src/main/java/com/vero/repolens/ui/screens/OverviewScreen.kt-337-1778878272059`
app/src/main/java/com/vero/repolens/ui/screens/OverviewScreen.kt:336
maintainability | magic-numbers-strings | low

low **Redundant toList() conversion in SearchScreen** `@issue-performance-unnecessary-computation-app/src/main/java/com/vero/repolens/ui/screens/SearchScreen.kt-113-1778878272059`
app/src/main/java/com/vero/repolens/ui/screens/SearchScreen.kt:112
performance | unnecessary-computation | low

All 4 issues have been added to review findings

📋 Review the issues in the Bob findings panel. *Mention its been added to bob findings*
<environment_details>
# VSCode Visible Files
app\src\main\java\com\vero\repolens\ui\theme\Theme.kt

# VSCode Open Tabs
app/src/main/java/com/vero/repolens/ui/screens/ModulesScreen.kt,app/src/main/java/com/vero/repolens/ui/screens/ModuleDetailScreen.kt,app/src/main/java/com/vero/repolens/ui/screens/FeaturesScreen.kt,app/src/main/java/com/vero/repolens/ui/screens/FeatureDetailScreen.kt,app/src/main/java/com/vero/repolens/data/models/SearchModels.kt,app/src/main/java/com/vero/repolens/ui/screens/SearchScreen.kt,app/src/main/java/com/vero/repolens/viewmodel/SearchViewModel.kt,app/src/main/java/com/vero/repolens/ui/screens/ActionItemsScreen.kt,app/src/main/java/com/vero/repolens/data/export/ExportService.kt,app/src/main/java/com/vero/repolens/data/export/MarkdownGenerator.kt,app/src/main/java/com/vero/repolens/data/export/JsonExporter.kt,app/src/main/java/com/vero/repolens/data/export/PdfGenerator.kt,app/src/main/java/com/vero/repolens/viewmodel/ExportViewModel.kt,app/src/main/java/com/vero/repolens/ui/components/ExportDialog.kt,app/src/main/AndroidManifest.xml,app/src/main/res/xml/file_paths.xml,app/src/main/java/com/vero/repolens/ui/screens/PerformanceScreen.kt,app/src/main/java/com/vero/repolens/data/engine/RecommendationEngine.kt,app/src/main/java/com/vero/repolens/viewmodel/RecommendationsViewModel.kt,app/src/main/java/com/vero/repolens/ui/components/RecommendationCard.kt

# Current Time
Current time in ISO 8601 UTC format: 2026-05-15T20:51:12.283Z
User time zone: Europe/London, UTC+1:00

# Current Cost
$0.56

# Review Exclusions
The following file patterns are excluded from code reviews:
- .vscode/**

# Current Mode
<slug>advanced</slug>
<name>🛠️ Advanced</name>
You have not created a todo list yet. Create one with `update_todo_list` if your task is complicated or involves multiple steps.
</environment_details>

---

**Assistant:**

<attempt_completion>
<result>
Review complete. 4 issues identified in the UI component updates:

- **1 medium severity**: Potential null pointer exception using !! operator in ActionItemsScreen
- **3 low severity**: Missing import, magic number, and redundant conversion

All findings have been added to the Bob Findings panel. Detailed explanations are available upon request.
</result>
</attempt_completion>

