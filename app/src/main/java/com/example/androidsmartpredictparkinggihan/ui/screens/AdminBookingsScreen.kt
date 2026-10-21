package com.example.androidsmartpredictparkinggihan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.androidsmartpredictparkinggihan.ui.viewmodel.AdminBookingsUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBookingsScreen(
    uiState: AdminBookingsUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All bookings") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
                actions = { TextButton(onClick = onRefresh) { Text("Refresh") } },
            )
        },
    ) { padding ->
        if (uiState.loading) {
            CircularProgressIndicator(Modifier.fillMaxSize().padding(padding).padding(24.dp))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    "${uiState.bookings.size} booking(s) stored in database",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (uiState.bookings.isEmpty()) {
                item { Text("No bookings yet.") }
            } else {
                items(uiState.bookings, key = { it.id }) { booking ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${booking.parkingId} · ${booking.spaceTitle}", fontWeight = FontWeight.SemiBold)
                            Text("Driver: ${booking.driverName} (${booking.driverEmail})")
                            Text("${booking.location} · ${booking.bookedHours} hr @ LKR ${booking.hourlyRateLkr.format(0)}/hr")
                            Text(
                                "Paid LKR ${booking.totalPaidLkr.format(0)} · ${booking.paymentMethod} · ${booking.paymentStatus.name}",
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                "Start hour ${booking.requestedHour}:00 · day index ${booking.dayOfWeek}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                formatTime(booking.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(epochMs: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(epochMs))

private fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)
