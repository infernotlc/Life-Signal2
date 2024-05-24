package com.tlh.bluetooth.data.entities

data class SensorOutput(
    val time:Long,
    val values :List<Float>,
    val magnitude:Double,
    val accuracy:Int,
    val type:Int
)
