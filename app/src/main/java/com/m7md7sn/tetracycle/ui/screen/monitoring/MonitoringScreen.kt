package com.m7md7sn.tetracycle.ui.screen.monitoring

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.m7md7sn.tetracycle.data.model.SensorStatus
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.m7md7sn.tetracycle.ui.screen.sensor.SensorScreen

@Composable
fun MonitoringScreen(
    sensorStatuses: List<SensorStatus>,
    isLoading: Boolean,
    error: String?,
    initialTab: Int = 0,
    onSensorCardClick: (Int) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && sensorStatuses.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (error != null && sensorStatuses.isEmpty()) {
            ErrorView(
                errorMessage = error ?: "Unknown error",
                onRetry = { /* no-op for now */ }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Monitoring",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (sensorStatuses.isNotEmpty()) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = { tabPositions: List<TabPosition> ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        sensorStatuses.forEachIndexed { index, sensor ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(sensor.name) }
                            )
                        }
                    }
                    val sensor = sensorStatuses[selectedTab]
                    SensorScreen(
                        sensorName = sensor.name,
                        currentValue = sensor.value,
                        unit = sensor.unit,
                        minValue = when (sensor.name.lowercase()) {
                            "ph" -> 0f
                            "tds" -> 0f
                            "turbidity" -> 0f
                            else -> 0f
                        },
                        maxValue = when (sensor.name.lowercase()) {
                            "ph" -> 14f
                            "tds" -> 3000f
                            "turbidity" -> 1000f
                            else -> 100f
                        },
                        normalRange = when (sensor.name.lowercase()) {
                            "ph" -> 6.5f..8.5f
                            "tds" -> 0f..500f
                            "turbidity" -> 0f..50f
                            else -> 0f..100f
                        },
                        viewModel = hiltViewModel(),
                        modifier = Modifier
                    )
                } else {
                    NoDataView()
                }
            }
            if (isLoading && sensorStatuses.isNotEmpty()) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
fun NoDataView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No sensor data available",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ErrorView(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = "Error",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Retry"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}

fun phColor(pH: Float): Color {
    return when {
        pH < 1f -> Color(0xFFFF0000) // Red
        pH < 2f -> Color(0xFFFF4500) // Orange-Red
        pH < 3f -> Color(0xFFFF9900) // Orange
        pH < 4f -> Color(0xFFFFC100) // Yellow-Orange
        pH < 5f -> Color(0xFFFFFF00) // Yellow
        pH < 6f -> Color(0xFFBFFF00) // Yellow-Green
        pH < 7f -> Color(0xFF00FF00) // Green
        pH < 8f -> Color(0xFF00FFB0) // Green-Cyan
        pH < 9f -> Color(0xFF00FFFF) // Cyan
        pH < 10f -> Color(0xFF00BFFF) // Blue-Green
        pH < 11f -> Color(0xFF007FFF) // Blue
        pH < 12f -> Color(0xFF4B0082) // Blue-Violet
        pH < 13f -> Color(0xFF8B00FF) // Violet
        else -> Color(0xFF800080) // Purple
    }
}

@Composable
fun MonitoringSensorDetails(sensor: SensorStatus) {
    val overlayColor = when (sensor.name.lowercase()) {
        "ph" -> phColor(sensor.value)
        else -> {
            if (sensor.state == "Normal") {
                Brush.horizontalGradient(listOf(Color(0xFF4CAF50), Color(0xFF81C784)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFFF44336), Color(0xFFE57373)))
            }
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Box(
            modifier = Modifier
                .wrapContentHeight()
                .clip(RoundedCornerShape(20.dp))
        ) {
            // Overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .let {
                        when (overlayColor) {
                            is Color -> it.background(overlayColor.copy(alpha = 0.5f))
                            is Brush -> it.background(overlayColor, alpha = 0.5f)
                            else -> it
                        }
                    }
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = sensor.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${sensor.value} ${sensor.unit}",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = sensor.state,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (sensor.isWorking) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Working",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Text("Working", color = Color.White, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "Not Working",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Text("Not Working", color = Color.White, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
                    }
                }
                // Optionally, add a mock chart or history here
            }
        }
    }
}

// Helper to determine state and create the list
fun getSensorStatuses(
    ph: Float, phWorking: Boolean,
    turbidity: Float, turbidityWorking: Boolean,
    tds: Float, tdsWorking: Boolean
): List<SensorStatus> {
    val phState = when {
        ph < 6.5f -> "Low"
        ph > 8.5f -> "High"
        else -> "Normal"
    }
    val turbidityState = if (turbidity < 50f) "Normal" else "High"
    val tdsState = if (tds < 500f) "Normal" else "High"
    return listOf(
        SensorStatus("pH", ph, "", phWorking, phState),
        SensorStatus("Turbidity", turbidity, "NTU", turbidityWorking, turbidityState),
        SensorStatus("TDS", tds, "ppm", tdsWorking, tdsState)
    )
} 