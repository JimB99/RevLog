package com.revlog.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.revlog.app.R
import com.revlog.domain.model.ServiceType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

fun formatDate(date: LocalDate?): String =
    date?.format(dateFormatter) ?: ""

@Composable
fun formatOptional(value: String?): String =
    value?.takeIf { it.isNotBlank() } ?: stringResource(R.string.empty)

@Composable
fun serviceTypeLabel(type: ServiceType): String = when (type) {
    ServiceType.TIRE_CHANGE -> stringResource(R.string.service_tire_change)
    ServiceType.OIL_CHANGE -> stringResource(R.string.service_oil_change)
    ServiceType.BRAKE_FLUID_CHANGE -> stringResource(R.string.service_brake_fluid)
    ServiceType.CHAIN_REPLACEMENT -> stringResource(R.string.service_chain_replacement)
    ServiceType.CHAIN_SERVICE -> stringResource(R.string.service_chain_service)
    ServiceType.INSPECTION -> stringResource(R.string.service_inspection)
}

fun slugify(name: String): String =
    name.lowercase(Locale.ROOT)
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
        .ifBlank { "fahrzeug" }
