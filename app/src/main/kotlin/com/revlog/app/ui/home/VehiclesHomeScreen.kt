package com.revlog.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Motorcycle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.revlog.app.R
import com.revlog.app.ui.viewmodel.VehiclesHomeViewModel
import com.revlog.domain.model.Vehicle
import com.revlog.domain.model.VehicleType
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesHomeScreen(
    onNavigateSettings: () -> Unit,
    onNavigateVehicle: (Long) -> Unit,
    onExportVehicle: (Long) -> Unit,
    viewModel: VehiclesHomeViewModel = hiltViewModel(),
) {
    val vehicles by viewModel.vehicles.collectAsState()
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newType by remember { mutableStateOf(VehicleType.CAR) }
    var dragging by remember { mutableStateOf(false) }
    val orderedVehicles = remember { mutableStateListOf<Vehicle>() }
    val latestOrdered = rememberUpdatedState(orderedVehicles.toList())
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromId = from.key as? Long ?: return@rememberReorderableLazyListState
        val toId = to.key as? Long ?: return@rememberReorderableLazyListState
        val fromIndex = orderedVehicles.indexOfFirst { it.id == fromId }
        val toIndex = orderedVehicles.indexOfFirst { it.id == toId }
        if (fromIndex < 0 || toIndex < 0 || fromIndex == toIndex) return@rememberReorderableLazyListState
        val item = orderedVehicles.removeAt(fromIndex)
        orderedVehicles.add(toIndex, item)
    }

    LaunchedEffect(vehicles) {
        if (!dragging) {
            orderedVehicles.clear()
            orderedVehicles.addAll(vehicles)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onNavigateSettings) {
                        Icon(Icons.Default.Settings, stringResource(R.string.settings))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Default.Add, stringResource(R.string.new_vehicle))
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            state = lazyListState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (orderedVehicles.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = stringResource(R.string.create_vehicle_hint),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            }
            items(orderedVehicles, key = { it.id }) { vehicle ->
                ReorderableItem(reorderableState, key = vehicle.id) {
                    VehicleRow(
                        vehicle = vehicle,
                        onOpen = { onNavigateVehicle(vehicle.id) },
                        onRename = { viewModel.renameVehicle(vehicle, it) },
                        onDelete = { viewModel.deleteVehicle(vehicle.id) },
                        onExport = { onExportVehicle(vehicle.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .longPressDraggableHandle(
                                onDragStarted = { dragging = true },
                                onDragStopped = {
                                    dragging = false
                                    viewModel.reorderVehicles(latestOrdered.value.map { it.id })
                                },
                            ),
                    )
                }
            }
        }
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text(stringResource(R.string.new_vehicle)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(stringResource(R.string.name)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { newType = VehicleType.CAR },
                    ) {
                        RadioButton(
                            selected = newType == VehicleType.CAR,
                            onClick = null,
                        )
                        Text(stringResource(R.string.vehicle_type_car))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { newType = VehicleType.MOTORCYCLE },
                    ) {
                        RadioButton(
                            selected = newType == VehicleType.MOTORCYCLE,
                            onClick = null,
                        )
                        Text(stringResource(R.string.vehicle_type_motorcycle))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        viewModel.createVehicle(newName, newType)
                        newName = ""
                        newType = VehicleType.CAR
                        showCreate = false
                    }
                }) { Text(stringResource(R.string.create)) }
            },
            dismissButton = {
                TextButton(onClick = { showCreate = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun VehicleRow(
    vehicle: Vehicle,
    onOpen: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    var showRename by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var renameValue by remember(vehicle.name) { mutableStateOf(vehicle.name) }

    Card(
        modifier = modifier.clickable(onClick = onOpen),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = if (vehicle.type == VehicleType.CAR) {
                        Icons.Default.DirectionsCar
                    } else {
                        Icons.Default.Motorcycle
                    },
                    contentDescription = null,
                )
                Text(
                    text = vehicle.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.rename)) },
                    onClick = {
                        menuOpen = false
                        showRename = true
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.export_vehicle)) },
                    onClick = {
                        menuOpen = false
                        onExport()
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.delete)) },
                    onClick = {
                        menuOpen = false
                        showDelete = true
                    },
                )
            }
        }
    }

    if (showRename) {
        AlertDialog(
            onDismissRequest = { showRename = false },
            title = { Text(stringResource(R.string.rename)) },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    label = { Text(stringResource(R.string.name)) },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (renameValue.isNotBlank()) {
                        onRename(renameValue)
                        showRename = false
                    }
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showRename = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text(stringResource(R.string.delete_vehicle_title)) },
            text = { Text(stringResource(R.string.delete_vehicle_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDelete = false
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}
