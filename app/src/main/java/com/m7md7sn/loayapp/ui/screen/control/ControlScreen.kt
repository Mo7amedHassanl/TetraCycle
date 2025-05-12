package com.m7md7sn.loayapp.ui.screen.control

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

@Composable
fun ControlScreen(
    modifier: Modifier = Modifier,
    viewModel: ControlViewModel = hiltViewModel()
) {
    val pumpNames = listOf("Pump 1", "Pump 2")
    val pumpStates by viewModel.pumpStates.collectAsState()
    var showScheduleDialog by remember { mutableStateOf(false) }
    val error by viewModel.error.collectAsState()
    val scheduleStatus by viewModel.scheduleStatus.collectAsState()
    val servomotorState by viewModel.servomotorState.collectAsState()
    val systemOn by viewModel.systemOn.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        if (error != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Button(onClick = { viewModel.retry() }) {
                    Text("Retry")
                }
            }
        }
        // Top quick actions, now as a horizontal scrollable row with accent backgrounds
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionButton(icon = Icons.Filled.PowerSettingsNew, label = "All On") {
                viewModel.turnAllOn()
            }
            QuickActionButton(icon = Icons.Filled.PowerOff, label = "All Off") {
                viewModel.turnAllOff()
            }
            QuickActionButton(icon = Icons.Filled.Schedule, label = "Schedule") {
                showScheduleDialog = true
            }
        }

        // List of pump controls, each in a stylized card with a colored sidebar
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            pumpNames.forEachIndexed { index, name ->
                PumpControlCard(
                    name = name,
                    isOn = pumpStates[index],
                    onToggle = { viewModel.togglePump(index) }
                )
            }
            // Servomotor Card
            PumpControlCard(
                name = "Servomotor",
                isOn = servomotorState,
                onToggle = { viewModel.toggleServomotor() }
            )
            // Push Button Card (System On/Off)
            PumpControlCard(
                name = "Push Button (System On/Off)",
                isOn = systemOn,
                onToggle = { viewModel.toggleSystemOn() }
            )
        }
    }

    if (showScheduleDialog) {
        ScheduleControlDialog(
            currentStatus = scheduleStatus,
            onDismiss = { showScheduleDialog = false },
            onCommandSelected = { command -> 
                viewModel.controlSchedule(command)
                showScheduleDialog = false 
            }
        )
    }
}

@Composable
fun PumpControlCard(name: String, isOn: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colored sidebar
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(
                        color = if (isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
            )
            Spacer(modifier = Modifier.width(16.dp))
            // Pump name and status
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
                Text(
                    text = if (isOn) "ON" else "OFF",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            // Switch
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

@Composable
fun QuickActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun ScheduleControlDialog(
    currentStatus: String?,
    onDismiss: () -> Unit,
    onCommandSelected: (String) -> Unit
) {
    val normalizedStatus = when (currentStatus?.lowercase()?.trim()) {
        "running" -> "running"
        "paused" -> "paused"
        "stopped" -> "stopped"
        else -> "stopped" // treat null, empty, or unknown as stopped
    }
    val statusDisplayText = when(normalizedStatus) {
        "running" -> "Schedule is currently RUNNING"
        "stopped" -> "Schedule is currently STOPPED"
        "paused" -> "Schedule is currently PAUSED"
        else -> "Schedule status unknown"
    }
    val statusColor = when(normalizedStatus) {
        "running" -> MaterialTheme.colorScheme.primary
        "stopped" -> MaterialTheme.colorScheme.error
        "paused" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Water Treatment") },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Status",
                        tint = statusColor,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = statusDisplayText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = statusColor
                    )
                }
                Text(
                    "The water treatment schedule runs through five cycles and takes approximately 6 hours to complete.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "Select a command below:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // Command buttons (now inside the dialog)
                when (normalizedStatus) {
                    "running" -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ScheduleButton(
                                text = "PAUSE",
                                icon = Icons.Default.Pause,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f)
                            ) {
                                onCommandSelected("pause")
                            }
                            ScheduleButton(
                                text = "STOP",
                                icon = Icons.Default.Stop,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            ) {
                                onCommandSelected("stop")
                            }
                        }
                    }
                    "paused" -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ScheduleButton(
                                text = "RESUME",
                                icon = Icons.Default.PlayArrow,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            ) {
                                onCommandSelected("resume")
                            }
                            ScheduleButton(
                                text = "STOP",
                                icon = Icons.Default.Stop,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            ) {
                                onCommandSelected("stop")
                            }
                        }
                    }
                    else -> {
                        ScheduleButton(
                            text = "START SCHEDULE",
                            icon = Icons.Default.PlayArrow,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            onCommandSelected("start")
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    )
}

@Composable
fun ScheduleButton(
    text: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(text)
    }
} 