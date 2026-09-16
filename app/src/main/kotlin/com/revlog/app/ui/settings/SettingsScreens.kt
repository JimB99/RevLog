package com.revlog.app.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.revlog.app.BuildConfig
import com.revlog.app.R
import com.revlog.app.share.RevLogShare
import com.revlog.app.ui.viewmodel.ExportViewModel
import com.revlog.app.ui.viewmodel.ImportViewModel
import com.revlog.app.ui.viewmodel.SettingsViewModel
import com.revlog.app.util.LocaleController
import com.revlog.domain.model.ImportConflictMode
import com.revlog.domain.model.VehicleBundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHubScreen(
    onBack: () -> Unit,
    onNavigateLanguage: () -> Unit,
    onNavigateExport: () -> Unit,
    onNavigateImportReview: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    importViewModel: ImportViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
            if (json != null) {
                importViewModel.parseImport(json)
                onNavigateImportReview()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.language)) },
                modifier = Modifier.clickable(onClick = onNavigateLanguage),
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.export_data)) },
                modifier = Modifier.clickable(onClick = onNavigateExport),
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.import_data)) },
                modifier = Modifier.clickable {
                    importLauncher.launch(arrayOf("application/vnd.revlog+json", "application/json", "*/*"))
                },
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.about)) },
                supportingContent = {
                    Text(stringResource(R.string.version, BuildConfig.VERSION_NAME))
                },
            )
        }
    }
}

@Composable
fun LanguageSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsState()
    val scope = rememberCoroutineScope()

    SettingsDetailScaffold(
        title = stringResource(R.string.language),
        onBack = onBack,
    ) {
        LocaleController.supportedTags.forEach { tag ->
            val selected = settings.languageTag == tag
            ListItem(
                headlineContent = {
                    Text("${LocaleController.flagForTag(tag)} ${LocaleController.nativeLabelForTag(tag)}")
                },
                leadingContent = {
                    RadioButton(selected = selected, onClick = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !selected) {
                        scope.launch {
                            viewModel.setLanguage(tag)
                            LocaleController.apply(tag)
                        }
                    },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onBack: () -> Unit,
    preselectedIds: List<Long> = emptyList(),
    viewModel: ExportViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val vehicles by viewModel.vehicles.collectAsState()
    var selected by remember { mutableStateOf(setOf<Long>()) }
    LaunchedEffect(vehicles, preselectedIds) {
        selected = when {
            preselectedIds.isNotEmpty() -> preselectedIds.toSet()
            selected.isEmpty() && vehicles.isNotEmpty() -> vehicles.map { it.id }.toSet()
            else -> selected
        }
    }
    val scope = rememberCoroutineScope()
    var showActions by remember { mutableStateOf(false) }
    var exportFile by remember { mutableStateOf<java.io.File?>(null) }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val uri = result.data?.data
        val file = exportFile
        if (uri != null && file != null) {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                file.inputStream().use { input -> input.copyTo(out) }
            }
        }
        showActions = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.export_data)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.export_select_vehicles), style = MaterialTheme.typography.titleMedium)
            vehicles.forEach { vehicle ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selected = if (vehicle.id in selected) {
                                selected - vehicle.id
                            } else {
                                selected + vehicle.id
                            }
                        },
                ) {
                    Checkbox(
                        checked = vehicle.id in selected,
                        onCheckedChange = { checked ->
                            selected = if (checked) selected + vehicle.id else selected - vehicle.id
                        },
                    )
                    Text(vehicle.name)
                }
            }
            TextButton(onClick = { selected = vehicles.map { it.id }.toSet() }) {
                Text(stringResource(R.string.export_all))
            }
            Button(
                onClick = {
                    scope.launch {
                        val ids = selected.toList()
                        if (ids.isEmpty()) return@launch
                        val json = viewModel.exportJson(ids)
                        val names = vehicles.filter { it.id in ids }.map { it.name }
                        val file = withContext(Dispatchers.IO) {
                            RevLogShare.writeExportFile(
                                context,
                                json,
                                RevLogShare.buildFilename(names),
                            )
                        }
                        exportFile = file
                        showActions = true
                    }
                },
                enabled = selected.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.export_data)) }
        }
    }

    if (showActions && exportFile != null) {
        val file = exportFile!!
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showActions = false },
            title = { Text(stringResource(R.string.export_data)) },
            text = { Text(file.name) },
            confirmButton = {
                TextButton(onClick = {
                    RevLogShare.shareFile(context, file)
                    showActions = false
                }) { Text(stringResource(R.string.share)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "application/vnd.revlog+json"
                        putExtra(Intent.EXTRA_TITLE, file.name)
                    }
                    saveLauncher.launch(intent)
                }) { Text(stringResource(R.string.save_file)) }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportReviewScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: ImportViewModel = hiltViewModel(),
) {
    val bundles by viewModel.pendingBundles.collectAsState()
    val error by viewModel.error.collectAsState()
    var conflictMode by remember { mutableStateOf(ImportConflictMode.ADD_NEW) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.import_review_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when {
                error != null -> Text(stringResource(R.string.import_error_invalid))
                bundles.isEmpty() -> Text(stringResource(R.string.import_error_invalid))
                else -> {
                    Text(stringResource(R.string.import_review_summary, bundles.size))
                    bundles.forEach { bundle: VehicleBundle ->
                        ListItem(
                            headlineContent = { Text(bundle.vehicle.name) },
                            supportingContent = {
                                Text(bundle.data.licensePlate ?: stringResource(R.string.empty))
                            },
                        )
                    }
                    ConflictOption(
                        selected = conflictMode == ImportConflictMode.ADD_NEW,
                        label = stringResource(R.string.import_conflict_add),
                        onClick = { conflictMode = ImportConflictMode.ADD_NEW },
                    )
                    ConflictOption(
                        selected = conflictMode == ImportConflictMode.UPDATE_EXISTING,
                        label = stringResource(R.string.import_conflict_update),
                        onClick = { conflictMode = ImportConflictMode.UPDATE_EXISTING },
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                val count = viewModel.confirmImport(conflictMode)
                                snackbar.showSnackbar(context.getString(R.string.import_success, count))
                                viewModel.clear()
                                onDone()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.import_confirm)) }
                }
            }
        }
    }
}

@Composable
private fun ConflictOption(selected: Boolean, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDetailScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            content()
        }
    }
}
