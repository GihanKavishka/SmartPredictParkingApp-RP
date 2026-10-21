package com.example.androidsmartpredictparkinggihan.ml

import android.content.Context
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.MessageDigest

data class ModelTrainingState(
    val isReady: Boolean = false,
    val datasetRows: Int = 0,
    val errorMessage: String? = null,
)

class AvailabilityPredictionService(private val context: Context) {

    private val mutex = Mutex()
    private var model: LogisticRegressionModel? = null
    private var metrics: ModelMetrics? = null
    private var datasetSize: Int = 0
    private var trainingState: ModelTrainingState = ModelTrainingState()
    private var cachedCsvHash: String? = null

    private val prefs by lazy {
        context.getSharedPreferences("ml_training", Context.MODE_PRIVATE)
    }

    suspend fun ensureModelTrained(force: Boolean = false) {
        mutex.withLock {
            val hash = readAssetHash()
            val storedHash = prefs.getString(PREF_CSV_HASH, null)
            val modelVersion = prefs.getInt(PREF_MODEL_VERSION, 0)
            val mustRetrain =
                force || model == null || storedHash != hash || modelVersion != MODEL_VERSION

            if (!mustRetrain) {
                trainingState = trainingState.copy(isReady = true, datasetRows = datasetSize)
                return
            }

            trainingState = ModelTrainingState(isReady = false)
            try {
                val rows = CsvDatasetLoader.load(context)
                datasetSize = rows.size
                if (rows.isEmpty()) {
                    trainingState = ModelTrainingState(
                        isReady = false,
                        datasetRows = 0,
                        errorMessage = "CSV not found or no valid rows. Place parking_availability_training.csv in app assets.",
                    )
                    model = null
                    metrics = null
                    return
                }
                val (trained, trainedMetrics) = LogisticRegressionModel.train(rows)
                model = trained
                metrics = trainedMetrics
                cachedCsvHash = hash
                prefs.edit()
                    .putString(PREF_CSV_HASH, hash)
                    .putInt(PREF_MODEL_VERSION, MODEL_VERSION)
                    .apply()
                trainingState = ModelTrainingState(isReady = true, datasetRows = rows.size)
            } catch (e: Exception) {
                model = null
                metrics = null
                trainingState = ModelTrainingState(
                    isReady = false,
                    datasetRows = datasetSize,
                    errorMessage = e.message ?: "Model training failed",
                )
            }
        }
    }

    fun getTrainingState(): ModelTrainingState = trainingState

    suspend fun predict(input: PredictionInput): PredictionResult {
        ensureModelTrained()
        val trained = model ?: error(trainingState.errorMessage ?: "Model not trained yet")
        return trained.predict(FeatureEncoder.encode(input))
    }

    suspend fun getMetrics(): ModelMetrics? {
        ensureModelTrained()
        return metrics
    }

    suspend fun getDatasetSize(): Int {
        ensureModelTrained()
        return datasetSize
    }

    suspend fun availabilityByHourFromCsv(): List<Pair<Int, Double>> {
        val rows = CsvDatasetLoader.load(context)
        if (rows.isEmpty()) return emptyList()
        return rows.groupBy { it.requestedHour }
            .toSortedMap()
            .map { (hour, group) ->
                hour to group.count { it.available == 1 }.toDouble() / group.size
            }
    }

    suspend fun occupancyDistributionFromCsv(): List<Pair<String, Double>> {
        val rows = CsvDatasetLoader.load(context)
        if (rows.isEmpty()) return emptyList()
        val buckets = listOf(
            "Low (0-0.3)" to (0.0..0.3),
            "Medium (0.3-0.6)" to (0.3..0.6),
            "High (0.6-1.0)" to (0.6..1.0),
        )
        return buckets.map { (label, range) ->
            val count = rows.count { it.avgOccupancy in range }
            label to count.toDouble()
        }
    }

    private fun readAssetHash(): String {
        return try {
            context.assets.open(CsvDatasetLoader.ASSET_FILE).use { stream ->
                val digest = MessageDigest.getInstance("SHA-256")
                val buffer = ByteArray(8192)
                var read = stream.read(buffer)
                while (read > 0) {
                    digest.update(buffer, 0, read)
                    read = stream.read(buffer)
                }
                digest.digest().joinToString("") { "%02x".format(it) }
            }
        } catch (_: Exception) {
            "missing"
        }
    }

    companion object {
        private const val PREF_CSV_HASH = "csv_sha256"
        private const val PREF_MODEL_VERSION = "model_version"
        private const val MODEL_VERSION = 2
    }
}
