package com.example.androidsmartpredictparkinggihan.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.androidsmartpredictparkinggihan.data.model.ParkingListingResult

@Composable
fun BookParkingDialog(
    listing: ParkingListingResult,
    bookedHours: Int,
    processing: Boolean,
    error: String?,
    onHoursChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onPay: () -> Unit,
) {
    val space = listing.space
    val total = space.basePriceLkr * bookedHours
    AlertDialog(
        onDismissRequest = { if (!processing) onDismiss() },
        title = { Text("Book & pay") },
        text = {
            Column {
                Text(space.title, fontWeight = FontWeight.SemiBold)
                Text("${space.parkingId} · ${space.location}", style = MaterialTheme.typography.bodySmall)
                Text(
                    "Rate LKR ${space.basePriceLkr.format(0)} / hr",
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text("Duration: $bookedHours hour(s)")
                Slider(
                    value = bookedHours.toFloat(),
                    onValueChange = { onHoursChange(it.toInt()) },
                    valueRange = 1f..12f,
                    steps = 10,
                )
                Text(
                    "Total: LKR ${total.format(0)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    "Simulated secure card payment (demo)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = onPay, enabled = !processing) {
                if (processing) CircularProgressIndicator() else Text("Pay & confirm booking")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !processing) { Text("Cancel") }
        },
    )
}

private fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)
