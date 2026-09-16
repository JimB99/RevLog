package com.revlog.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revlog.data.backup.RevLogBackupManager
import com.revlog.data.repository.SettingsRepository
import com.revlog.data.repository.VehicleRepository
import com.revlog.domain.ServiceSummaryResolver
import com.revlog.domain.model.ImportConflictMode
import com.revlog.domain.model.ServiceLogEntry
import com.revlog.domain.model.ServiceType
import com.revlog.domain.model.Vehicle
import com.revlog.domain.model.VehicleBundle
import com.revlog.domain.model.VehicleData
import com.revlog.domain.model.VehicleType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class VehiclesHomeViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
) : ViewModel() {
    val vehicles = vehicleRepository.observeVehicles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createVehicle(name: String, type: VehicleType) {
        viewModelScope.launch {
            vehicleRepository.createVehicle(name, type)
        }
    }

    fun renameVehicle(vehicle: Vehicle, newName: String) {
        viewModelScope.launch {
            vehicleRepository.updateVehicle(vehicle.copy(name = newName.trim()))
        }
    }

    fun deleteVehicle(vehicleId: Long) {
        viewModelScope.launch {
            vehicleRepository.deleteVehicle(vehicleId)
        }
    }

    fun reorderVehicles(orderedIds: List<Long>) {
        viewModelScope.launch {
            vehicleRepository.reorderVehicles(orderedIds)
        }
    }

    suspend fun getBundles(vehicleIds: List<Long>): List<VehicleBundle> =
        vehicleRepository.getBundles(vehicleIds)
}

@HiltViewModel
class VehicleDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vehicleRepository: VehicleRepository,
) : ViewModel() {
    private val vehicleId: Long = savedStateHandle.get<Long>("vehicleId") ?: 0L

    val vehicle = vehicleRepository.observeVehicle(vehicleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val vehicleData = vehicleRepository.observeVehicleData(vehicleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val serviceLogs = vehicleRepository.observeServiceLogs(vehicleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val serviceSummary = combine(vehicle, serviceLogs) { v, logs ->
        if (v == null) emptyMap()
        else ServiceSummaryResolver.latestByType(logs, v.type)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun saveVehicleData(data: VehicleData) {
        viewModelScope.launch {
            vehicleRepository.upsertVehicleData(data)
        }
    }

    suspend fun saveVehicleDataAwait(data: VehicleData) {
        vehicleRepository.upsertVehicleData(data)
    }
}

@HiltViewModel
class ServiceEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vehicleRepository: VehicleRepository,
) : ViewModel() {
    private val vehicleId: Long = savedStateHandle.get<Long>("vehicleId") ?: 0L
    val serviceType: ServiceType = ServiceType.valueOf(
        savedStateHandle.get<String>("serviceType") ?: ServiceType.OIL_CHANGE.name,
    )

    fun addEntry(date: LocalDate, odometerKm: Int?, note: String?) {
        viewModelScope.launch {
            vehicleRepository.addServiceLog(
                ServiceLogEntry(
                    id = 0,
                    vehicleId = vehicleId,
                    type = serviceType,
                    performedAt = date,
                    odometerKm = odometerKm,
                    note = note?.takeIf { it.isNotBlank() },
                ),
            )
        }
    }

    suspend fun addEntryAwait(date: LocalDate, odometerKm: Int?, note: String?) {
        vehicleRepository.addServiceLog(
            ServiceLogEntry(
                id = 0,
                vehicleId = vehicleId,
                type = serviceType,
                performedAt = date,
                odometerKm = odometerKm,
                note = note?.takeIf { it.isNotBlank() },
            ),
        )
    }
}

@HiltViewModel
class ServiceLogsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vehicleRepository: VehicleRepository,
) : ViewModel() {
    private val vehicleId: Long = savedStateHandle.get<Long>("vehicleId") ?: 0L
    val serviceType: ServiceType = ServiceType.valueOf(
        savedStateHandle.get<String>("serviceType") ?: ServiceType.OIL_CHANGE.name,
    )

    val logs = vehicleRepository.observeServiceLogsByType(vehicleId, serviceType.name)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            vehicleRepository.deleteServiceLog(id)
        }
    }
}

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
) : ViewModel() {
    val vehicles = vehicleRepository.observeVehicles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun exportJson(selectedIds: List<Long>): String {
        val bundles = vehicleRepository.getBundles(selectedIds)
        return RevLogBackupManager.export(bundles)
    }
}

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
) : ViewModel() {
    private val _pendingBundles = MutableStateFlow<List<VehicleBundle>>(emptyList())
    val pendingBundles: StateFlow<List<VehicleBundle>> = _pendingBundles.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun parseImport(json: String) {
        try {
            val backup = RevLogBackupManager.import(json)
            _pendingBundles.value = RevLogBackupManager.toBundles(backup)
            _error.value = null
        } catch (_: Exception) {
            _pendingBundles.value = emptyList()
            _error.value = "invalid"
        }
    }

    suspend fun confirmImport(conflictMode: ImportConflictMode): Int =
        vehicleRepository.importBundles(_pendingBundles.value, conflictMode)

    fun clear() {
        _pendingBundles.value = emptyList()
        _error.value = null
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val settings = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), com.revlog.data.repository.AppSettings())

    fun setLanguage(tag: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(tag)
        }
    }
}
