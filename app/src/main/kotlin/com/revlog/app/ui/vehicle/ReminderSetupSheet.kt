package com.revlog.app.ui.vehicle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.revlog.app.R
import com.revlog.domain.model.ReminderRule
import com.revlog.domain.model.ServiceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSetupSheet(
    serviceType: ServiceType,
    existing: ReminderRule?,
    vehicleId: Long,
    onDismiss: () -> Unit,
    onSave: (ReminderRule) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var enabled by remember(existing) { mutableStateOf(existing?.enabled ?: true) }
    var intervalMonths by remember(existing) { mutableIntStateOf(existing?.intervalMonths ?: 12) }
    var leadDays by remember(existing) { mutableIntStateOf(existing?.leadDays ?: 14) }
    var customInterval by remember(existing) {
        mutableStateOf(existing?.intervalMonths?.toString() ?: "12")
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = stringResource(R.string.reminder_setup))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.reminder_enabled))
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }
            Text(stringResource(R.string.reminder_interval))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IntervalChip(6, intervalMonths) { intervalMonths = 6; customInterval = "6" }
                IntervalChip(12, intervalMonths) { intervalMonths = 12; customInterval = "12" }
                IntervalChip(24, intervalMonths) { intervalMonths = 24; customInterval = "24" }
                IntervalChip(36, intervalMonths) { intervalMonths = 36; customInterval = "36" }
            }
            OutlinedTextField(
                value = customInterval,
                onValueChange = {
                    customInterval = it
                    it.toIntOrNull()?.let { months -> intervalMonths = months }
                },
                label = { Text(stringResource(R.string.reminder_interval)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = leadDays.toString(),
                onValueChange = { it.toIntOrNull()?.let { days -> leadDays = days } },
                label = { Text(stringResource(R.string.reminder_lead_days)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    onSave(
                        ReminderRule(
                            id = existing?.id ?: 0,
                            vehicleId = vehicleId,
                            serviceType = serviceType,
                            intervalMonths = intervalMonths,
                            leadDays = leadDays,
                            enabled = enabled,
                            lastNotifiedDueDate = existing?.lastNotifiedDueDate,
                        ),
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@Composable
private fun IntervalChip(months: Int, selected: Int, onClick: () -> Unit) {
    val label = when (months) {
        6 -> stringResource(R.string.interval_6_months)
        12 -> stringResource(R.string.interval_1_year)
        24 -> stringResource(R.string.interval_2_years)
        36 -> stringResource(R.string.interval_3_years)
        else -> "$months"
    }
    TextButtonChip(selected == months, label, onClick)
}

@Composable
private fun TextButtonChip(selected: Boolean, label: String, onClick: () -> Unit) {
    androidx.compose.material3.FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}
