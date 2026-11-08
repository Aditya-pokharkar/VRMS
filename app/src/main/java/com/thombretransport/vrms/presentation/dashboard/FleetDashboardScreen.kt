package com.thombretransport.vrms.presentation.dashboard

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thombretransport.vrms.data.local.entity.VehicleEntity
import com.thombretransport.vrms.domain.util.RegistrationNumberValidator
import com.thombretransport.vrms.presentation.MainViewModel
import com.thombretransport.vrms.presentation.components.ThombreTopAppBar
import com.thombretransport.vrms.presentation.theme.*

@Composable
fun FleetDashboardScreen(
    viewModel: MainViewModel,
    onNavigateToTripLogger: (Long) -> Unit,
    onNavigateToBillCompilation: (Long) -> Unit,
    onNavigateToBillHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val vehicles by viewModel.vehicles.collectAsState()
    val showDeactivated by viewModel.showDeactivated.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ThombreTopAppBar(
                title = "Fleet Dashboard",
                actions = {
                    IconButton(onClick = onNavigateToBillHistory) {
                        Icon(Icons.Default.History, contentDescription = "Bill History", tint = Color.White)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ThombreRed,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Vehicle")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SurfaceBg)
        ) {
            // Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (showDeactivated) "All Registered Vehicles (${vehicles.size})" else "Active Fleet (${vehicles.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "View Deactivated", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = showDeactivated,
                        onCheckedChange = { viewModel.showDeactivated.value = it }
                    )
                }
            }

            if (vehicles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showDeactivated) "No vehicles registered yet." else "No active vehicles. Tap + to add one.",
                        color = TextSecondary,
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(vehicles, key = { it.id }) { vehicle ->
                        VehicleCard(
                            vehicle = vehicle,
                            onLogTrip = { onNavigateToTripLogger(vehicle.id) },
                            onGenerateBill = { onNavigateToBillCompilation(vehicle.id) },
                            onDeactivate = { viewModel.deactivateVehicle(vehicle) },
                            onReactivate = {
                                viewModel.reactivateVehicle(vehicle) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDelete = {
                                viewModel.deleteVehicle(vehicle) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddVehicleDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { reg, type, fuel, notes ->
                viewModel.addVehicle(reg, type, fuel, notes) { success, message ->
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    if (success) showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun VehicleCard(
    vehicle: VehicleEntity,
    onLogTrip: () -> Unit,
    onGenerateBill: () -> Unit,
    onDeactivate: () -> Unit,
    onReactivate: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isDeactivated = vehicle.status == "DEACTIVATED"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDeactivated) Color(0xFFF0F0F0) else CardBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDeactivated) 1.dp else 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Prominent Registration & Context Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = RegistrationNumberValidator.formatForDisplay(vehicle.registrationNumber),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDeactivated) Color.Gray else ThombreRedDark
                    )
                    if (isDeactivated) {
                        Text(
                            text = "STATUS: DEACTIVATED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    }
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        if (!isDeactivated) {
                            DropdownMenuItem(
                                text = { Text("Deactivate Vehicle") },
                                onClick = {
                                    menuExpanded = false
                                    onDeactivate()
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Reactivate Vehicle") },
                                onClick = {
                                    menuExpanded = false
                                    onReactivate()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Permanently", color = Color.Red) },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Details: Vehicle Type & Fuel Type Badges
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("Type: ${vehicle.vehicleType}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text("Fuel: ${vehicle.fuelType}", fontSize = 12.sp) }
                )
            }

            if (vehicle.otherDetails.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Notes: ${vehicle.otherDetails}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            if (!isDeactivated) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onLogTrip,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ThombreRed)
                    ) {
                        Text("Log Trip", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onGenerateBill,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Generate Bill", fontWeight = FontWeight.Bold, color = ThombreRed)
                    }
                }
            }
        }
    }
}

@Composable
fun AddVehicleDialog(
    onDismiss: () -> Unit,
    onConfirm: (reg: String, type: String, fuel: String, notes: String) -> Unit
) {
    var regNumber by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf("AC") }
    var fuelType by remember { mutableStateOf("Diesel") }
    var notes by remember { mutableStateOf("") }

    val vehicleTypes = listOf("AC", "Non-AC Cage", "Other")
    val fuelTypes = listOf("Diesel", "Petrol", "EV")

    var typeExpanded by remember { mutableStateOf(false) }
    var fuelExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register New Vehicle", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = regNumber,
                    onValueChange = { regNumber = RegistrationNumberValidator.normalize(it) },
                    label = { Text("Registration No. (e.g. MH14XX1234)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Vehicle Type Dropdown
                @OptIn(ExperimentalMaterial3Api::class)
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = vehicleType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vehicle Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        vehicleTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    vehicleType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Fuel Type Dropdown (Default Diesel)
                @OptIn(ExperimentalMaterial3Api::class)
                ExposedDropdownMenuBox(
                    expanded = fuelExpanded,
                    onExpandedChange = { fuelExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = fuelType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fuel Type (Default: Diesel)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fuelExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = fuelExpanded,
                        onDismissRequest = { fuelExpanded = false }
                    ) {
                        fuelTypes.forEach { fuel ->
                            DropdownMenuItem(
                                text = { Text(fuel) },
                                onClick = {
                                    fuelType = fuel
                                    fuelExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Other Details / Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(regNumber, vehicleType, fuelType, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = ThombreRed)
            ) {
                Text("Save Vehicle")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
