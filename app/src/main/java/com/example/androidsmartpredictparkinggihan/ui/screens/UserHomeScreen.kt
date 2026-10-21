package com.example.androidsmartpredictparkinggihan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.androidsmartpredictparkinggihan.ui.components.BookParkingDialog
import com.example.androidsmartpredictparkinggihan.ui.components.ParkingListingCard
import com.example.androidsmartpredictparkinggihan.ui.components.StatCard
import com.example.androidsmartpredictparkinggihan.data.model.ParkingListingResult
import com.example.androidsmartpredictparkinggihan.ui.viewmodel.UserHomeUiState

private val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    userName: String,
    uiState: UserHomeUiState,
    onAdvancedPredict: () -> Unit,
    onLogout: () -> Unit,
    onLocationChange: (String) -> Unit,
    onMaxPriceChange: (Float) -> Unit,
    onHourChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
    onHolidayToggle: (Boolean) -> Unit,
    onEventToggle: (Boolean) -> Unit,
    onOnlyAvailableToggle: (Boolean) -> Unit,
    onSearch: () -> Unit,
    onBook: (ParkingListingResult) -> Unit,
    onCloseBooking: () -> Unit,
    onBookHoursChange: (Int) -> Unit,
    onPayAndBook: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Predict Parking") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.loading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Hello, $userName", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Search peer-to-peer parking with ML availability prediction",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Model ready · ${uiState.csvRows} CSV records", fontWeight = FontWeight.SemiBold)
                        uiState.modelMetrics?.let {
                            Text("Accuracy ${(it.accuracy * 100).format(1)}% · F1 ${(it.f1Score * 100).format(1)}%")
                        }
                        uiState.modelError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = onAdvancedPredict) { Text("Advanced single-slot prediction") }
                    }
                }
            }
            item {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        RowTitle(Icons.Default.Tune, "Your requirements")
                        OutlinedTextField(
                            value = uiState.criteria.locationQuery,
                            onValueChange = onLocationChange,
                            label = { Text("Location / area (e.g. Colombo Fort)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        Text("Max price: LKR ${uiState.criteria.maxPriceLkr.format(0)}")
                        Slider(
                            value = uiState.criteria.maxPriceLkr.toFloat(),
                            onValueChange = onMaxPriceChange,
                            valueRange = 100f..600f,
                        )
                        OutlinedTextField(
                            value = uiState.criteria.requestedHour.toString(),
                            onValueChange = { v -> v.toIntOrNull()?.let(onHourChange) },
                            label = { Text("Requested hour (0–23)") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text("Day: ${dayLabels.getOrElse(uiState.criteria.dayOfWeek) { "?" }}")
                        Slider(
                            value = uiState.criteria.dayOfWeek.toFloat(),
                            onValueChange = { onDayChange(it.toInt()) },
                            valueRange = 0f..6f,
                            steps = 5,
                        )
                        ToggleLine("Public holiday", uiState.criteria.publicHoliday, onHolidayToggle)
                        ToggleLine("Event nearby", uiState.criteria.eventNearby, onEventToggle)
                        ToggleLine("Show only likely available", uiState.criteria.onlyShowPredictedAvailable, onOnlyAvailableToggle)
                        Button(
                            onClick = onSearch,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.searching && uiState.modelError == null,
                        ) {
                            if (uiState.searching) {
                                CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                    Text("Search parking spaces", modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                        uiState.searchError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        uiState.bookingMessage?.let {
                            Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
            if (uiState.myBookings.isNotEmpty()) {
                item {
                    Text("My bookings", fontWeight = FontWeight.SemiBold)
                }
                items(uiState.myBookings, key = { it.id }) { booking ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("${booking.parkingId} · ${booking.spaceTitle}", fontWeight = FontWeight.Medium)
                            Text("LKR ${booking.totalPaidLkr.format(0)} · ${booking.bookedHours} hr · ${booking.paymentStatus.name}")
                            Text(booking.location, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            item {
                StatCard(
                    title = if (uiState.hasSearched) "Search results" else "All listed spaces",
                    value = if (uiState.hasSearched) uiState.searchResults.size.toString() else uiState.allSpaces.size.toString(),
                    subtitle = "Sorted by availability probability",
                )
            }
            if (uiState.hasSearched) {
                if (uiState.searchResults.isEmpty()) {
                    item {
                        Text(
                            "No spaces match your filters. Try a wider area or higher max price.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else {
                    items(uiState.searchResults, key = { it.space.parkingId }) { result ->
                        ParkingListingCard(result, onBook = { onBook(result) })
                    }
                }
            } else {
                items(uiState.allSpaces, key = { it.parkingId }) { space ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(space.title, fontWeight = FontWeight.SemiBold)
                            Text("${space.parkingId} · ${space.location}")
                            Text(
                                "LKR ${space.basePriceLkr.format(0)} / hr · ${space.securityLevel}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                "Tap Search to run availability prediction",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    uiState.bookingTarget?.let { target ->
        BookParkingDialog(
            listing = target,
            bookedHours = uiState.bookHours,
            processing = uiState.bookingProcessing,
            error = uiState.bookingError,
            onHoursChange = onBookHoursChange,
            onDismiss = onCloseBooking,
            onPay = onPayAndBook,
        )
    }
}

@Composable
private fun RowTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(text, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun ToggleLine(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

private fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)
