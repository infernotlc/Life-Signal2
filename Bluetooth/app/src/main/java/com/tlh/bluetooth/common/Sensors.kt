package com.tlh.bluetooth.common

import android.hardware.Sensor

enum class Sensors(var shortName: String, var sensor: Int) {
    GRAVITY("AGG", Sensor.TYPE_GRAVITY),
    ACG("ACG", Sensor.TYPE_ACCELEROMETER),
    ACC("ACC", Sensor.TYPE_LINEAR_ACCELERATION),
    GYRO("GYRO", Sensor.TYPE_GYROSCOPE),
    MAGNET("MAGNET", Sensor.TYPE_MAGNETIC_FIELD),
    ROTATION("ROTATION", Sensor.TYPE_ROTATION_VECTOR)
}