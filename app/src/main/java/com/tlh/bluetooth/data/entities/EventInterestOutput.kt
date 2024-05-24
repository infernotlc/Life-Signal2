package com.tlh.bluetooth.data.entities

data class EventInterestOutput(
    val magnitudeList: MutableList<SensorOutput>,
    val begin: Int,
    val end: Int,
    val maxPeakIndex: Int,
    val freeFallEnd: Int?
) {
    fun fromFreeFall(): DoubleArray {
        val magnitude = magnitudeList.map {
            it.magnitude
        }.toDoubleArray()
        if (freeFallEnd != null) {
            return magnitude.sliceArray(freeFallEnd - begin until magnitude.size)
        }
        return magnitude
    }
}