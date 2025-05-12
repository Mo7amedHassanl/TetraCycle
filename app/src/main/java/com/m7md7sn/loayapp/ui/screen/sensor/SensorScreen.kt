package com.m7md7sn.loayapp.ui.screen.sensor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import com.m7md7sn.loayapp.data.model.TimedSensorReading
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun SensorScreen(
    sensorName: String,
    currentValue: Float,
    unit: String,
    minValue: Float,
    maxValue: Float,
    normalRange: ClosedFloatingPointRange<Float>,
    viewModel: SensorViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    LaunchedEffect(sensorName) { viewModel.loadReadings(sensorName) }
    val readings by viewModel.readings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val displayReadings = readings
    val state = when {
        currentValue < normalRange.start -> "Low"
        currentValue > normalRange.endInclusive -> "High"
        else -> "Normal"
    }
    val stateColor = when (state) {
        "Normal" -> Color(0xFF4CAF50)
        "High" -> Color(0xFFF44336)
        else -> Color(0xFF2196F3)
    }
    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading && displayReadings.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (error != null && displayReadings.isEmpty()) {
            ErrorView(
                errorMessage = error ?: "Unknown error",
                onRetry = { viewModel.refresh(sensorName) }
            )
        } else {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Value Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp, horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(stateColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = currentValue.toString(),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = stateColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                // Status/Range Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text("State: $state") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = stateColor.copy(alpha = 0.15f),
                            labelColor = stateColor
                        )
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("Normal: ${normalRange.start} - ${normalRange.endInclusive}") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            labelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                // Trend Chart Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "Reading Trend",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        if (displayReadings.isEmpty()) {
                            NoDataView()
                        } else {
                            VicoSensorLineChart(displayReadings)
                        }
                    }
                }
            }
            if (isLoading && displayReadings.isNotEmpty()) {
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
fun GradientCircularProgressBar(
    value: Float,
    minValue: Float,
    maxValue: Float,
    normalRange: ClosedFloatingPointRange<Float>,
    unit: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val progress = ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
        val color = when {
            value < normalRange.start -> Color(0xFF4CAF50)
            value > normalRange.endInclusive -> Color(0xFFF44336)
            else -> Color(0xFFFFEB3B)
        }
        CircularProgressIndicator(
            progress = { progress },
            strokeWidth = 18.dp,
            color = color,
            trackColor = Color.LightGray,
            modifier = Modifier.fillMaxSize()
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ReadingTable(readings: List<TimedSensorReading>) {
    val maxRows = 5
    val columns = if (readings.isEmpty()) 1 else (readings.size + maxRows - 1) / maxRows
    val tableData = List(columns) { col ->
        readings.drop(col * maxRows).take(maxRows)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(vertical = 12.dp, horizontal = 0.dp)) {
            // Header
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .padding(vertical = 10.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (col in 0 until columns) {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "Reading",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "Time",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            for (row in 0 until maxRows) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (col in 0 until columns) {
                        val reading = tableData.getOrNull(col)?.getOrNull(row)
                        if (reading != null) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if ((row + col) % 2 == 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = CircleShape
                                    )
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                    .weight(1f)
                            ) {
                                Text(
                                    text = reading.value.toString(),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = reading.time,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(Modifier.weight(2f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VicoSensorLineChart(readings: List<TimedSensorReading>, modifier: Modifier = Modifier) {
    if (readings.size < 2) return

    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(readings) {
        modelProducer.runTransaction {
            lineSeries {
                series(*readings.map { it.value }.toTypedArray())
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
    )
}

@Composable
fun NoDataView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No data available",
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