package com.example.androidsmartpredictparkinggihan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.androidsmartpredictparkinggihan.ui.viewmodel.OwnerHomeUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerHomeScreen(
    ownerName: String,
    uiState: OwnerHomeUiState,
    onLogout: () -> Unit,
    onParkingIdChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onSecurityChange: (String) -> Unit,
    onAddSpace: () -> Unit,
    onRemoveSpace: (String) -> Unit,
    onMarkNotificationsRead: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("House owner · $ownerName") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                },
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "List your driveway or garage for peer-to-peer parking",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Card(
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null)
                            Text(
                                "Booking notifications (${uiState.unreadBookingCount} unread)",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                        if (uiState.bookingAlerts.isEmpty()) {
                            Text("No bookings yet.", style = MaterialTheme.typography.bodySmall)
                        } else {
                            uiState.bookingAlerts.take(5).forEach { booking ->
                                Text(
                                    "${booking.driverName} booked ${booking.parkingId} · LKR ${"%.0f".format(booking.totalPaidLkr)}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                        OutlinedButton(onClick = onMarkNotificationsRead, modifier = Modifier.fillMaxWidth()) {
                            Text("Mark all as read")
                        }
                    }
                }
            }
            item {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AddHome, contentDescription = null)
                            Text("Add parking space", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                        }
                        OutlinedTextField(
                            value = uiState.parkingId,
                            onValueChange = onParkingIdChange,
                            label = { Text("Parking ID (e.g. H001)") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = uiState.title,
                            onValueChange = onTitleChange,
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = uiState.location,
                            onValueChange = onLocationChange,
                            label = { Text("Location / area") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = uiState.priceLkr,
                            onValueChange = onPriceChange,
                            label = { Text("Price LKR / hour") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = uiState.securityLevel,
                            onValueChange = onSecurityChange,
                            label = { Text("Security (e.g. CCTV + Gate)") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        uiState.error?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        uiState.message?.let {
                            Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = onAddSpace,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.saving,
                        ) {
                            if (uiState.saving) CircularProgressIndicator() else Text("Add to platform")
                        }
                    }
                }
            }
            item {
                Text("My listed spaces (${uiState.mySpaces.size})", fontWeight = FontWeight.SemiBold)
            }
            if (uiState.mySpaces.isEmpty()) {
                item {
                    Text("No spaces yet. Add your first listing above.", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                items(uiState.mySpaces, key = { it.parkingId }) { space ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(space.title, fontWeight = FontWeight.SemiBold)
                                Text("${space.parkingId} · ${space.location}")
                                Text(
                                    "LKR ${"%.0f".format(space.basePriceLkr)} / hr · ${space.securityLevel}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            IconButton(onClick = { onRemoveSpace(space.parkingId) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove")
                            }
                        }
                    }
                }
            }
        }
    }
}
