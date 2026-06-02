package dev.harrisonsoftware.stitchCounter.feature.settings

import android.content.Intent
import android.content.ClipData
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.domain.model.AppTheme
import dev.harrisonsoftware.stitchCounter.feature.navigation.RootNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import androidx.core.net.toUri
import androidx.core.content.FileProvider
import dev.harrisonsoftware.stitchCounter.Constants
import java.io.File
import timber.log.Timber

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
    val showBugReportDialog = remember { mutableStateOf(false) }
    val includeDiagnosticsForCurrentBugReport = rememberSaveable { mutableStateOf(true) }
    val settingsSections = remember { SettingsScreenSection.entries.toList() }
    val expandedSectionNames = rememberSaveable { mutableStateOf(emptyList<String>()) }
    val lazyListState = rememberLazyListState()

    fun isSectionExpanded(section: SettingsScreenSection): Boolean {
        return section.name in expandedSectionNames.value
    }

    fun updateSectionExpanded(section: SettingsScreenSection, isExpanded: Boolean) {
        expandedSectionNames.value = updatedExpandedSettingsSectionNames(
            expandedSectionNames = expandedSectionNames.value,
            sectionName = section.name,
            isExpanded = isExpanded
        )
    }
    
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
                    viewModel.onLaunchingExternalActivity()
                    val encodedSubject = Uri.encode(effect.subject)
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "mailto:${Constants.SUPPORT_EMAIL}?subject=$encodedSubject".toUri()
                    }
                    launchExternalActivitySafely(context, intent)
                }
                is SettingsEffect.OpenBugReportShare -> {
                    viewModel.onLaunchingExternalActivity()
                    val attachmentPath = effect.attachmentFilePath
                    val launched = if (attachmentPath.isNullOrBlank()) {
                        launchMailtoIntent(context, effect.subject)
                    } else {
                        launchBugReportWithAttachment(context, effect.subject, attachmentPath)
                    }

                    if (!launched && !attachmentPath.isNullOrBlank()) {
                        launchMailtoIntent(context, effect.subject)
                    }
                }
                is SettingsEffect.OpenPrivacyPolicy -> {
                    viewModel.onLaunchingExternalActivity()
                    val browserIntent = Intent(Intent.ACTION_VIEW,
                        Constants.PRIVACY_POLICY_URL.toUri())
                    launchExternalActivitySafely(context, browserIntent)
                }
                is SettingsEffect.OpenEULA -> {
                    viewModel.onLaunchingExternalActivity()
                    val browserIntent = Intent(Intent.ACTION_VIEW,
                        Constants.EULA_URL.toUri())
                    launchExternalActivitySafely(context, browserIntent)
                }
            }
        }
    }

    settingsSections.forEachIndexed { index, section ->
        ScrollToRevealExpandedItem(isSectionExpanded(section), index, lazyListState)
    }

    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(top = 16.dp, bottom = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = settingsSections,
                    key = { sectionType -> sectionType.name }
                ) { sectionType ->
                SettingsSection(
                    sectionName = stringResource(sectionType.titleId),
                    isExpanded = isSectionExpanded(sectionType),
                    updateExpanded = { newIsExpanded ->
                        updateSectionExpanded(sectionType, newIsExpanded)
                    }
                ) {
                    when (sectionType) {
                        SettingsScreenSection.THEME -> {
                            ThemeCard(
                                themeColors = uiState.themeColors,
                                selectedTheme = uiState.selectedTheme,
                                updateTheme = { newTheme ->
                                    viewModel.onThemeSelected(newTheme)
                            })
                        }
                        SettingsScreenSection.SETTINGS -> {
                            SettingsCard(
                                forceDarkMode = uiState.forceDarkMode,
                                onForceDarkModeChange = viewModel::onForceDarkModeChanged,
                                forceLightMode = uiState.forceLightMode,
                                onForceLightModeChange = viewModel::onForceLightModeChanged,
                                forceCounterScreensOn = uiState.forceCounterScreensOn,
                                onForceCounterScreensOnChange = viewModel::onForceCounterScreensOnChanged
                            )
                        }
                        SettingsScreenSection.BACKUP_RESTORE -> {
                            BackupRestoreCard(
                                isExporting = uiState.isExporting,
                                isImporting = uiState.isImporting,
                                exportError = uiState.exportError,
                                importError = uiState.importError,
                                onExport = {
                                    viewModel.onLaunchingExternalActivity()
                                    val timestamp = java.text.SimpleDateFormat(
                                        "yyyyMMdd_HHmmss",
                                        java.util.Locale.US
                                    )
                                        .format(java.util.Date())
                                    exportLauncher.launch("stitch_counter_backup_$timestamp.zip")
                                },
                                onImport = {
                                    viewModel.onLaunchingExternalActivity()
                                    importLauncher.launch("application/zip")
                                }
                            )
                        }
                        SettingsScreenSection.SUPPORT -> {
                            SupportCard(
                                onReportBug = {
                                    includeDiagnosticsForCurrentBugReport.value = true
                                    showBugReportDialog.value = true
                                },
                                onGiveFeedback = {
                                    viewModel.onGiveFeedback()
                                },
                                onRequestFeature = {
                                    viewModel.onRequestFeature()
                                }
                            )
                        }
                        SettingsScreenSection.LEGAL -> {
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
            }
            }

            SettingsAppVersionLabel(appVersion = uiState.appVersion)
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

        if (showBugReportDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    showBugReportDialog.value = false
                },
                title = {
                    Text(stringResource(R.string.settings_bug_report_dialog_title))
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.settings_bug_report_dialog_message))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = includeDiagnosticsForCurrentBugReport.value,
                                onCheckedChange = { isChecked ->
                                    includeDiagnosticsForCurrentBugReport.value = isChecked
                                }
                            )
                            Text(
                                text = stringResource(R.string.settings_bug_report_dialog_include_diagnostics),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showBugReportDialog.value = false
                            viewModel.onReportBug(includeDiagnosticsForCurrentBugReport.value)
                        }
                    ) {
                        Text(stringResource(R.string.settings_bug_report_dialog_send))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBugReportDialog.value = false }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        }
    }
}

private enum class SettingsScreenSection(@param:StringRes val titleId: Int) {
    THEME(R.string.settings_theme),
    SETTINGS(R.string.nav_settings),
    BACKUP_RESTORE(R.string.settings_backup_restore),
    SUPPORT(R.string.settings_support),
    LEGAL(R.string.settings_privacy_legal)
}

internal fun updatedExpandedSettingsSectionNames(
    expandedSectionNames: List<String>,
    sectionName: String,
    isExpanded: Boolean
): List<String> {
    return if (isExpanded) {
        if (sectionName in expandedSectionNames) {
            expandedSectionNames
        } else {
            expandedSectionNames + sectionName
        }
    } else {
        expandedSectionNames - sectionName
    }
}

internal fun launchExternalActivitySafely(context: android.content.Context, intent: Intent): Boolean {
    return runCatching { context.startActivity(intent) }
        .fold(
            onSuccess = { true },
            onFailure = { throwable ->
                runCatching {
                    Timber.tag(Constants.LOG_TAG_SETTINGS_SCREEN).w(throwable, "Failed to launch external activity")
                }
                false
            }
        )
}

private fun launchMailtoIntent(context: android.content.Context, subject: String): Boolean {
    val encodedSubject = Uri.encode(subject)
    val mailtoIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:${Constants.SUPPORT_EMAIL}?subject=$encodedSubject".toUri()
    }
    return launchExternalActivitySafely(context, mailtoIntent)
}

private fun launchBugReportWithAttachment(
    context: android.content.Context,
    subject: String,
    attachmentFilePath: String
): Boolean {
    return runCatching {
        val attachmentFile = File(attachmentFilePath)
        val attachmentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            attachmentFile
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(Constants.SUPPORT_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_STREAM, attachmentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(context.contentResolver, "stitch_diagnostics_zip", attachmentUri)
        }
        val chooserIntent = Intent.createChooser(sendIntent, null)
        launchExternalActivitySafely(context, chooserIntent)
    }.getOrElse { throwable ->
        runCatching {
            Timber.tag(Constants.LOG_TAG_SETTINGS_SCREEN).w(throwable, "Failed to launch bug report with attachment")
        }
        false
    }
}

@Composable
private fun SettingsAppVersionLabel(
    appVersion: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.settings_app_version, appVersion),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun SettingsSection(
    sectionName: String,
    isExpanded: Boolean,
    updateExpanded: (isExpanded: Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    ExpandableSection(
        title = sectionName,
        isExpanded = isExpanded,
        onToggleExpanded = { updateExpanded(!isExpanded) },
        content = content
    )
}
