package com.revlog.app.ui.vehicle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.revlog.app.R
import com.revlog.app.ui.components.SaveChangesButton
import com.revlog.app.ui.components.formatDate
import com.revlog.app.ui.components.formatOptional
import com.revlog.app.ui.components.rememberUnsavedChangesGuard
import com.revlog.app.ui.components.serviceTypeLabel
import com.revlog.app.ui.viewmodel.ServiceEntryViewModel
import com.revlog.app.ui.viewmodel.ServiceLogsViewModel
import com.revlog.app.ui.viewmodel.VehicleDetailViewModel
import com.revlog.domain.PowerConversion
import com.revlog.domain.ServiceSummaryResolver
import com.revlog.domain.model.ServiceType
import com.revlog.domain.model.VehicleData
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    vehicleId: Long,
    onBack: () -> Unit,
    onEditData: () -> Unit,
    onAddService: (ServiceType) -> Unit,
    onViewLogs: (ServiceType) -> Unit,
    viewModel: VehicleDetailViewModel = hiltViewModel(),
) {
    val vehicle by viewModel.vehicle.collectAsState()
    val data by viewModel.vehicleData.collectAsState()
    val summary by viewModel.serviceSummary.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

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
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.tab_data)) },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.tab_service)) },
                )
            }
            when (selectedTab) {
                0 -> DatenTab(data = data, onEdit = onEditData)
                1 -> ServiceTab(
                    vehicleType = vehicle?.type,
                    summary = summary,
                    onAdd = onAddService,
                    onLogs = onViewLogs,
                )
            }
        }
    }
}

@Composable
private fun DatenTab(data: VehicleData?, onEdit: () -> Unit) {
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
        DataRow(stringResource(R.string.tire_dimensions), formatOptional(data?.tireDimensions))
        Text(stringResource(R.string.tire_pressure), style = MaterialTheme.typography.titleSmall)
        DataRow(stringResource(R.string.tire_pressure_front), formatOptional(data?.tirePressureFront))
        DataRow(stringResource(R.string.tire_pressure_rear), formatOptional(data?.tirePressureRear))
        DataRow(stringResource(R.string.tire_pressure_loaded), formatOptional(data?.tirePressureLoaded))
        DataRow(stringResource(R.string.tire_pressure_unladen), formatOptional(data?.tirePressureUnladen))
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
    onAdd: (ServiceType) -> Unit,
    onLogs: (ServiceType) -> Unit,
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
            CardServiceRow(
                label = serviceTypeLabel(type),
                date = latest?.let { formatDate(it) } ?: stringResource(R.string.empty),
                onAdd = { onAdd(type) },
                onLogs = { onLogs(type) },
            )
        }
    }
}

@Composable
private fun CardServiceRow(
    label: String,
    date: String,
    onAdd: () -> Unit,
    onLogs: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        ListItem(
            headlineContent = { Text(label) },
            supportingContent = { Text(date) },
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

    var licensePlate by remember(initial) { mutableStateOf(initial?.licensePlate ?: "") }
    var vin by remember(initial) { mutableStateOf(initial?.vin ?: "") }
    var firstRegistration by remember(initial) { mutableStateOf(initial?.firstRegistration?.toString() ?: "") }
    var purchasedAt by remember(initial) { mutableStateOf(initial?.purchasedAt?.toString() ?: "") }
    var purchasedKm by remember(initial) { mutableStateOf(initial?.purchasedKm?.toString() ?: "") }
    var tireDimensions by remember(initial) { mutableStateOf(initial?.tireDimensions ?: "") }
    var tirePressureFront by remember(initial) { mutableStateOf(initial?.tirePressureFront ?: "") }
    var tirePressureRear by remember(initial) { mutableStateOf(initial?.tirePressureRear ?: "") }
    var tirePressureLoaded by remember(initial) { mutableStateOf(initial?.tirePressureLoaded ?: "") }
    var tirePressureUnladen by remember(initial) { mutableStateOf(initial?.tirePressureUnladen ?: "") }
    var powerKw by remember(initial) { mutableStateOf(initial?.powerKw?.toString() ?: "") }
    var powerPs by remember(initial) { mutableStateOf(initial?.powerPs?.toString() ?: "") }
    var displacementCc by remember(initial) { mutableStateOf(initial?.displacementCc?.toString() ?: "") }
    var engineOil by remember(initial) { mutableStateOf(initial?.engineOil ?: "") }
    var brakeFluid by remember(initial) { mutableStateOf(initial?.brakeFluid ?: "") }

    fun buildData(): VehicleData? {
        val v = vehicle ?: return null
        return VehicleData(
            vehicleId = v.id,
            licensePlate = licensePlate.trim().ifBlank { null },
            vin = vin.trim().ifBlank { null },
            firstRegistration = firstRegistration.trim().takeIf { it.isNotBlank() }?.let(LocalDate::parse),
            purchasedAt = purchasedAt.trim().takeIf { it.isNotBlank() }?.let(LocalDate::parse),
            purchasedKm = purchasedKm.trim().toIntOrNull(),
            tireDimensions = tireDimensions.trim().ifBlank { null },
            tirePressureFront = tirePressureFront.trim().ifBlank { null },
            tirePressureRear = tirePressureRear.trim().ifBlank { null },
            tirePressureLoaded = tirePressureLoaded.trim().ifBlank { null },
            tirePressureUnladen = tirePressureUnladen.trim().ifBlank { null },
            powerKw = powerKw.trim().toIntOrNull(),
            powerPs = powerPs.trim().toIntOrNull(),
            displacementCc = displacementCc.trim().toIntOrNull(),
            engineOil = engineOil.trim().ifBlank { null },
            brakeFluid = brakeFluid.trim().ifBlank { null },
        )
    }

    val isDirty = initial != null && buildData() != initial

    val backGuard = rememberUnsavedChangesGuard(
        isDirty = isDirty,
        onNavigateBack = onBack,
        onSave = { buildData()?.let { viewModel.saveVehicleDataAwait(it) } },
        onDiscardChanges = {
            licensePlate = initial?.licensePlate ?: ""
            vin = initial?.vin ?: ""
            firstRegistration = initial?.firstRegistration?.toString() ?: ""
            purchasedAt = initial?.purchasedAt?.toString() ?: ""
            purchasedKm = initial?.purchasedKm?.toString() ?: ""
            tireDimensions = initial?.tireDimensions ?: ""
            tirePressureFront = initial?.tirePressureFront ?: ""
            tirePressureRear = initial?.tirePressureRear ?: ""
            tirePressureLoaded = initial?.tirePressureLoaded ?: ""
            tirePressureUnladen = initial?.tirePressureUnladen ?: ""
            powerKw = initial?.powerKw?.toString() ?: ""
            powerPs = initial?.powerPs?.toString() ?: ""
            displacementCc = initial?.displacementCc?.toString() ?: ""
            engineOil = initial?.engineOil ?: ""
            brakeFluid = initial?.brakeFluid ?: ""
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
            FormField(R.string.first_registration, firstRegistration, "YYYY-MM-DD") { firstRegistration = it }
            FormField(R.string.purchased_at, purchasedAt, "YYYY-MM-DD") { purchasedAt = it }
            FormField(R.string.purchased_km, purchasedKm) { purchasedKm = it }
            FormField(R.string.tire_dimensions, tireDimensions) { tireDimensions = it }
            Text(stringResource(R.string.tire_pressure), style = MaterialTheme.typography.titleSmall)
            FormField(R.string.tire_pressure_front, tirePressureFront) { tirePressureFront = it }
            FormField(R.string.tire_pressure_rear, tirePressureRear) { tirePressureRear = it }
            FormField(R.string.tire_pressure_loaded, tirePressureLoaded) { tirePressureLoaded = it }
            FormField(R.string.tire_pressure_unladen, tirePressureUnladen) { tirePressureUnladen = it }
            FormField(R.string.power_kw, powerKw) {
                powerKw = it
                it.toIntOrNull()?.let { kw -> if (powerPs.isBlank()) powerPs = PowerConversion.kwToPs(kw).toString() }
            }
            FormField(R.string.power_ps, powerPs) {
                powerPs = it
                it.toIntOrNull()?.let { ps -> if (powerKw.isBlank()) powerKw = PowerConversion.psToKw(ps).toString() }
            }
            FormField(R.string.displacement, displacementCc) { displacementCc = it }
            FormField(R.string.engine_oil, engineOil) { engineOil = it }
            FormField(R.string.brake_fluid, brakeFluid) { brakeFluid = it }
            SaveChangesButton(
                visible = isDirty,
                onClick = {
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
private fun FormField(
    labelRes: Int,
    value: String,
    placeholder: String = "",
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelRes)) },
        placeholder = if (placeholder.isNotBlank()) ({ Text(placeholder) }) else null,
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceEntryScreen(
    onBack: () -> Unit,
    viewModel: ServiceEntryViewModel = hiltViewModel(),
) {
    var dateText by remember { mutableStateOf(LocalDate.now().toString()) }
    var odometer by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FormField(R.string.service_date, dateText, "YYYY-MM-DD") { dateText = it }
            FormField(R.string.odometer, odometer) { odometer = it }
            FormField(R.string.note, note) { note = it }
            Button(
                onClick = {
                    scope.launch {
                        viewModel.addEntryAwait(
                            LocalDate.parse(dateText),
                            odometer.trim().toIntOrNull(),
                            note,
                        )
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
