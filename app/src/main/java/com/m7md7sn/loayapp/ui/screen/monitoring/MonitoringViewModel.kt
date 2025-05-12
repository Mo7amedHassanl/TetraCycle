package com.m7md7sn.loayapp.ui.screen.monitoring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m7md7sn.loayapp.data.model.SensorStatus
import com.m7md7sn.loayapp.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@HiltViewModel
class MonitoringViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {
    private val _sensorStatuses = MutableStateFlow<List<SensorStatus>>(emptyList())
    val sensorStatuses: StateFlow<List<SensorStatus>> = _sensorStatuses
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadStatuses()
    }

    private fun loadStatuses() {
        _isLoading.value = true
        _error.value = null
        
        // First load with synchronous method for immediate data
        _sensorStatuses.value = repository.getSensorStatuses()
        
        // Then subscribe to real-time updates
        viewModelScope.launch {
            repository.getSensorStatusesFlow()
                .catch { e ->
                    _error.value = "Error loading sensor data: ${e.message}"
                    _isLoading.value = false
                }
                .collect { statuses ->
                    _sensorStatuses.value = statuses
                    _isLoading.value = false
                }
        }
    }
    
    fun refresh() {
        loadStatuses()
    }
} 