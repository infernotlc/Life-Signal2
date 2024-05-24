package com.tlh.bluetooth.data.repo

import android.util.Log
import com.tlh.bluetooth.common.DataCarrier
import com.tlh.bluetooth.data.entities.EventInterestOutput
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

class ParametersRepoImpl :ParametersRepo {

    override fun calculateAcgParameters(
        eventHolder: EventInterestOutput
    ): DoubleArray? {

        val timeSeconds = DataCarrier.temp.map {
            it.time
        }

        val magnitude = DataCarrier.temp.map {
            it.magnitude
        }
        val acgXyz = DataCarrier.temp.map {
            it.values
        }
        val changeInAngleValue = changeInAngle(acgXyz)
        Log.i("changeInAngleValue","$changeInAngleValue")

        val changeInAngleCosValue = changeInAngleCos(
            timeSeconds, acgXyz, eventHolder.begin, eventHolder.end
        )
        Log.i("changeInAngleCosValue","$changeInAngleCosValue")

        if (changeInAngleCosValue.isNaN()) {
            return null
        }

        val angleDeviation = ad(acgXyz)
        Log.i("angleDeviation","$angleDeviation")

        val ffi = freeFallIndex(magnitude.toDoubleArray(), eventHolder)
        Log.i("ffi","$ffi")

        val mm = minMax(eventHolder.magnitudeList.map {
            it.magnitude
        }.toDoubleArray())
        Log.i("mm","$mm")

        val ratio = ratio3g(eventHolder.fromFreeFall())
        Log.i("ratio","$ratio")

        val kurt = kurtosis(eventHolder.fromFreeFall())
        Log.i("kurt","$kurt")

        val skew = skewness(eventHolder.fromFreeFall())
        Log.i("skew","$skew")

        val oneGCrosses = gCrossRate(eventHolder.fromFreeFall(), threshold = 9.25)

        Log.i("oneGCrosses","$oneGCrosses")


        return basicStats(eventHolder.magnitudeList.map {
            it.magnitude
        }.toDoubleArray()) + doubleArrayOf(
            changeInAngleValue,
            changeInAngleCosValue,
            angleDeviation,
            ffi,
            mm,
            ratio,
            kurt,
            skew,
            oneGCrosses
        )
    }

    override fun basicStats(eventMagnitude: DoubleArray): DoubleArray {
        val avg = eventMagnitude.average()
        val deviation = standardDeviation(eventMagnitude)
        val hjorthParams = hjorthParams(eventMagnitude)
        val tkeo = avgTkeo(eventMagnitude)
        val output = avgOutput(eventMagnitude)
        val entropy = apEn(eventMagnitude, 10, 3.0)
        val wave = waveformLength(eventMagnitude)
        val crest = crestFactor(eventMagnitude)

        return doubleArrayOf(
            avg,
            deviation,
            hjorthParams[0],  // Variance
            hjorthParams[1],  // Mobility
            hjorthParams[2],  // Complexity
            tkeo,
            output,
            entropy,
            wave,
            crest
        )
    }


    override fun changeInAngle(
        sensorValues:
        List<List<Float>>
    ): Double {
        return sensorValues.filterIndexed { index, _ -> index == 0 || index == 2 }  // Filter X and Z axis
            .map { vector -> sqrt(vector[0].pow(2) + vector[1].pow(2)) }  // Euclidean norm
            .average()  // Mean of the norms
    }

    override fun changeInAngleCos(
        timeSeconds: List<Long>,
        acgXyz: List<List<Float>>,
        beginIndex: Int,
        endIndex: Int
    ): Double {
        // Pick one second before and after the event
        val (valuesXyzBefore, valuesXyzAfter) = beforeAndAfterFall(
            timeSeconds,
            acgXyz,
            beginIndex,
            endIndex
        )

        // Check if either of the before or after arrays are empty
        if (valuesXyzBefore.isEmpty() || valuesXyzAfter.isEmpty()) {
            println("Empty")
            // return null
        }

        val aa = valuesXyzBefore.map { vector ->
            sqrt(
                vector[0].pow(2) + vector[1].pow(2) + vector[2].pow(2)
            )
        }

        val ab = valuesXyzAfter.map { vector ->
            sqrt(
                vector[0].pow(2) + vector[1].pow(2) + vector[2].pow(2)
            )
        }


        val acca = sqrt(aa.average())
        val accb = sqrt(ab.average())

        val dotProduct = dot1D(aa,ab)
        val angleCos = dotProduct / (acca * accb)

        return Math.toDegrees(acos(angleCos))
    }
    private fun dot1D(l1: List<Float>, l2: List<Float>): Double {

        val size = minOf(l1.size, l2.size)

        var dotProduct = 0.0
        for (i in 0 until size) {
            dotProduct += l1[i] * l2[i]
        }
        return dotProduct
    }



    override fun beforeAndAfterFall(
        timeSeconds: List<Long>,
        acgXyz: List<List<Float>>,
        beginIndex: Int,
        endIndex: Int
    ): Pair<List<List<Float>>,
            List<List<Float>>> {
        var timeBeginBeforeIndex: Int? = null
        var timeEndBeforeIndex = beginIndex - 1

        var timeBeginAfterIndex = endIndex + 1
        var timeEndAfterIndex: Int? = null

        // Searching for the beginning
        for (i in timeEndBeforeIndex downTo 0) {
            if (abs(timeSeconds[timeEndBeforeIndex] - timeSeconds[i]) >= 1.0) {
                timeBeginBeforeIndex = i
                break
            }
        }

        if (timeBeginBeforeIndex == null) {
            timeBeginBeforeIndex = 0
        }

        // Searching for the end
        for (i in timeBeginAfterIndex until timeSeconds.size) {
            if (abs(timeSeconds[timeBeginAfterIndex] - timeSeconds[i]) >= 1.0) {
                timeEndAfterIndex = i
                break
            }
        }

        if (timeEndAfterIndex == null) {
            timeEndAfterIndex = timeSeconds.size
        }

        val beforeArray = acgXyz.slice(timeBeginBeforeIndex..timeEndBeforeIndex)
        val afterArray = acgXyz.slice(timeBeginAfterIndex..timeEndAfterIndex)

        return Pair(beforeArray, afterArray)
    }

    override fun ad(values: List<List<Float>>): Double {
        var summation = 0.0
        var passed = 0

        var previousNorm = sqrt(values[0][0].pow(2) + values[1][0].pow(2) + values[2][0].pow(2))
        for (v in 0 until values[0].size - 1) {
            val newNorm =
                sqrt(values[0][v + 1].pow(2) + values[1][v + 1].pow(2) + values[2][v + 1].pow(2))
            val multiplication = previousNorm * newNorm
            previousNorm = newNorm

            val dotProduct =
                values[0][v] * values[0][v + 1] + values[1][v] * values[1][v + 1] + values[2][v] * values[2][v + 1]
            val division = dotProduct / multiplication

            // Ignoring warning, because it is resolved
            val arc = if (division <= 1 && division >= -1) acos(division.toDouble()) else 0.0

            // 90° results in NaN - ignoring warning, because it is resolved
            if (arc.isNaN()) {
                passed++
            } else {
                summation += Math.toDegrees(arc)
            }
        }
        return summation / (values[0].size - 1 - passed)
    }

    override fun freeFallIndex(
        valuesAcg:
        DoubleArray, eventOfInterest: EventInterestOutput
    ): Double {
        if (eventOfInterest.freeFallEnd == null) {
            return 10.0
        }
        val acgSlice = valuesAcg.slice(eventOfInterest.begin until eventOfInterest.freeFallEnd)
        return acgSlice.average()
    }

    override fun minMax(
        magnitude:
        DoubleArray
    ): Double {
        return magnitude.maxOrNull()!! - magnitude.minOrNull()!!
    }

    override fun ratio3g(magnitude: DoubleArray, threshold: Double): Double {
        val aboveThresholdCount = magnitude.count { it > threshold }
        val belowThresholdCount = magnitude.count { it < threshold }

        return if (belowThresholdCount != 0) aboveThresholdCount.toDouble() / belowThresholdCount.toDouble() else 0.0
    }

    override fun kurtosis(magnitude: DoubleArray): Double {
        val fourthMoment = momentum(magnitude, 4)
        val secondMoment = momentum(magnitude, 2)
        return fourthMoment / (secondMoment * secondMoment)
    }

    override fun momentum(magnitude: DoubleArray, moment: Int): Double {
        val avg = magnitude.average()
        return magnitude.map { it.pow(moment) - avg }.average()
    }

    override fun skewness(magnitude: DoubleArray): Double {
        val thirdMoment = momentum(magnitude, 3)
        val secondMoment = momentum(magnitude, 2)
        return thirdMoment / (secondMoment.pow(1.5))
    }

    override fun gCrossRate(magnitude: DoubleArray, threshold: Double): Double {
        var crossed = false
        var crosses = 0.0
        for (i in magnitude) {
            if (crossed && i > threshold) {
                crosses++
                crossed = false
            } else if (!crossed && i < threshold) {
                crosses++
                crossed = true
            }
        }
        return crosses
    }


    override fun hjorthParams(magnitude: DoubleArray): DoubleArray {
        val activity = variance(magnitude)

        val d1 = magnitude.dropLast(1).zipWithNext { a, b -> b - a }.toDoubleArray()
        val mobility = variance(d1) / activity

        val d2 = d1.dropLast(1).zipWithNext { a, b -> b - a }.toDoubleArray()
        val complexity = variance(d2) / mobility

        return doubleArrayOf(activity, mobility, complexity)
    }

    override fun variance(values: DoubleArray): Double {
        val avg = values.average()
        val sumSquaredDiff = values.sumOf { (it - avg) * (it - avg) }
        return sumSquaredDiff / values.size
    }

    override fun avgTkeo(magnitude: DoubleArray): Double {
        var sum = 0.0
        for (i in 1 until magnitude.size - 1) {
            sum += magnitude[i] * magnitude[i] + magnitude[i - 1] * magnitude[i + 1]
        }
        return sum / (magnitude.size - 2)
    }

    override fun avgOutput(magnitude: DoubleArray): Double {
        val sum = magnitude.sumOf { it * it }
        return sum / magnitude.size
    }

    override fun apEn(magnitude: DoubleArray, m: Int, r: Double): Double {
        if (r <= 0) {
            throw IllegalArgumentException("r must be a positive real number")
        }

        val n = magnitude.size

        fun maxDist(xi: DoubleArray, xj: DoubleArray): Double {
            return xi.zip(xj).maxOfOrNull { (ua, va) -> abs(ua - va) } ?: 0.0
        }

        fun phi(m: Int): Double {
            val x = (0 until n - m + 1).map { i -> magnitude.sliceArray(i until i + m) }
            val c = x.map { xi ->
                x.count { xj -> maxDist(xi, xj) <= r }.toDouble() / (n - m + 1.0)
            }
            val nm = n - m
            val denom = if (nm == 0) 1 else nm

            return 1.0 / denom * c.sumOf { ln(it) }
        }

        return abs(phi(m + 1) - phi(m)) / n
    }

    override fun waveformLength(amplitude: DoubleArray): Double {
        val amplitudeList = amplitude.toList()
        return amplitudeList.zipWithNext { a, b -> abs(b - a) }.average()
    }

    override fun crestFactor(amplitude: DoubleArray): Double {
        val rms = sqrt(amplitude.map { it * it }.average())
        val peak = amplitude.maxOrNull() ?: 0.0
        return abs(peak) / rms
    }

    override fun standardDeviation(values: DoubleArray): Double {
        val varValue = variance(values)
        return sqrt(varValue)
    }

}