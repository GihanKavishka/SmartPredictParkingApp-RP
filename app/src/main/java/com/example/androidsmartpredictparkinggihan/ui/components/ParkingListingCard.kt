package com.example.androidsmartpredictparkinggihan.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.androidsmartpredictparkinggihan.data.model.ParkingListingResult
import com.example.androidsmartpredictparkinggihan.ui.theme.ParkingAvailable
import com.example.androidsmartpredictparkinggihan.ui.theme.ParkingUnavailable

@Composable
fun ParkingListingCard(
    result: ParkingListingResult,
    modifier: Modifier = Modifier,
    onBook: (() -> Unit)? = null,
) {
    val space = result.space
    val prediction = result.prediction
    val available = prediction.available
    val badgeColor = if (available) ParkingAvailable else ParkingUnavailable
    val badgeText = if (available) "Likely available" else "Likely unavailable"

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(space.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Surface(color = badgeColor.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                    Text(
                        badgeText,
                        color = badgeColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            Text("${space.parkingId} · ${space.location}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "LKR ${"%.0f".format(space.basePriceLkr)} / hr · ${space.securityLevel}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Availability confidence ${(prediction.confidence * 100).format(1)}% · P(available) ${(prediction.probability * 100).format(1)}%",
                style = MaterialTheme.typography.bodySmall,
            )
            if (onBook != null) {
                Button(onClick = onBook, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("Book & pay")
                }
            }
        }
    }
}

private fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)
