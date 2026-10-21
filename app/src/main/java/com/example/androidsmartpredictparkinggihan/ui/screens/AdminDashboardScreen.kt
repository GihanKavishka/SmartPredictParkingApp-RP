package com.example.androidsmartpredictparkinggihan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.androidsmartpredictparkinggihan.ui.components.MultiColorBarChart
import com.example.androidsmartpredictparkinggihan.ui.components.SimpleBarChart
import com.example.androidsmartpredictparkinggihan.ui.components.StatCard
import com.example.androidsmartpredictparkinggihan.ui.viewmodel.AdminDashboardUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatisticsScreen(
    adminName: String,
    uiState: AdminDashboardUiState,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistical analytics") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.loading || uiState.stats == null) {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
            )
            return@Scaffold
        }
        val stats = uiState.stats
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Predictive Parking Slot Availability — statistical dashboard",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            item {
                StatCard("Registered users", stats.totalUsers.toString(), "${stats.totalDrivers} drivers · ${stats.totalAdmins} admins")
            }
            item {
                StatCard("Listed P2P spaces", stats.listedSpaces.toString())
            }
            item {
                StatCard(
                    "Predictions run in app",
                    stats.totalPredictions.toString(),
                    "${stats.predictedAvailableCount} predicted available",
                )
            }
            item {
                StatCard("CSV training rows", stats.csvRows.toString())
            }
            stats.modelMetrics?.let { m ->
                item {
                    StatCard(
                        title = "Model test accuracy",
                        value = "${(m.accuracy * 100).format(1)}%",
                        subtitle = "Precision ${(m.precision * 100).format(1)}% · Recall ${(m.recall * 100).format(1)}% · F1 ${(m.f1Score * 100).format(1)}%",
                    )
                }
                item {
                    StatCard(
                        title = "Historical availability (CSV)",
                        value = "${(m.csvAvailabilityRate * 100).format(1)}%",
                        subtitle = "Train ${m.trainingSamples} · Test ${m.testSamples} samples",
                    )
                }
            }
            item {
                SimpleBarChart(
                    title = "CSV availability rate by hour",
                    data = stats.csvAvailabilityByHour.map { (hour, rate) ->
                        hour.toString() to rate.toFloat()
                    },
                )
            }
            item {
                MultiColorBarChart(
                    title = "CSV occupancy distribution",
                    data = stats.occupancyBuckets.map { (label, count) -> label to count.toFloat() },
                )
            }
            item {
                SimpleBarChart(
                    title = "In-app predictions by hour",
                    data = stats.predictionsByHour.map { (hour, count) -> hour.toString() to count.toFloat() },
                )
            }
            item {
                SimpleBarChart(
                    title = "In-app predictions by day",
                    data = stats.predictionsByDay.map { (day, count) ->
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").getOrElse(day) { day.toString() } to count.toFloat()
                    },
                )
            }
            items(stats.csvAvailabilityByHour.take(6)) { (hour, rate) ->
                Text(
                    "Hour $hour historical availability: ${(rate * 100).format(1)}%",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)
