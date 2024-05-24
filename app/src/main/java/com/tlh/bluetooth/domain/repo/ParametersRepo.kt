package com.tlh.bluetooth.domain.repo

import com.tlh.bluetooth.data.entities.EventInterestOutput

interface ParametersRepo {
    fun calculateAcgParameters(
        eventHolder: EventInterestOutput
    ): DoubleArray?

    fun basicStats(eventMagnitude: DoubleArray): DoubleArray

    fun changeInAngle(
        sensorValues:
        List<List<Float>>
    ): Double

    fun changeInAngleCos(
        timeSeconds: List<Long>,
        acgXyz: List<List<Float>>,
        beginIndex: Int,
        endIndex: Int
    ): Double

    fun beforeAndAfterFall(
        timeSeconds: List<Long>,
        acgXyz: List<List<Float>>,
        beginIndex: Int,
        endIndex: Int
    ): Pair<List<List<Float>>,
            List<List<Float>>>

    fun ad(values: List<List<Float>>): Double

    fun freeFallIndex(
        valuesAcg:
        DoubleArray, eventOfInterest: EventInterestOutput
    ): Double

    fun minMax(
        magnitude:
        DoubleArray
    ): Double

    fun ratio3g(magnitude: DoubleArray, threshold: Double = 30.0): Double

    fun kurtosis(magnitude: DoubleArray): Double

    fun momentum(magnitude: DoubleArray, moment: Int = 2): Double

    fun skewness(magnitude: DoubleArray): Double

    fun gCrossRate(magnitude: DoubleArray, threshold: Double = 9.25): Double

    fun hjorthParams(magnitude: DoubleArray): DoubleArray

    fun variance(values: DoubleArray): Double

    fun avgTkeo(magnitude: DoubleArray): Double

    fun avgOutput(magnitude: DoubleArray): Double

    fun apEn(magnitude: DoubleArray, m: Int, r: Double): Double

    fun waveformLength(amplitude: DoubleArray): Double

    fun crestFactor(amplitude: DoubleArray): Double

    fun standardDeviation(values: DoubleArray): Double
}