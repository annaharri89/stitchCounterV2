package dev.harrisonsoftware.stitchCounter.feature.settings

import android.content.Intent
import android.content.ClipData
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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

private const val SETTINGS_SCREEN_LOG_TAG = "SettingsScreen"

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
    val isThemeSectionExpanded = remember { mutableStateOf(false) }
    val isBackupSectionExpanded = remember { mutableStateOf(false) }
    val isSupportSectionExpanded = remember { mutableStateOf(false) }
    val isLegalSectionExpanded = remember { mutableStateOf(false) }
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

    ScrollToRevealExpandedItem(isBackupSectionExpanded.value, 1, lazyListState)
    ScrollToRevealExpandedItem(isSupportSectionExpanded.value, 2, lazyListState)
    ScrollToRevealExpandedItem(isLegalSectionExpanded.value, 3, lazyListState)

    Surface {
        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(vertical = 16.dp),
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
                                viewModel.onLaunchingExternalActivity()
                                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
                                    .format(java.util.Date())
                                exportLauncher.launch("stitch_counter_backup_$timestamp.zip")
                            },
                            onImport = {
                                viewModel.onLaunchingExternalActivity()
                                importLauncher.launch("application/zip")
                            }
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
                        attachDiagnosticsToBugReport = uiState.attachDiagnosticsToBugReport,
                        onAttachDiagnosticsToBugReportChanged = { isEnabled ->
                            viewModel.onAttachDiagnosticsToBugReportChanged(isEnabled)
                        },
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

internal fun launchExternalActivitySafely(context: android.content.Context, intent: Intent): Boolean {
    return runCatching { context.startActivity(intent) }
        .fold(
            onSuccess = { true },
            onFailure = { throwable ->
                runCatching {
                    Log.w(SETTINGS_SCREEN_LOG_TAG, "Failed to launch external activity", throwable)
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
            Log.w(SETTINGS_SCREEN_LOG_TAG, "Failed to launch bug report with attachment", throwable)
        }
        false
    }
}
