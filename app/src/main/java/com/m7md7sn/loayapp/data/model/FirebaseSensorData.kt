package com.m7md7sn.loayapp.data.model

import androidx.annotation.Keep

/**
 * Data class representing the sensor data structure in Firebase Realtime Database
 */
@Keep
data class FirebaseSensorData(
    val flow: Float = 0f,
    val ph: Float = 0f,
    val tds: Float = 0f,
    val timestamp: String = "",
    val turbidity: Float = 0f,
    val volume: Float = 0f
) {
    // Secondary constructor for JSON deserialization
    constructor() : this(0f, 0f, 0f, "", 0f, 0f)
} 