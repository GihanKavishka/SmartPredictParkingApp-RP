package com.example.androidsmartpredictparkinggihan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.androidsmartpredictparkinggihan.ui.components.StatCard
import com.example.androidsmartpredictparkinggihan.ui.viewmodel.AdminDashboardUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    adminName: String,
    uiState: AdminDashboardUiState,
    onOpenStatistics: () -> Unit,
    onOpenUserManagement: () -> Unit,
    onOpenBookings: () -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin dashboard") },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.loading || uiState.stats == null) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            )
            return@Scaffold
        }
        val stats = uiState.stats
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Welcome, $adminName", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Manage Smart Predict Parking — availability component",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                StatCard(
                    "Registered users",
                    stats.totalUsers.toString(),
                    "${stats.totalDrivers} drivers · ${stats.totalHouseOwners} house owners · ${stats.totalAdmins} admins",
                )
            }
            item { StatCard("Listed P2P parking spaces", stats.listedSpaces.toString()) }
            item { StatCard("Paid bookings", stats.totalBookings.toString(), "All booking records in DB") }
            item {
                StatCard(
                    "Availability predictions logged",
                    stats.totalPredictions.toString(),
                    "${stats.predictedAvailableCount} marked likely available",
                )
            }
            item { StatCard("CSV training dataset", "${stats.csvRows} rows") }
            stats.modelMetrics?.let { m ->
                item {
                    StatCard(
                        title = "ML model (test split)",
                        value = "${(m.accuracy * 100).format(1)}% accuracy",
                        subtitle = "Precision ${(m.precision * 100).format(1)}% · Recall ${(m.recall * 100).format(1)}%",
                    )
                }
            }
            item {
                Button(onClick = onOpenBookings, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null)
                    Text("  View all bookings", modifier = Modifier.padding(start = 8.dp))
                }
            }
            item {
                Button(onClick = onOpenUserManagement, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Group, contentDescription = null)
                    Text("  User management panel", modifier = Modifier.padding(start = 8.dp))
                }
            }
            item {
                Button(onClick = onOpenStatistics, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Analytics, contentDescription = null)
                    Text("  Statistical analytics dashboard", modifier = Modifier.padding(start = 8.dp))
                }
            }
            item {
                OutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
                    Text("Refresh overview")
                }
            }
            item {
                Column(Modifier.padding(top = 8.dp)) {
                    Text("Quick tips", fontWeight = FontWeight.SemiBold)
                    Text("• Users search spaces on the home screen with ML predictions per slot.", style = MaterialTheme.typography.bodySmall)
                    Text("• Statistical charts and CSV breakdowns are in the analytics dashboard.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)
