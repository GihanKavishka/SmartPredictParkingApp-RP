package com.example.androidsmartpredictparkinggihan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.androidsmartpredictparkinggihan.ui.theme.ParkingAvailable
import com.example.androidsmartpredictparkinggihan.ui.theme.ParkingUnavailable
import com.example.androidsmartpredictparkinggihan.ui.viewmodel.PredictionUiState

private val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PredictionScreen(
    uiState: PredictionUiState,
    onBack: () -> Unit,
    onParkingSelected: (String) -> Unit,
    onHourChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
    onHolidayToggle: (Boolean) -> Unit,
    onEventToggle: (Boolean) -> Unit,
    onOccupancyChange: (Float) -> Unit,
    onDurationHoursChange: (Float) -> Unit,
    onCancellationCountChange: (Float) -> Unit,
    onPredict: () -> Unit,
    onApplyLowDemandPreset: () -> Unit,
    onApplyHighDemandPreset: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Availability prediction") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Enter booking context (matches your CSV features)",
                style = MaterialTheme.typography.bodyMedium,
            )

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = uiState.selectedParkingId,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Parking ID") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    uiState.spaces.forEach { space ->
                        DropdownMenuItem(
                            text = { Text("${space.parkingId} · ${space.location}") },
                            onClick = {
                                onParkingSelected(space.parkingId)
                                expanded = false
                            },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.requestedHour.toString(),
                onValueChange = { v -> v.toIntOrNull()?.let(onHourChange) },
                label = { Text("Requested hour (0–23)") },
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Day of week: ${dayLabels.getOrElse(uiState.dayOfWeek) { "?" }}")
            Slider(
                value = uiState.dayOfWeek.toFloat(),
                onValueChange = { onDayChange(it.toInt()) },
                valueRange = 0f..6f,
                steps = 5,
            )

            Text("Quick scenarios", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = onApplyLowDemandPreset, label = { Text("Low demand") })
                AssistChip(onClick = onApplyHighDemandPreset, label = { Text("High demand") })
            }

            ToggleRow("Public holiday", uiState.publicHoliday, onHolidayToggle)
            ToggleRow("Event nearby", uiState.eventNearby, onEventToggle)

            LabeledSlider("Avg occupancy (last 7 days)", uiState.avgOccupancy, 0f..1f, onOccupancyChange)
            LabeledSlider(
                "Avg parking duration (hours)",
                uiState.avgParkingDurationHours,
                0.5f..10f,
                onDurationHoursChange,
            )
            LabeledSlider(
                "Cancellations (last 7 days)",
                uiState.cancellationCountWeek,
                0f..7f,
                onCancellationCountChange,
            )

            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(onClick = onPredict, modifier = Modifier.fillMaxWidth(), enabled = !uiState.loading) {
                if (uiState.loading) CircularProgressIndicator() else Text("Run ML prediction")
            }

            uiState.result?.let { result ->
                ResultCard(result.available, result.confidence, result.probability)
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
) {
    Text("$label: ${"%.2f".format(value)}")
    Slider(value = value, onValueChange = onChange, valueRange = range)
}

@Composable
private fun ResultCard(available: Boolean, confidence: Double, probability: Double) {
    val color = if (available) ParkingAvailable else ParkingUnavailable
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                if (available) "Likely AVAILABLE" else "Likely UNAVAILABLE",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Text("Availability confidence ${(confidence * 100).format(1)}%")
            Text(
                "P(available) = ${(probability * 100).format(1)}% · P(unavailable) = ${((1 - probability) * 100).format(1)}%",
                style = MaterialTheme.typography.bodySmall,
            )
            LinearProgressIndicator(
                progress = { confidence.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
        }
    }
}

private fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)
