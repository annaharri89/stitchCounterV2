package dev.harrisonsoftware.stitchCounter.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.domain.model.AppTheme
import dev.harrisonsoftware.stitchCounter.feature.navigation.RootNavGraph
import dev.harrisonsoftware.stitchCounter.feature.theme.ThemeColor
import com.ramcosta.composedestinations.annotation.Destination
import androidx.core.net.toUri
import kotlinx.coroutines.flow.takeWhile
import dev.harrisonsoftware.stitchCounter.Constants

@RootNavGraph
@Destination
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportLibrary(it) }
    }
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importLibrary(it) }
    }
    
    val context = LocalContext.current
    val showImportDialog = remember { mutableStateOf(false) }
    val isThemeSectionExpanded = remember { mutableStateOf(true) }
    val isBackupSectionExpanded = remember { mutableStateOf(true) }
    val isSupportSectionExpanded = remember { mutableStateOf(true) }
    val isLegalSectionExpanded = remember { mutableStateOf(true) }
    val lazyListState = rememberLazyListState()
    
    LaunchedEffect(uiState.exportSuccess) {
        if (uiState.exportSuccess) {
            viewModel.clearExportStatus()
        }
    }
    
    LaunchedEffect(uiState.importSuccess) {
        if (uiState.importSuccess) {
            showImportDialog.value = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SettingsEffect.OpenEmailClient -> {
                    val encodedSubject = Uri.encode(effect.subject)
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "mailto:${Constants.SUPPORT_EMAIL}?subject=$encodedSubject".toUri()
                    }
                    context.startActivity(intent)
                }
                is SettingsEffect.OpenPrivacyPolicy -> {
                    val browserIntent = Intent(Intent.ACTION_VIEW,
                        Constants.PRIVACY_POLICY_URL.toUri())
                    context.startActivity(browserIntent)
                }
                is SettingsEffect.OpenEULA -> {
                    val browserIntent = Intent(Intent.ACTION_VIEW,
                        Constants.EULA_URL.toUri())
                    context.startActivity(browserIntent)
                }
            }
        }
    }

    ScrollToRevealExpandedItem(isThemeSectionExpanded.value, 0, lazyListState)
    ScrollToRevealExpandedItem(isBackupSectionExpanded.value, 1, lazyListState)
    ScrollToRevealExpandedItem(isSupportSectionExpanded.value, 2, lazyListState)
    ScrollToRevealExpandedItem(isLegalSectionExpanded.value, 3, lazyListState)

    Surface {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ExpandableSection(
                    title = stringResource(R.string.settings_theme),
                    isExpanded = isThemeSectionExpanded.value,
                    onToggleExpanded = { isThemeSectionExpanded.value = !isThemeSectionExpanded.value }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = stringResource(R.string.settings_choose_color_scheme),
                            style = MaterialTheme.typography.titleMedium
                        )

                        AppTheme.entries.forEach { theme ->
                            ThemeOptionCard(
                                theme = theme,
                                isSelected = uiState.selectedTheme == theme,
                                themeColors = if (uiState.selectedTheme == theme) uiState.themeColors else emptyList(),
                                onThemeSelected = { viewModel.onThemeSelected(theme) }
                            )
                        }
                    }
                }
            }

            item {
                ExpandableSection(
                    title = stringResource(R.string.settings_backup_restore),
                    isExpanded = isBackupSectionExpanded.value,
                    onToggleExpanded = { isBackupSectionExpanded.value = !isBackupSectionExpanded.value }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = stringResource(R.string.settings_export_import_library),
                            style = MaterialTheme.typography.titleMedium
                        )

                        BackupRestoreCard(
                            isExporting = uiState.isExporting,
                            isImporting = uiState.isImporting,
                            exportError = uiState.exportError,
                            importError = uiState.importError,
                            onExport = {
                                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
                                    .format(java.util.Date())
                                exportLauncher.launch("stitch_counter_backup_$timestamp.zip")
                            },
                            onImport = { importLauncher.launch("application/zip") }
                        )
                    }
                }
            }

            item {
                ExpandableSection(
                    title = stringResource(R.string.settings_support),
                    isExpanded = isSupportSectionExpanded.value,
                    onToggleExpanded = { isSupportSectionExpanded.value = !isSupportSectionExpanded.value }
                ) {
                    SupportCard(
                        onReportBug = {
                            viewModel.onReportBug()
                        },
                        onGiveFeedback = {
                            viewModel.onGiveFeedback()
                        },
                        onRequestFeature = {
                            viewModel.onRequestFeature()
                        }
                    )
                }
            }

            item {
                ExpandableSection(
                    title = stringResource(R.string.settings_privacy_legal),
                    isExpanded = isLegalSectionExpanded.value,
                    onToggleExpanded = { isLegalSectionExpanded.value = !isLegalSectionExpanded.value }
                ) {
                    LegalCard(
                        onOpenPrivacyPolicy = {
                            viewModel.onOpenPrivacyPolicy()
                        },
                        onOpenEULA = {
                            viewModel.onOpenEULA()
                        }
                    )
                }
            }
        }

        if (showImportDialog.value) {
            uiState.importResult?.let {

                ImportResultDialog(
                    result = it,
                    onDismiss = {
                        showImportDialog.value = false
                        viewModel.clearImportStatus()
                    }
                )
            }
        }
    }
}

@Composable
private fun ScrollToRevealExpandedItem(
    isExpanded: Boolean,
    itemIndex: Int,
    lazyListState: LazyListState
) {
    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            val trackingStartTime = System.currentTimeMillis()
            snapshotFlow { lazyListState.layoutInfo }
                .takeWhile { System.currentTimeMillis() - trackingStartTime < 500L }
                .collect { layoutInfo ->
                    val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == itemIndex }
                    if (itemInfo != null) {
                        val overflowBeyondViewport = (itemInfo.offset + itemInfo.size) - layoutInfo.viewportEndOffset
                        if (overflowBeyondViewport > 0) {
                            lazyListState.scrollBy(overflowBeyondViewport.toFloat())
                        }
                    }
                }
        }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { onToggleExpanded() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.semantics { heading() }
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) stringResource(R.string.cd_collapse) else stringResource(R.string.cd_expand)
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun ThemeIconPreview(
    theme: AppTheme,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundResourceId = when (theme) {
        AppTheme.SEA_COTTAGE -> R.drawable.ic_launcher_background_sea_cottage
        AppTheme.RETRO_SUMMER -> R.drawable.ic_launcher_background_retro_summer
        AppTheme.GOLDEN_HEARTH -> R.drawable.ic_launcher_background_golden_hearth
        AppTheme.FOREST_FIBER -> R.drawable.ic_launcher_background_forest_fiber
        AppTheme.CLOUD_SOFT -> R.drawable.ic_launcher_background_cloud_soft
        AppTheme.YARN_CANDY -> R.drawable.ic_launcher_background_yarn_candy
        AppTheme.DUSTY_ROSE -> R.drawable.ic_launcher_background_dusty_rose
    }

    val foregroundResourceId = when (theme) {
        AppTheme.SEA_COTTAGE -> R.drawable.ic_yarn_sea_cottage
        AppTheme.RETRO_SUMMER -> R.drawable.ic_yarn_retro_summer
        AppTheme.GOLDEN_HEARTH -> R.drawable.ic_yarn_golden_hearth
        AppTheme.FOREST_FIBER -> R.drawable.ic_yarn_forest_fiber
        AppTheme.CLOUD_SOFT -> R.drawable.ic_yarn_cloud_soft
        AppTheme.YARN_CANDY -> R.drawable.ic_yarn_yarn_candy
        AppTheme.DUSTY_ROSE -> R.drawable.ic_yarn_dusty_rose
    }

    val themeIconDescription = stringResource(R.string.cd_theme_icon, theme.displayName)

    val iconShape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .size(48.dp)
            .then(
                if (isSelected) Modifier.shadow(
                    elevation = 12.dp,
                    shape = iconShape,
                    ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ) else Modifier
            )
            .clip(iconShape)
            .semantics { contentDescription = themeIconDescription }
    ) {
        Image(
            painter = painterResource(id = backgroundResourceId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Image(
            painter = painterResource(id = foregroundResourceId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun ThemeOptionCard(
    theme: AppTheme,
    isSelected: Boolean,
    themeColors: List<ThemeColor>,
    onThemeSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onThemeSelected
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ThemeIconPreview(theme = theme, isSelected = isSelected)
                    Text(
                        text = theme.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                RadioButton(
                    selected = isSelected,
                    onClick = onThemeSelected
                )
            }
            
            if (isSelected && themeColors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.settings_colors_in_theme),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    themeColors.forEach { themeColor ->
                        ColorItem(themeColor = themeColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorItem(themeColor: ThemeColor) {
    val lightSwatchDescription = stringResource(R.string.cd_color_swatch_light, themeColor.name)
    val darkSwatchDescription = stringResource(R.string.cd_color_swatch_dark, themeColor.name)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(themeColor.lightColor)
                .semantics { contentDescription = lightSwatchDescription }
        )
        
        Text(
            text = stringResource(R.string.settings_color_light_format, themeColor.name),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(themeColor.darkColor)
                .semantics { contentDescription = darkSwatchDescription }
        )
        
        Text(
            text = stringResource(R.string.settings_dark),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BackupRestoreCard(
    isExporting: Boolean,
    isImporting: Boolean,
    exportError: String?,
    importError: String?,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onExport,
                enabled = !isExporting && !isImporting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isExporting) stringResource(R.string.settings_exporting)
                    else stringResource(R.string.settings_export_library)
                )
            }
            
            if (exportError != null) {
                Text(
                    text = stringResource(R.string.settings_error_format, exportError),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Button(
                onClick = onImport,
                enabled = !isExporting && !isImporting,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isImporting) stringResource(R.string.settings_importing)
                    else stringResource(R.string.settings_import_library)
                )
            }
            
            if (importError != null) {
                Text(
                    text = stringResource(R.string.settings_error_format, importError),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun SupportCard(
    onReportBug: () -> Unit,
    onGiveFeedback: () -> Unit,
    onRequestFeature: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onReportBug,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.settings_report_bug))
            }

            Button(
                onClick = onGiveFeedback,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Feedback,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.settings_give_feedback))
            }

            Button(
                onClick = onRequestFeature,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.settings_request_feature))
            }
        }
    }
}

@Composable
private fun LegalCard(
    onOpenPrivacyPolicy: () -> Unit,
    onOpenEULA: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Button(
                onClick = onOpenPrivacyPolicy,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Policy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.settings_privacy_policy))
            }

            Button(
                onClick = onOpenEULA,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.LocalPolice,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.settings_eula))
            }

            Text(stringResource(R.string.settings_eula_description))
        }
    }
}

@Composable
private fun ImportResultDialog(
    result: dev.harrisonsoftware.stitchCounter.domain.usecase.ImportResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_import_complete)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.settings_imported_count, result.importedCount))
                if (result.failedCount > 0) {
                    Text(
                        stringResource(R.string.settings_failed_import_count, result.failedCount),
                        color = MaterialTheme.colorScheme.error
                    )
                    if (result.failedProjectNames.isNotEmpty()) {
                        Text(
                            result.failedProjectNames.joinToString("\n"),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_ok))
            }
        }
    )
}
