package com.m7md7sn.tetracycle.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.m7md7sn.tetracycle.data.model.SensorReading
import com.m7md7sn.tetracycle.ui.theme.LoayAppTheme
import androidx.hilt.navigation.compose.hiltViewModel
import com.m7md7sn.tetracycle.ui.screen.control.ControlViewModel
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onSensorCardClick: (Int) -> Unit = {},
    onSystemPartClick: (Int) -> Unit = {}
) {
    val sensorReadings by viewModel.sensorReadings.collectAsState()
    val systemParts by viewModel.systemParts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val filteredReadings = sensorReadings.filter { it.label.lowercase() != "temperature" }
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 0.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Welcome!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Water Quality Section
            Text(
                text = "Water Quality",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
            )
            if (isLoading && filteredReadings.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (error != null && filteredReadings.isEmpty()) {
                ErrorView(
                    errorMessage = error ?: "Unknown error",
                    onRetry = { viewModel.refresh() }
                )
            } else {
                SensorGridAllVisible(
                    readings = filteredReadings,
                    onSensorCardClick = { index ->
                        // Navigate to monitoring screen with selected tab index
                        onSensorCardClick(index)
                    }
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            // System Control Section
            Text(
                text = "System Control",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
            )
            SystemControlDetailedCards()
            Spacer(modifier = Modifier.height(24.dp))
        }
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

@Composable
fun SensorGridAllVisible(
    readings: List<SensorReading>,
    onSensorCardClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false // disables grid scrolling, relies on main screen scroll
    ) {
        items(readings.size) { index ->
            ModernSensorCard(
                reading = readings[index],
                onCardClick = { onSensorCardClick(index) }
            )
        }
    }
}

@Composable
fun ModernSensorCard(
    reading: SensorReading,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val value = reading.value.toFloatOrNull() ?: 0f
    val state = when (reading.label.lowercase()) {
        "tds" -> if (value < 500f) "Normal" else "High"
        "turbidity" -> if (value < 50f) "Normal" else "High"
        "ph" -> when {
            value < 6.5f -> "Low"
            value > 8.5f -> "High"
            else -> "Normal"
        }
        else -> "Normal"
    }
    val stateColor = when (state) {
        "Normal" -> Color(0xFF4CAF50)
        "High" -> Color(0xFFF44336)
        "Low" -> Color(0xFF2196F3)
        else -> Color.Gray
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        onClick = onCardClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = reading.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = reading.value,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = stateColor
                    )
                    if (!reading.unit.isNullOrBlank()) {
                        Text(
                            text = " ${reading.unit}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(stateColor, CircleShape)
            )
        }
    }
}

@Composable
fun SystemControlDetailedCards(viewModel: ControlViewModel = hiltViewModel()) {
    val pumpStates by viewModel.pumpStates.collectAsState()
    val servomotorState by viewModel.servomotorState.collectAsState()
    val systemOn by viewModel.systemOn.collectAsState()
    val error by viewModel.error.collectAsState()
    val controls = listOf(
        Triple("System On/Off", systemOn) { viewModel.toggleSystemOn() },
        Triple("Pump 1", pumpStates.getOrNull(0) ?: false) { viewModel.togglePump(0) },
        Triple("Pump 2", pumpStates.getOrNull(1) ?: false) { viewModel.togglePump(1) },
        Triple("Servomotor", servomotorState) { viewModel.toggleServomotor() }
    )
    val icons = listOf(
        Icons.Filled.PowerSettingsNew,
        Icons.Filled.Water,
        Icons.Filled.Water,
        Icons.Filled.Settings
    )
    val descriptions = listOf(
        "Turns the whole system ON or OFF.",
        "Controls the first water pump.",
        "Controls the second water pump.",
        "Controls the servomotor."
    )
    if (error != null) {
        Text(
            text = error ?: "Unknown error",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        controls.forEachIndexed { idx, (name, isOn, onToggle) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icons[idx],
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = if (isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(18.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        color = if (isOn) Color(0xFF4CAF50) else Color(0xFFF44336),
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOn) "ON" else "OFF",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isOn) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        }
                    }
                    Switch(
                        checked = isOn,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.primary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    LoayAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, start = 24.dp, end = 24.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .padding(8.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Welcome!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                // Water Quality Section
                Text(
                    text = "Water Quality",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
                )
                SensorGridAllVisible(readings = listOf(), onSensorCardClick = {})
                Spacer(modifier = Modifier.height(32.dp))
                // System Control Section
                Text(
                    text = "System Control",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
                )
                SystemControlDetailedCards()
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}