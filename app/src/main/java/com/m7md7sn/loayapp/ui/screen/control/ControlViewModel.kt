package com.m7md7sn.loayapp.ui.screen.control

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import com.m7md7sn.loayapp.data.repository.SensorRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class ControlViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {
    private val _pumpStates = MutableStateFlow(List(2) { false })
    val pumpStates: StateFlow<List<Boolean>> = _pumpStates

    private val _servomotorState = MutableStateFlow(false)
    val servomotorState: StateFlow<Boolean> = _servomotorState

    private val _systemOn = MutableStateFlow(false)
    val systemOn: StateFlow<Boolean> = _systemOn

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _scheduleStatus = MutableStateFlow("unknown")
    val scheduleStatus: StateFlow<String> = _scheduleStatus

    init {
        listenToPumpStates()
        listenToScheduleStatus()
        listenToSystemState()
        listenToServomotorState()
    }

    private fun listenToPumpStates() {
        viewModelScope.launch {
            try {
                repository.getPumpStatesFlow().collectLatest { states ->
                    _pumpStates.value = states
                    _error.value = null
                }
            } catch (e: Exception) {
                _error.value = "Failed to load pump states: ${e.message}"
            }
        }
    }

    private fun listenToSystemState() {
        viewModelScope.launch {
            try {
                repository.getSystemStateFlow().collectLatest { isOn ->
                    _systemOn.value = isOn
                    _error.value = null
                }
            } catch (e: Exception) {
                _error.value = "Failed to load system state: ${e.message}"
            }
        }
    }

    private fun listenToServomotorState() {
        viewModelScope.launch {
            try {
                repository.getServomotorStateFlow().collectLatest { isOn ->
                    _servomotorState.value = isOn
                    _error.value = null
                }
            } catch (e: Exception) {
                _error.value = "Failed to load servomotor state: ${e.message}"
            }
        }
    }

    private fun listenToScheduleStatus() {
        viewModelScope.launch {
            try {
                repository.getScheduleStatusFlow().collectLatest { status ->
                    _scheduleStatus.value = status
                }
            } catch (e: Exception) {
                _error.value = "Failed to load schedule status: ${e.message}"
            }
        }
    }

    fun retry() {
        _error.value = null
        listenToPumpStates()
    }

    fun togglePump(index: Int) {
        val isOn = !_pumpStates.value[index]
        try {
            repository.setPumpState(index, isOn)
            // State will update via Firebase listener
        } catch (e: Exception) {
            _error.value = "Failed to update pump: ${e.message}"
        }
    }

    fun toggleServomotor() {
        val newState = !_servomotorState.value
        try {
            repository.setServomotorState(newState)
            // State will update via Firebase listener
        } catch (e: Exception) {
            _error.value = "Failed to update servomotor: ${e.message}"
        }
    }

    fun toggleSystemOn() {
        val newState = !_systemOn.value
        try {
            repository.setSystemState(newState)
            // State will update via Firebase listener
        } catch (e: Exception) {
            _error.value = "Failed to update system state: ${e.message}"
        }
    }

    fun turnAllOn() {
        try {
            repository.setAllPumps(true)
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Failed to turn all pumps on: ${e.message}"
        }
    }

    fun turnAllOff() {
        try {
            repository.setAllPumps(false)
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Failed to turn all pumps off: ${e.message}"
        }
    }

    fun schedulePumps() {
        // Placeholder for scheduling logic
    }

    fun controlSchedule(command: String) {
        viewModelScope.launch {
            try {
                repository.setControlCommand(command)
            } catch (e: Exception) {
                _error.value = "Failed to control schedule: ${e.message}"
            }
        }
    }
} 