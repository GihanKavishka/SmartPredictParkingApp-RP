package com.example.androidsmartpredictparkinggihan.ml

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

object CsvDatasetLoader {
    const val ASSET_FILE = "parking_availability_training.csv"

    private val dayNameToIndex = mapOf(
        "mon" to 0, "monday" to 0,
        "tue" to 1, "tues" to 1, "tuesday" to 1,
        "wed" to 2, "wednesday" to 2,
        "thu" to 3, "thur" to 3, "thurs" to 3, "thursday" to 3,
        "fri" to 4, "friday" to 4,
        "sat" to 5, "saturday" to 5,
        "sun" to 6, "sunday" to 6,
    )

    fun load(context: Context): List<ParkingTrainingRow> {
        return try {
            context.assets.open(ASSET_FILE).use { stream ->
                BufferedReader(InputStreamReader(stream)).use { reader ->
                    val lines = reader.lineSequence()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .toList()
                    if (lines.size < 2) return emptyList()
                    val header = parseCsvLine(lines.first())
                    lines.drop(1).mapNotNull { parseLine(parseCsvLine(it), header) }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseLine(cols: List<String>, header: List<String>): ParkingTrainingRow? {
        if (cols.size < header.size) return null
        val map = header.indices.associate { index ->
            normalizeHeader(header[index]) to cols[index].trim()
        }
        return try {
            ParkingTrainingRow(
                parkingId = map.require("Parking_ID"),
                requestedHour = parseRequestedHour(map),
                dayOfWeek = parseDayOfWeek(map.require("Day_of_Week")),
                publicHoliday = parseBinary(map.require("Public_Holiday")),
                avgOccupancy = parseOccupancy(map),
                avgParkingDuration = parseDurationMinutes(map),
                cancellationRate = parseCancellationRate(map),
                eventNearby = parseBinary(map.require("Event_Nearby")),
                available = parseAvailable(map.require("Available")),
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        line.forEach { ch ->
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(ch)
            }
        }
        result.add(current.toString())
        return result.map { it.trim().removeSurrounding("\"") }
    }

    private fun normalizeHeader(raw: String): String {
        val cleaned = raw.trim().replace("\uFEFF", "")
        return when {
            cleaned.equals("Parking_ID", true) -> "Parking_ID"
            cleaned.startsWith("Requested", true) -> "Requested"
            cleaned.startsWith("Day_of_Week", true) || cleaned.startsWith("Day_of_We", true) -> "Day_of_Week"
            cleaned.startsWith("Public_Holiday", true) || cleaned.startsWith("Public_Hol", true) -> "Public_Holiday"
            cleaned.contains("Occupancy", true) || cleaned.startsWith("Avg_Occup", true) -> "Avg_Occupancy"
            cleaned.contains("Parking_Duration", true) || cleaned.startsWith("Avg_Parkin", true) -> "Avg_Parking_Duration"
            cleaned.contains("Cancellation", true) || cleaned.startsWith("Cancellatio", true) -> "Cancellation"
            cleaned.startsWith("Event_Nearby", true) || cleaned.startsWith("Event_Nea", true) -> "Event_Nearby"
            cleaned.equals("Available", true) -> "Available"
            else -> cleaned
        }
    }

    private fun Map<String, String>.require(key: String): String =
        this[key] ?: error("Missing column $key")

    private fun parseRequestedHour(map: Map<String, String>): Int {
        val raw = map["Requested"] ?: error("Missing Requested time")
        raw.toIntOrNull()?.let { return it.coerceIn(0, 23) }
        val parts = raw.split(":")
        if (parts.isNotEmpty()) {
            parts[0].toIntOrNull()?.let { return it.coerceIn(0, 23) }
        }
        error("Invalid requested time: $raw")
    }

    private fun parseDayOfWeek(raw: String): Int {
        raw.toIntOrNull()?.let { return it.coerceIn(0, 6) }
        return dayNameToIndex[raw.lowercase(Locale.US)]
            ?: dayNameToIndex[raw.take(3).lowercase(Locale.US)]
            ?: error("Invalid day: $raw")
    }

    private fun parseBinary(raw: String): Int = when (raw.lowercase(Locale.US)) {
        "1", "true", "yes" -> 1
        "0", "false", "no" -> 0
        else -> raw.toInt().coerceIn(0, 1)
    }

    private fun parseAvailable(raw: String): Int = when (raw.lowercase(Locale.US)) {
        "1", "true", "yes", "available" -> 1
        "0", "false", "no", "unavailable" -> 0
        else -> raw.toInt().coerceIn(0, 1)
    }

    private fun parseOccupancy(map: Map<String, String>): Double {
        val raw = map.require("Avg_Occupancy")
        return raw.toDouble().coerceIn(0.0, 1.0)
    }

    /** CSV uses hours (0.5–9.2); convert to minutes for the model. */
    private fun parseDurationMinutes(map: Map<String, String>): Double {
        val hours = map.require("Avg_Parking_Duration").toDouble()
        return if (hours <= 24.0) hours * 60.0 else hours
    }

    /** Count over last 7 days → rate in [0, 1]. */
    private fun parseCancellationRate(map: Map<String, String>): Double {
        val raw = map.require("Cancellation").toDouble()
        return if (raw <= 1.0) raw else (raw / 7.0).coerceIn(0.0, 1.0)
    }
}
