package com.revlog.domain

import com.revlog.domain.model.ServiceLogEntry
import com.revlog.domain.model.ServiceType
import com.revlog.domain.model.VehicleType
import java.time.LocalDate

object ServiceSummaryResolver {
    private val motorcycleOnlyTypes = setOf(
        ServiceType.CHAIN_REPLACEMENT,
        ServiceType.CHAIN_SERVICE,
    )

    fun applicableTypes(vehicleType: VehicleType): List<ServiceType> =
        ServiceType.entries.filter { type ->
            vehicleType == VehicleType.MOTORCYCLE || type !in motorcycleOnlyTypes
        }

    fun latestByType(
        logs: List<ServiceLogEntry>,
        vehicleType: VehicleType,
    ): Map<ServiceType, LocalDate?> {
        val applicable = applicableTypes(vehicleType).toSet()
        val latest = logs
            .filter { it.type in applicable }
            .groupBy { it.type }
            .mapValues { (_, entries) -> entries.maxOf { it.performedAt } }

        return applicable.associateWith { type -> latest[type] }
    }
}
