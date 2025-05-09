package com.m7md7sn.tetracycle.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.m7md7sn.tetracycle.data.model.FirebaseSensorData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSensorRepository @Inject constructor() {
    private val database = FirebaseDatabase.getInstance()
    private val sensorDataRef = database.getReference("tetracycle_sensor_data")
    private val controlRef = database.getReference("tetracycle_control")
    
    /**
     * Get a flow of the latest sensor data from Firebase
     */
    fun getLatestSensorData(): Flow<Map<String, FirebaseSensorData>> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sensorDataMap = mutableMapOf<String, FirebaseSensorData>()
                
                for (childSnapshot in snapshot.children) {
                    val key = childSnapshot.key ?: continue
                    val sensorData = childSnapshot.getValue(FirebaseSensorData::class.java) ?: continue
                    sensorDataMap[key] = sensorData
                }
                
                trySend(sensorDataMap).isSuccess
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        sensorDataRef.addValueEventListener(valueEventListener)
        
        // Remove listener when flow collection ends
        awaitClose {
            sensorDataRef.removeEventListener(valueEventListener)
        }
    }
    
    /**
     * Get a flow of sensor data history for a specific sensor
     */
    fun getSensorHistory(): Flow<List<FirebaseSensorData>> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sensorDataList = mutableListOf<FirebaseSensorData>()
                
                for (childSnapshot in snapshot.children) {
                    val sensorData = childSnapshot.getValue(FirebaseSensorData::class.java) ?: continue
                    sensorDataList.add(sensorData)
                }
                
                trySend(sensorDataList.sortedBy { it.timestamp }).isSuccess
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        // Limit to last 50 readings
        sensorDataRef.limitToLast(50).addValueEventListener(valueEventListener)
        
        // Remove listener when flow collection ends
        awaitClose {
            sensorDataRef.removeEventListener(valueEventListener)
        }
    }

    /**
     * Get a flow of the latest pump states from Firebase
     */
    fun getPumpStatesFlow(): Flow<List<Boolean>> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pumpStates = MutableList(2) { false }
                pumpStates[0] = (snapshot.child("pump1").getValue(Int::class.java) ?: 0) == 1
                pumpStates[1] = (snapshot.child("pump2").getValue(Int::class.java) ?: 0) == 1
                trySend(pumpStates).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        controlRef.addValueEventListener(valueEventListener)
        awaitClose { controlRef.removeEventListener(valueEventListener) }
    }

    /**
     * Set the state of a specific pump in tetracycle_control
     */
    fun setPumpState(index: Int, state: Boolean) {
        controlRef.child("pump${index+1}").setValue(if (state) 1 else 0)
    }

    /**
     * Set all pumps to a specific state in tetracycle_control
     */
    fun setAllPumps(state: Boolean) {
        val updates = mapOf(
            "pump1" to if (state) 1 else 0,
            "pump2" to if (state) 1 else 0
        )
        controlRef.updateChildren(updates)
    }
    
    /**
     * Set the system state in tetracycle_control
     */
    fun setSystemState(isOn: Boolean) {
        controlRef.child("system").setValue(if (isOn) 1 else 0)
    }
    
    /**
     * Set the servomotor state in tetracycle_control
     */
    fun setServomotorState(isOn: Boolean) {
        controlRef.child("servo").setValue(if (isOn) 1 else 0)
    }

    /**
     * Get a flow of the system state from Firebase
     */
    fun getSystemStateFlow(): Flow<Boolean> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOn = (snapshot.getValue(Int::class.java) ?: 0) == 1
                trySend(isOn).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        controlRef.child("system").addValueEventListener(valueEventListener)
        awaitClose { controlRef.child("system").removeEventListener(valueEventListener) }
    }

    /**
     * Get a flow of the servomotor state from Firebase
     */
    fun getServomotorStateFlow(): Flow<Boolean> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOn = (snapshot.getValue(Int::class.java) ?: 0) == 1
                trySend(isOn).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        controlRef.child("servo").addValueEventListener(valueEventListener)
        awaitClose { controlRef.child("servo").removeEventListener(valueEventListener) }
    }

    /**
     * Get a flow of the current schedule status from Firebase
     */
    fun getScheduleStatusFlow(): Flow<String> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.child("status").getValue(String::class.java) ?: "stopped"
                trySend(status).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        controlRef.addValueEventListener(valueEventListener)
        awaitClose { controlRef.removeEventListener(valueEventListener) }
    }

    /**
     * Send a control command to Firebase (for the bridge script)
     */
    fun setControlCommand(command: String) {
        controlRef.child("command").setValue(command)
    }
} 