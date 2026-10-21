package com.example.androidsmartpredictparkinggihan.ml

import kotlin.math.exp
import kotlin.math.sqrt
import kotlin.random.Random

class LogisticRegressionModel(
    val weights: DoubleArray,
    val bias: Double,
    val featureMeans: DoubleArray,
    val featureStds: DoubleArray,
    val decisionThreshold: Double,
) {
    fun scale(raw: DoubleArray): DoubleArray {
        return DoubleArray(raw.size) { index ->
            (raw[index] - featureMeans[index]) / featureStds[index]
        }
    }

    fun probability(rawFeatures: DoubleArray): Double {
        val features = scale(rawFeatures)
        var z = bias
        for (i in features.indices) {
            z += weights[i] * features[i]
        }
        return sigmoid(z)
    }

    fun predict(rawFeatures: DoubleArray): PredictionResult {
        val prob = probability(rawFeatures)
        val available = prob >= decisionThreshold
        val confidence = if (available) prob else 1.0 - prob
        return PredictionResult(available = available, confidence = confidence, probability = prob)
    }

    companion object {
        fun train(
            dataset: List<ParkingTrainingRow>,
            epochs: Int = 800,
            learningRate: Double = 0.05,
            testRatio: Double = 0.2,
            seed: Int = 42,
        ): Pair<LogisticRegressionModel, ModelMetrics> {
            require(dataset.isNotEmpty()) { "Training dataset is empty" }
            val random = Random(seed)
            val shuffled = dataset.shuffled(random)
            val testSize = (shuffled.size * testRatio).toInt().coerceAtLeast(1)
            val testSet = shuffled.take(testSize)
            val trainSet = shuffled.drop(testSize)

            val rawTrain = trainSet.map { FeatureEncoder.encode(it) }
            val means = computeMeans(rawTrain)
            val stds = computeStds(rawTrain, means)

            val positiveCount = trainSet.count { it.available == 1 }.coerceAtLeast(1)
            val negativeCount = trainSet.size - positiveCount
            val positiveWeight = negativeCount.toDouble() / positiveCount.toDouble()

            val weights = DoubleArray(FeatureEncoder.FEATURE_SIZE) { 0.0 }
            var bias = 0.0

            repeat(epochs) {
                var gradBias = 0.0
                val grads = DoubleArray(weights.size)
                trainSet.forEach { row ->
                    val raw = FeatureEncoder.encode(row)
                    val features = scale(raw, means, stds)
                    val y = row.available.toDouble()
                    val sampleWeight = if (y == 1.0) positiveWeight else 1.0
                    val pred = sigmoid(dot(weights, bias, features))
                    val error = (pred - y) * sampleWeight
                    gradBias += error
                    for (i in features.indices) {
                        grads[i] += error * features[i]
                    }
                }
                val n = trainSet.size.toDouble()
                bias -= learningRate * (gradBias / n)
                for (i in weights.indices) {
                    weights[i] -= learningRate * (grads[i] / n)
                }
            }

            val threshold = findBestThreshold(weights, bias, means, stds, trainSet)
            val model = LogisticRegressionModel(weights, bias, means, stds, threshold)
            val metrics = evaluate(model, testSet, trainSet.size, testSet.size, dataset)
            return model to metrics
        }

        private fun findBestThreshold(
            weights: DoubleArray,
            bias: Double,
            means: DoubleArray,
            stds: DoubleArray,
            trainSet: List<ParkingTrainingRow>,
        ): Double {
            var bestThreshold = 0.5
            var bestF1 = -1.0
            var candidate = 0.30
            while (candidate <= 0.70) {
                val f1 = f1AtThreshold(weights, bias, means, stds, trainSet, candidate)
                if (f1 > bestF1) {
                    bestF1 = f1
                    bestThreshold = candidate
                }
                candidate += 0.02
            }
            return bestThreshold
        }

        private fun f1AtThreshold(
            weights: DoubleArray,
            bias: Double,
            means: DoubleArray,
            stds: DoubleArray,
            trainSet: List<ParkingTrainingRow>,
            threshold: Double,
        ): Double {
            var tp = 0
            var fp = 0
            var fn = 0
            trainSet.forEach { row ->
                val raw = FeatureEncoder.encode(row)
                val features = scale(raw, means, stds)
                val prob = sigmoid(dot(weights, bias, features))
                val pred = prob >= threshold
                val actual = row.available == 1
                when {
                    pred && actual -> tp++
                    pred && !actual -> fp++
                    !pred && actual -> fn++
                }
            }
            val precision = if (tp + fp == 0) 0.0 else tp.toDouble() / (tp + fp)
            val recall = if (tp + fn == 0) 0.0 else tp.toDouble() / (tp + fn)
            return if (precision + recall == 0.0) 0.0 else 2 * precision * recall / (precision + recall)
        }

        private fun computeMeans(rows: List<DoubleArray>): DoubleArray {
            val size = rows.first().size
            val means = DoubleArray(size)
            rows.forEach { row ->
                for (i in row.indices) {
                    means[i] += row[i]
                }
            }
            val n = rows.size.toDouble()
            for (i in means.indices) {
                means[i] /= n
            }
            return means
        }

        private fun computeStds(rows: List<DoubleArray>, means: DoubleArray): DoubleArray {
            val size = means.size
            val vars = DoubleArray(size)
            rows.forEach { row ->
                for (i in row.indices) {
                    val diff = row[i] - means[i]
                    vars[i] += diff * diff
                }
            }
            val n = rows.size.toDouble()
            return DoubleArray(size) { index ->
                sqrt(vars[index] / n).coerceAtLeast(1e-6)
            }
        }

        private fun scale(raw: DoubleArray, means: DoubleArray, stds: DoubleArray): DoubleArray {
            return DoubleArray(raw.size) { index ->
                (raw[index] - means[index]) / stds[index]
            }
        }

        private fun evaluate(
            model: LogisticRegressionModel,
            testSet: List<ParkingTrainingRow>,
            trainCount: Int,
            testCount: Int,
            fullDataset: List<ParkingTrainingRow>,
        ): ModelMetrics {
            var tp = 0
            var fp = 0
            var fn = 0
            var tn = 0
            testSet.forEach { row ->
                val pred = model.predict(FeatureEncoder.encode(row)).available
                val actual = row.available == 1
                when {
                    pred && actual -> tp++
                    pred && !actual -> fp++
                    !pred && actual -> fn++
                    else -> tn++
                }
            }
            val total = tp + fp + fn + tn
            val accuracy = if (total == 0) 0.0 else (tp + tn).toDouble() / total
            val precision = if (tp + fp == 0) 0.0 else tp.toDouble() / (tp + fp)
            val recall = if (tp + fn == 0) 0.0 else tp.toDouble() / (tp + fn)
            val f1 = if (precision + recall == 0.0) 0.0 else 2 * precision * recall / (precision + recall)
            val availRate = fullDataset.count { it.available == 1 }.toDouble() / fullDataset.size
            return ModelMetrics(
                accuracy = accuracy,
                precision = precision,
                recall = recall,
                f1Score = f1,
                trainingSamples = trainCount,
                testSamples = testCount,
                csvAvailabilityRate = availRate,
            )
        }

        private fun dot(weights: DoubleArray, bias: Double, x: DoubleArray): Double {
            var z = bias
            for (i in x.indices) {
                z += weights[i] * x[i]
            }
            return z
        }

        private fun sigmoid(z: Double): Double = 1.0 / (1.0 + exp(-z))
    }
}
