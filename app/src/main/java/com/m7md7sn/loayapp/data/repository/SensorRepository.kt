package com.m7md7sn.loayapp.data.repository

import com.m7md7sn.loayapp.data.model.FirebaseSensorData
import com.m7md7sn.loayapp.data.model.SensorReading
import com.m7md7sn.loayapp.data.model.SystemPart
import com.m7md7sn.loayapp.data.model.TimedSensorReading
import com.m7md7sn.loayapp.data.model.SensorStatus
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Sync
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorRepository @Inject constructor(
    private val firebaseRepository: FirebaseSensorRepository
) {
    // Cache the latest sensor data
    private var latestData: Map<String, FirebaseSensorData>? = null
    
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getSensorReadingsFlow(): Flow<List<SensorReading>> {
        return firebaseRepository.getLatestSensorData().map { sensorDataMap ->
            latestData = sensorDataMap
            
            // Find the latest entry
            val latestEntry = sensorDataMap.entries.maxByOrNull { it.key.toDoubleOrNull() ?: 0.0 }?.value
                ?: return@map emptyList<SensorReading>()
            
            listOf(
                SensorReading("TDS", latestEntry.tds.toString(), "ppm", Icons.Outlined.Sensors),
                SensorReading("pH", String.format("%.2f", latestEntry.ph), null, Icons.Outlined.Sensors),
                SensorReading("Turbidity", String.format("%.2f", latestEntry.turbidity), "NTU", Icons.Outlined.Sensors)
            )
        }
    }

    // Fallback method for non-flow access
    fun getSensorReadings(): List<SensorReading> {
        val latestEntry = latestData?.entries?.maxByOrNull { it.key.toDoubleOrNull() ?: 0.0 }?.value
        
        return if (latestEntry != null) {
            listOf(
                SensorReading("TDS", latestEntry.tds.toString(), "ppm", Icons.Outlined.Sensors),
                SensorReading("pH", String.format("%.2f", latestEntry.ph), null, Icons.Outlined.Sensors),
                SensorReading("Turbidity", String.format("%.2f", latestEntry.turbidity), "NTU", Icons.Outlined.Sensors)
            )
        } else {
            // Fallback to dummy data if no real data is available yet
            listOf(
                SensorReading("TDS", "0", "ppm", Icons.Outlined.Sensors),
                SensorReading("pH", "0", null, Icons.Outlined.Sensors),
                SensorReading("Turbidity", "0", "NTU", Icons.Outlined.Sensors)
            )
        }
    }

    fun getSystemParts(): List<SystemPart> = listOf(
        SystemPart("Pumps", Icons.Outlined.Sync),
        SystemPart("Sensors", Icons.Outlined.Sensors)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTimedSensorReadingsFlow(sensorName: String): Flow<List<TimedSensorReading>> {
        return firebaseRepository.getSensorHistory().map { sensorDataList ->
            when (sensorName.lowercase()) {
                "ph" -> sensorDataList.map { 
                    TimedSensorReading(it.ph, formatTimestamp(it.timestamp)) 
                }
                "tds" -> sensorDataList.map { 
                    TimedSensorReading(it.tds, formatTimestamp(it.timestamp)) 
                }
                "turbidity" -> sensorDataList.map { 
                    TimedSensorReading(it.turbidity, formatTimestamp(it.timestamp)) 
                }
                else -> emptyList()
            }.takeLast(10)
        }
    }
    
    // Fallback for non-flow access
    fun getTimedSensorReadings(sensorName: String): List<TimedSensorReading> {
        val sensorHistory = latestData?.values?.toList() ?: return emptyList()
        return when (sensorName.lowercase()) {
            "ph" -> sensorHistory.map { 
                TimedSensorReading(it.ph, formatTimestamp(it.timestamp)) 
            }
            "tds" -> sensorHistory.map { 
                TimedSensorReading(it.tds, formatTimestamp(it.timestamp)) 
            }
            "turbidity" -> sensorHistory.map { 
                TimedSensorReading(it.turbidity, formatTimestamp(it.timestamp)) 
            }
            else -> emptyList()
        }.takeLast(5)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getSensorStatusesFlow(): Flow<List<SensorStatus>> {
        return firebaseRepository.getLatestSensorData().map { sensorDataMap ->
            val latestEntry = sensorDataMap.entries.maxByOrNull { it.key.toDoubleOrNull() ?: 0.0 }?.value
                ?: return@map emptyList<SensorStatus>()
            
            listOf(
                SensorStatus(
                    name = "pH",
                    value = latestEntry.ph,
                    unit = "",
                    isWorking = true,
                    state = determinePHState(latestEntry.ph)
                ),
                SensorStatus(
                    name = "Turbidity",
                    value = latestEntry.turbidity,
                    unit = "NTU",
                    isWorking = true,
                    state = determineTurbidityState(latestEntry.turbidity)
                ),
                SensorStatus(
                    name = "TDS",
                    value = latestEntry.tds,
                    unit = "ppm",
                    isWorking = true,
                    state = determineTDSState(latestEntry.tds)
                )
            )
        }
    }
    
    fun getSensorStatuses(): List<SensorStatus> {
        val latestEntry = latestData?.entries?.maxByOrNull { it.key.toDoubleOrNull() ?: 0.0 }?.value
        
        return if (latestEntry != null) {
            listOf(
                SensorStatus(
                    name = "pH",
                    value = latestEntry.ph,
                    unit = "",
                    isWorking = true,
                    state = determinePHState(latestEntry.ph)
                ),
                SensorStatus(
                    name = "Turbidity",
                    value = latestEntry.turbidity,
                    unit = "NTU",
                    isWorking = true,
                    state = determineTurbidityState(latestEntry.turbidity)
                ),
                SensorStatus(
                    name = "TDS",
                    value = latestEntry.tds,
                    unit = "ppm",
                    isWorking = true,
                    state = determineTDSState(latestEntry.tds)
                )
            )
        } else {
            // Fallback to dummy data
            listOf(
                SensorStatus("pH", 0f, "", false, "Offline"),
                SensorStatus("Turbidity", 0f, "NTU", false, "Offline"),
                SensorStatus("TDS", 0f, "ppm", false, "Offline")
            )
        }
    }
    
    private fun formatTimestamp(timestamp: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(timestamp)
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            timestamp
        }
    }
    
    private fun determinePHState(ph: Float): String = when {
        ph < 6.5f -> "Low"
        ph > 8.5f -> "High"
        else -> "Normal"
    }
    
    private fun determineTurbidityState(turbidity: Float): String = when {
        turbidity > 500f -> "High"
        else -> "Normal"
    }
    
    private fun determineTDSState(tds: Float): String = when {
        tds > 500f -> "High"
        tds < 50f -> "Low"
        else -> "Normal"
    }

    // --- Pump state support ---
    fun getPumpStatesFlow() = firebaseRepository.getPumpStatesFlow()
    fun setPumpState(index: Int, state: Boolean) = firebaseRepository.setPumpState(index, state)
    fun setAllPumps(state: Boolean) = firebaseRepository.setAllPumps(state)
    fun setSystemState(isOn: Boolean) = firebaseRepository.setSystemState(isOn)
    fun setServomotorState(isOn: Boolean) = firebaseRepository.setServomotorState(isOn)
    fun getSystemStateFlow() = firebaseRepository.getSystemStateFlow()
    fun getServomotorStateFlow() = firebaseRepository.getServomotorStateFlow()

    // --- Schedule control support ---
    fun getScheduleStatusFlow() = firebaseRepository.getScheduleStatusFlow()
    fun setControlCommand(command: String) = firebaseRepository.setControlCommand(command)
} 