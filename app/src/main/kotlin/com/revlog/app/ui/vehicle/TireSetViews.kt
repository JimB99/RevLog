package com.revlog.app.ui.vehicle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlog.app.R
import com.revlog.app.ui.components.SelectableContent
import com.revlog.domain.DecimalSeparator
import com.revlog.domain.InputNormalizer
import com.revlog.domain.TireDisplayResolver
import com.revlog.domain.model.TirePosition
import com.revlog.domain.model.TireRow
import com.revlog.domain.model.TireSet
import com.revlog.domain.model.VehicleType

@Composable
fun TireSetsReadOnlySection(
    tireSets: List<TireSet>,
    vehicleType: VehicleType,
) {
    SelectableContent {
    if (tireSets.isEmpty()) {
        Text(text = stringResource(R.string.empty))
        return@SelectableContent
    }
    tireSets.forEachIndexed { index, set ->
        val rows = TireDisplayResolver.visibleRows(set, vehicleType)
        if (rows.isEmpty()) return@forEachIndexed
        if (index > 0) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
        TireSetReadOnlyTable(rows = rows, vehicleType = vehicleType)
    }
    }
}

@Composable
private fun TireSetReadOnlyTable(
    rows: List<TireRow>,
    vehicleType: VehicleType,
) {
    val isCar = vehicleType == VehicleType.CAR
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("", modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.labelSmall)
            Text(
                stringResource(R.string.tire_dimensions_col),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
            if (isCar) {
                Text(
                    stringResource(R.string.tire_pressure_loaded),
                    modifier = Modifier.weight(0.8f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    stringResource(R.string.tire_pressure_unladen),
                    modifier = Modifier.weight(0.8f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            } else {
                Text(
                    stringResource(R.string.tire_pressure_col),
                    modifier = Modifier.weight(0.8f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    tirePositionLabel(row.position),
                    modifier = Modifier.weight(1.2f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    row.dimensions ?: stringResource(R.string.empty),
                    modifier = Modifier.weight(1f),
                )
                if (isCar) {
                    Text(
                        row.pressureLoaded ?: stringResource(R.string.empty),
                        modifier = Modifier.weight(0.8f),
                    )
                    Text(
                        row.pressureUnladen ?: stringResource(R.string.empty),
                        modifier = Modifier.weight(0.8f),
                    )
                } else {
                    Text(
                        row.pressure ?: stringResource(R.string.empty),
                        modifier = Modifier.weight(0.8f),
                    )
                }
            }
        }
    }
}

@Composable
fun TireSetsEditor(
    tireSets: List<TireSet>,
    vehicleType: VehicleType,
    languageTag: String,
    onChange: (List<TireSet>) -> Unit,
) {
    val sets = if (tireSets.isEmpty()) {
        listOf(TireSet.defaultFor(vehicleType))
    } else {
        tireSets
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        sets.forEachIndexed { setIndex, set ->
            if (setIndex > 0) {
                HorizontalDivider()
            }
            TireSetEditor(
                set = set,
                vehicleType = vehicleType,
                languageTag = languageTag,
                onChange = { updated ->
                    onChange(sets.toMutableList().also { it[setIndex] = updated })
                },
            )
        }
        TextButton(
            onClick = {
                val template = sets.lastOrNull() ?: TireSet.defaultFor(vehicleType)
                onChange(sets + duplicateTireSet(template))
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.tire_add_set))
        }
    }
}

@Composable
private fun TireSetEditor(
    set: TireSet,
    vehicleType: VehicleType,
    languageTag: String,
    onChange: (TireSet) -> Unit,
) {
    val rows = TireDisplayResolver.editorRows(set, vehicleType)
    val isCar = vehicleType == VehicleType.CAR
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEachIndexed { rowIndex, row ->
            Text(
                text = tirePositionLabel(row.position),
                style = MaterialTheme.typography.titleSmall,
            )
            FormFieldValue(
                label = stringResource(R.string.tire_dimensions_col),
                value = row.dimensions ?: "",
                languageTag = languageTag,
                onValueChange = { value ->
                    onChange(set.withRow(rowIndex, row.copy(dimensions = value.ifBlank { null })))
                },
            )
            if (isCar) {
                FormFieldValue(
                    label = stringResource(R.string.tire_pressure_loaded),
                    value = row.pressureLoaded ?: "",
                    languageTag = languageTag,
                    onValueChange = { value ->
                        onChange(set.withRow(rowIndex, row.copy(pressureLoaded = value.ifBlank { null })))
                    },
                )
                FormFieldValue(
                    label = stringResource(R.string.tire_pressure_unladen),
                    value = row.pressureUnladen ?: "",
                    languageTag = languageTag,
                    onValueChange = { value ->
                        onChange(set.withRow(rowIndex, row.copy(pressureUnladen = value.ifBlank { null })))
                    },
                )
            } else {
                FormFieldValue(
                    label = stringResource(R.string.tire_pressure_col),
                    value = row.pressure ?: "",
                    languageTag = languageTag,
                    onValueChange = { value ->
                        onChange(set.withRow(rowIndex, row.copy(pressure = value.ifBlank { null })))
                    },
                )
            }
        }
    }
}

@Composable
internal fun FormFieldValue(
    label: String,
    value: String,
    languageTag: String,
    placeholder: String = "",
    isError: Boolean = false,
    normalizeDecimal: Boolean = true,
    onValueChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val decimalSeparator = DecimalSeparator.forLanguageTag(languageTag)
    OutlinedTextField(
        value = value,
        onValueChange = { raw ->
            var v = if (normalizeDecimal) {
                InputNormalizer.normalizeDecimalSeparator(raw, decimalSeparator)
            } else {
                raw
            }
            v = InputNormalizer.sanitizeSingleLine(v)
            onValueChange(v)
        },
        label = { Text(label) },
        placeholder = if (placeholder.isNotBlank()) ({ Text(placeholder) }) else null,
        isError = isError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun tirePositionLabel(position: TirePosition): String = when (position) {
    TirePosition.FRONT -> stringResource(R.string.tire_front)
    TirePosition.REAR -> stringResource(R.string.tire_rear)
}

private fun TireSet.withRow(index: Int, row: TireRow): TireSet {
    val updated = rows.toMutableList()
    if (index in updated.indices) {
        updated[index] = row
    }
    return copy(rows = updated)
}

private fun duplicateTireSet(set: TireSet): TireSet = TireSet(
    rows = set.rows.map { row ->
        row.copy(
            dimensions = row.dimensions,
            pressureLoaded = row.pressureLoaded,
            pressureUnladen = row.pressureUnladen,
            pressure = row.pressure,
        )
    },
)
