package com.revlog.app.ui.vehicle

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.revlog.app.R
import com.revlog.app.ui.components.FormField
import com.revlog.app.ui.components.SaveChangesButton
import com.revlog.app.ui.components.formatDate
import com.revlog.app.ui.components.formatOptional
import com.revlog.app.ui.components.rememberUnsavedChangesGuard
import com.revlog.app.ui.components.serviceTypeLabel
import com.revlog.app.ui.viewmodel.ServiceEntryViewModel
import com.revlog.app.ui.viewmodel.ServiceLogsViewModel
import com.revlog.app.ui.viewmodel.VehicleDetailViewModel
import com.revlog.app.worker.ReminderCheckWorker
import com.revlog.domain.DateParser
import com.revlog.domain.PowerConversion
import com.revlog.domain.ServiceSummaryResolver
import com.revlog.domain.model.ServiceType
import com.revlog.domain.model.VehicleData
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VehicleDetailScreen(
    vehicleId: Long,
    initialTab: Int = 0,
    onBack: () -> Unit,
    onEditData: () -> Unit,
    onAddService: (ServiceType) -> Unit,
    onViewLogs: (ServiceType) -> Unit,
    viewModel: VehicleDetailViewModel = hiltViewModel(),
) {
    val vehicle by viewModel.vehicle.collectAsState()
    val data by viewModel.vehicleData.collectAsState()
    val summary by viewModel.serviceSummary.collectAsState()
    val reminderRules by viewModel.reminderRules.collectAsState()
    var selectedTab by rememberSaveable(vehicleId) {
        mutableIntStateOf(initialTab.coerceIn(0, 1))
    }
    val pagerState = rememberPagerState(
        initialPage = selectedTab,
        pageCount = { 2 },
    )
    val scope = rememberCoroutineScope()
    var reminderType by remember { mutableStateOf<ServiceType?>(null) }

    LaunchedEffect(initialTab) {
        if (initialTab == 1 && pagerState.currentPage != 1) {
            pagerState.scrollToPage(1)
            selectedTab = 1
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            selectedTab = page
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(vehicle?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text(stringResource(R.string.tab_data)) },
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text(stringResource(R.string.tab_service)) },
                )
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (page) {
                    0 -> DatenTab(
                        data = data,
                        vehicleType = vehicle?.type,
                        onEdit = onEditData,
                    )
                    1 -> ServiceTab(
                        vehicleType = vehicle?.type,
                        summary = summary,
                        reminderRules = reminderRules,
                        onAdd = onAddService,
                        onLogs = onViewLogs,
                        onReminder = { reminderType = it },
                    )
                }
            }
        }
    }

    reminderType?.let { type ->
        val existing = reminderRules.find { it.serviceType == type }
        val context = androidx.compose.ui.platform.LocalContext.current
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { _ -> }
        ReminderSetupSheet(
            serviceType = type,
            existing = existing,
            vehicleId = vehicleId,
            onDismiss = { reminderType = null },
            onSave = { rule ->
                viewModel.saveReminderRule(rule)
                if (rule.enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                ReminderCheckWorker.schedule(context)
            },
        )
    }
}

@Composable
private fun DatenTab(
    data: VehicleData?,
    vehicleType: com.revlog.domain.model.VehicleType?,
    onEdit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onEdit) {
                Text(stringResource(R.string.edit))
            }
        }
        DataRow(stringResource(R.string.license_plate), formatOptional(data?.licensePlate))
        DataRow(stringResource(R.string.vin), formatOptional(data?.vin))
        DataRow(stringResource(R.string.first_registration), formatDate(data?.firstRegistration).ifBlank { formatOptional(null) })
        DataRow(
            stringResource(R.string.purchased_at),
            formatDate(data?.purchasedAt).ifBlank { formatOptional(null) },
        )
        DataRow(
            stringResource(R.string.purchased_km),
            data?.purchasedKm?.let { "$it ${stringResource(R.string.km_unit)}" } ?: formatOptional(null),
        )
        Text(stringResource(R.string.tire_dimensions), style = MaterialTheme.typography.titleSmall)
        if (vehicleType != null) {
            TireSetsReadOnlySection(
                tireSets = data?.tireSets ?: emptyList(),
                vehicleType = vehicleType,
            )
        }
        DataRow(
            "${stringResource(R.string.power_kw)} / ${stringResource(R.string.power_ps)}",
            when {
                data?.powerKw != null && data.powerPs != null -> "${data.powerKw} kW / ${data.powerPs} PS"
                data?.powerKw != null -> "${data.powerKw} kW"
                data?.powerPs != null -> "${data.powerPs} PS"
                else -> formatOptional(null)
            },
        )
        DataRow(
            stringResource(R.string.displacement),
            data?.displacementCc?.let { "$it ${stringResource(R.string.cc_unit)}" } ?: formatOptional(null),
        )
        DataRow(stringResource(R.string.engine_oil), formatOptional(data?.engineOil))
        DataRow(stringResource(R.string.brake_fluid), formatOptional(data?.brakeFluid))
    }
}

@Composable
private fun DataRow(label: String, value: String) {
    ListItem(
        headlineContent = { Text(label) },
        supportingContent = { Text(value) },
    )
}

@Composable
private fun ServiceTab(
    vehicleType: com.revlog.domain.model.VehicleType?,
    summary: Map<ServiceType, LocalDate?>,
    reminderRules: List<com.revlog.domain.model.ReminderRule>,
    onAdd: (ServiceType) -> Unit,
    onLogs: (ServiceType) -> Unit,
    onReminder: (ServiceType) -> Unit,
) {
    val types = vehicleType?.let { ServiceSummaryResolver.applicableTypes(it) } ?: emptyList()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        types.forEach { type ->
            val latest = summary[type]
            val rule = reminderRules.find { it.serviceType == type }
            CardServiceRow(
                label = serviceTypeLabel(type),
                date = latest?.let { formatDate(it) } ?: stringResource(R.string.empty),
                reminderEnabled = rule?.enabled == true,
                onAdd = { onAdd(type) },
                onLogs = { onLogs(type) },
                onReminder = { onReminder(type) },
            )
        }
    }
}

@Composable
private fun CardServiceRow(
    label: String,
    date: String,
    reminderEnabled: Boolean,
    onAdd: () -> Unit,
    onLogs: () -> Unit,
    onReminder: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        ListItem(
            headlineContent = { Text(label) },
            supportingContent = { Text(date) },
            trailingContent = {
                IconButton(onClick = onReminder) {
                    Icon(
                        imageVector = if (reminderEnabled) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                        contentDescription = stringResource(R.string.reminder_setup),
                    )
                }
            },
            modifier = Modifier.clickable(onClick = onAdd),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onLogs) {
                Text(stringResource(R.string.logs))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDataEditScreen(
    onBack: () -> Unit,
    viewModel: VehicleDetailViewModel = hiltViewModel(),
) {
    val vehicle by viewModel.vehicle.collectAsState()
    val initial by viewModel.vehicleData.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val datePlaceholder = stringResource(R.string.date_placeholder)
    val dateInvalidMessage = stringResource(R.string.date_invalid)

    var licensePlate by remember(initial) { mutableStateOf(initial?.licensePlate ?: "") }
    var vin by remember(initial) { mutableStateOf(initial?.vin ?: "") }
    var firstRegistration by remember(initial) {
        mutableStateOf(DateParser.format(initial?.firstRegistration))
    }
    var purchasedAt by remember(initial) {
        mutableStateOf(DateParser.format(initial?.purchasedAt))
    }
    var purchasedKm by remember(initial) { mutableStateOf(initial?.purchasedKm?.toString() ?: "") }
    var tireSets by remember(initial) { mutableStateOf(initial?.tireSets ?: emptyList()) }
    var powerKw by remember(initial) { mutableStateOf(initial?.powerKw?.toString() ?: "") }
    var powerPs by remember(initial) { mutableStateOf(initial?.powerPs?.toString() ?: "") }
    var displacementCc by remember(initial) { mutableStateOf(initial?.displacementCc?.toString() ?: "") }
    var engineOil by remember(initial) { mutableStateOf(initial?.engineOil ?: "") }
    var brakeFluid by remember(initial) { mutableStateOf(initial?.brakeFluid ?: "") }
    var dateError by remember { mutableStateOf(false) }

    fun dateFieldDirty(text: String, original: LocalDate?): Boolean {
        val trimmed = text.trim()
        if (trimmed.isBlank() && original == null) return false
        val parsed = DateParser.parseOrNull(trimmed)
        return parsed != original || (trimmed.isNotBlank() && parsed == null)
    }

    fun buildData(): VehicleData? {
        val v = vehicle ?: return null
        return VehicleData(
            vehicleId = v.id,
            licensePlate = licensePlate.trim().ifBlank { null },
            vin = vin.trim().ifBlank { null },
            firstRegistration = DateParser.parseOrNull(firstRegistration),
            purchasedAt = DateParser.parseOrNull(purchasedAt),
            purchasedKm = purchasedKm.trim().toIntOrNull(),
            tireSets = tireSets,
            powerKw = powerKw.trim().toIntOrNull(),
            powerPs = powerPs.trim().toIntOrNull(),
            displacementCc = displacementCc.trim().toIntOrNull(),
            engineOil = engineOil.trim().ifBlank { null },
            brakeFluid = brakeFluid.trim().ifBlank { null },
        )
    }

    val snapshot = initial
    val isDirty = snapshot != null && (
        licensePlate.trim() != snapshot.licensePlate.orEmpty().trim() ||
            vin.trim() != snapshot.vin.orEmpty().trim() ||
            dateFieldDirty(firstRegistration, snapshot.firstRegistration) ||
            dateFieldDirty(purchasedAt, snapshot.purchasedAt) ||
            purchasedKm.trim() != snapshot.purchasedKm?.toString().orEmpty() ||
            tireSets != snapshot.tireSets ||
            powerKw.trim() != snapshot.powerKw?.toString().orEmpty() ||
            powerPs.trim() != snapshot.powerPs?.toString().orEmpty() ||
            displacementCc.trim() != snapshot.displacementCc?.toString().orEmpty() ||
            engineOil.trim() != snapshot.engineOil.orEmpty().trim() ||
            brakeFluid.trim() != snapshot.brakeFluid.orEmpty().trim()
        )

    val backGuard = rememberUnsavedChangesGuard(
        isDirty = isDirty,
        onNavigateBack = onBack,
        onSave = {
            if (!DateParser.isValidDisplayInput(firstRegistration) ||
                !DateParser.isValidDisplayInput(purchasedAt)
            ) {
                dateError = true
                false
            } else {
                buildData()?.let { viewModel.saveVehicleDataAwait(it) }
                true
            }
        },
        onDiscardChanges = {
            licensePlate = initial?.licensePlate ?: ""
            vin = initial?.vin ?: ""
            firstRegistration = DateParser.format(initial?.firstRegistration)
            purchasedAt = DateParser.format(initial?.purchasedAt)
            purchasedKm = initial?.purchasedKm?.toString() ?: ""
            tireSets = initial?.tireSets ?: emptyList()
            powerKw = initial?.powerKw?.toString() ?: ""
            powerPs = initial?.powerPs?.toString() ?: ""
            displacementCc = initial?.displacementCc?.toString() ?: ""
            engineOil = initial?.engineOil ?: ""
            brakeFluid = initial?.brakeFluid ?: ""
            dateError = false
        },
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit)) },
                navigationIcon = {
                    IconButton(onClick = backGuard::navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FormField(R.string.license_plate, licensePlate) { licensePlate = it }
            FormField(R.string.vin, vin) { vin = it }
            FormField(
                labelRes = R.string.first_registration,
                value = firstRegistration,
                placeholder = datePlaceholder,
                isError = dateError && !DateParser.isValidDisplayInput(firstRegistration),
                onValueChange = { firstRegistration = it; dateError = false },
            )
            FormField(
                labelRes = R.string.purchased_at,
                value = purchasedAt,
                placeholder = datePlaceholder,
                isError = dateError && !DateParser.isValidDisplayInput(purchasedAt),
                onValueChange = { purchasedAt = it; dateError = false },
            )
            FormField(R.string.purchased_km, purchasedKm) { purchasedKm = it }
            Text(stringResource(R.string.tire_dimensions), style = MaterialTheme.typography.titleSmall)
            vehicle?.type?.let { type ->
                TireSetsEditor(
                    tireSets = tireSets,
                    vehicleType = type,
                    onChange = { tireSets = it },
                )
            }
            PowerRow(kw = powerKw, ps = powerPs, onKwChange = { powerKw = it }, onPsChange = { powerPs = it })
            FormField(R.string.displacement, displacementCc) { displacementCc = it }
            FormField(R.string.engine_oil, engineOil) { engineOil = it }
            FormField(R.string.brake_fluid, brakeFluid) { brakeFluid = it }
            SaveChangesButton(
                visible = isDirty,
                onClick = {
                    if (!DateParser.isValidDisplayInput(firstRegistration) ||
                        !DateParser.isValidDisplayInput(purchasedAt)
                    ) {
                        dateError = true
                        scope.launch { snackbarHostState.showSnackbar(dateInvalidMessage) }
                        return@SaveChangesButton
                    }
                    scope.launch {
                        buildData()?.let { viewModel.saveVehicleDataAwait(it) }
                        onBack()
                    }
                },
            )
        }
    }
}

@Composable
private fun PowerRow(
    kw: String,
    ps: String,
    onKwChange: (String) -> Unit,
    onPsChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = kw,
            onValueChange = {
                onKwChange(it)
                it.toIntOrNull()?.let { value -> onPsChange(PowerConversion.kwToPs(value).toString()) }
            },
            label = { Text(stringResource(R.string.power_kw)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
            value = ps,
            onValueChange = {
                onPsChange(it)
                it.toIntOrNull()?.let { value -> onKwChange(PowerConversion.psToKw(value).toString()) }
            },
            label = { Text(stringResource(R.string.power_ps)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.weight(1f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceEntryScreen(
    onBack: () -> Unit,
    viewModel: ServiceEntryViewModel = hiltViewModel(),
) {
    var dateText by remember { mutableStateOf(DateParser.format(LocalDate.now())) }
    var odometer by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val datePlaceholder = stringResource(R.string.date_placeholder)
    val dateInvalidMessage = stringResource(R.string.date_invalid)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(serviceTypeLabel(viewModel.serviceType)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FormField(
                labelRes = R.string.service_date,
                value = dateText,
                placeholder = datePlaceholder,
                isError = dateError,
                onValueChange = { dateText = it; dateError = false },
            )
            FormField(R.string.odometer, odometer) { odometer = it }
            FormField(R.string.note, note) { note = it }
            Button(
                onClick = {
                    val date = DateParser.parseOrNull(dateText)
                    if (date == null) {
                        dateError = true
                        scope.launch { snackbarHostState.showSnackbar(dateInvalidMessage) }
                        return@Button
                    }
                    scope.launch {
                        viewModel.addEntryAwait(date, odometer.trim().toIntOrNull(), note)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.save)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceLogsScreen(
    onBack: () -> Unit,
    viewModel: ServiceLogsViewModel = hiltViewModel(),
) {
    val logs by viewModel.logs.collectAsState()
    var deleteId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(serviceTypeLabel(viewModel.serviceType)) },
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
            if (logs.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_logs),
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                logs.forEach { entry ->
                    ListItem(
                        headlineContent = { Text(formatDate(entry.performedAt)) },
                        supportingContent = {
                            val parts = buildList {
                                entry.odometerKm?.let { add("$it ${stringResource(R.string.km_unit)}") }
                                entry.note?.let { add(it) }
                            }
                            if (parts.isNotEmpty()) Text(parts.joinToString(" · "))
                        },
                        trailingContent = {
                            TextButton(onClick = { deleteId = entry.id }) {
                                Text(stringResource(R.string.delete))
                            }
                        },
                    )
                }
            }
        }
    }

    deleteId?.let { id ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text(stringResource(R.string.delete_entry_title)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEntry(id)
                    deleteId = null
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteId = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}
