package com.example.espbattery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.espbattery.ui.theme.*

@Composable
fun MainScreen(
    viewModel: AppViewModel,
    onScanClick: () -> Unit,
    onConnectDevice: (EspDevice) -> Unit,
    onDisconnectClick: () -> Unit,
    onStartClick: () -> Unit
) {
    val currentBattery by viewModel.currentBattery.collectAsState()
    val targetBattery by viewModel.targetBattery.collectAsState()
    val btStatusMessage by viewModel.btStatusMessage.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val isServiceRunning by viewModel.isServiceRunning.collectAsState()
    val pairedDevices by viewModel.pairedDevices.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "ESP Battery Monitor",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Connection Status Card
        StatusCard(status = btStatusMessage, isConnected = isConnected)

        Spacer(modifier = Modifier.height(32.dp))

        // Battery Circle Info
        BatteryCircle(level = currentBattery)

        Spacer(modifier = Modifier.height(40.dp))

        // Target Slider
        Text(
            text = "Target Level: ${targetBattery.toInt()}%",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Slider(
            value = targetBattery,
            onValueChange = { viewModel.setTargetBattery(it) },
            valueRange = 0f..100f,
            steps = 100,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    if (isConnected) {
                        onDisconnectClick()
                    } else {
                        onScanClick()
                        showDialog = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text(if (isConnected) "Disconnect" else "Scan BT", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = onStartClick,
                enabled = isConnected, // Only enable if connected
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isServiceRunning) DangerRed else MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text(if (isServiceRunning) "Stop Service" else "Start Service", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showDialog && pairedDevices.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select ESP32 Device") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(pairedDevices) { device ->
                        Text(
                            text = "${device.name}\n${device.address}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showDialog = false
                                    onConnectDevice(device)
                                }
                                .padding(16.dp)
                        )
                        Divider()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatusCard(status: String, isConnected: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isConnected) SuccessGreen else DangerRed)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = status,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun BatteryCircle(level: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(75.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$level%",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Current",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}
