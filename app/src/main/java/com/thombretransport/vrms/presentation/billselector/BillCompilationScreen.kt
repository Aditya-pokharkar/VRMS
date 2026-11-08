package com.thombretransport.vrms.presentation.billselector

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thombretransport.vrms.data.local.entity.TripEntity
import com.thombretransport.vrms.data.local.entity.VehicleEntity
import com.thombretransport.vrms.domain.util.IndianNumberToWordsConverter
import com.thombretransport.vrms.domain.util.RegistrationNumberValidator
import com.thombretransport.vrms.presentation.MainViewModel
import com.thombretransport.vrms.presentation.components.*
import com.thombretransport.vrms.presentation.theme.*

@Composable
fun BillCompilationScreen(
    vehicleId: Long,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onBillGenerated: () -> Unit
) {
    val context = LocalContext.current
    var vehicle by remember { mutableStateOf<VehicleEntity?>(null) }

    LaunchedEffect(vehicleId) {
        vehicle = viewModel.getVehicle(vehicleId)
    }

    val allTrips by viewModel.getTripsForVehicle(vehicleId).collectAsState(initial = emptyList())
    val clientSuggestions by viewModel.clientSuggestions.collectAsState()

    var clientName by remember { mutableStateOf("") }
    var clientExpanded by remember { mutableStateOf(false) }
    var customPeriodText by remember { mutableStateOf("") }

    val selectedTripIds = remember { mutableStateListOf<Long>() }
    var tripToConfirmRebilling by remember { mutableStateOf<TripEntity?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    val selectedTrips = allTrips.filter { selectedTripIds.contains(it.id) }

    // Auto-derive billing period text whenever selected trips change (if not manually edited)
    LaunchedEffect(selectedTrips) {
        if (selectedTrips.isNotEmpty()) {
            if (selectedTrips.size == 1) {
                customPeriodText = "Transport Bill For Date: ${selectedTrips.first().tripDate}"
            } else {
                val minDate = selectedTrips.minOf { it.tripDate }
                val maxDate = selectedTrips.maxOf { it.tripDate }
                customPeriodText = "Transport Bill From $minDate to $maxDate"
            }
        } else {
            customPeriodText = ""
        }
    }

    Scaffold(
        topBar = {
            ThombreTopAppBar(
                title = vehicle?.let { "Bill: ${RegistrationNumberValidator.formatForDisplay(it.registrationNumber)}" } ?: "Generate Bill",
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        val v = vehicle
        if (v == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ThombreRed)
            }
        } else {
            val isAc = v.vehicleType.equals("AC", ignoreCase = true)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(SurfaceBg)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Card: Client Selection & Billing Period
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(text = "Invoice Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ThombreRedDark)

                                // Client Autocomplete Field
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = clientName,
                                        onValueChange = {
                                            clientName = it
                                            clientExpanded = it.isNotBlank() && clientSuggestions.any { s -> s.contains(it, ignoreCase = true) }
                                        },
                                        label = { Text("Client Name / Company ('To') *") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    DropdownMenu(
                                        expanded = clientExpanded,
                                        onDismissRequest = { clientExpanded = false }
                                    ) {
                                        clientSuggestions.filter { it.contains(clientName, ignoreCase = true) }.take(5).forEach { suggestion ->
                                            DropdownMenuItem(
                                                text = { Text(suggestion) },
                                                onClick = {
                                                    clientName = suggestion
                                                    clientExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = customPeriodText,
                                    onValueChange = { customPeriodText = it },
                                    label = { Text("Billing Period Label (Editable)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Trips Selection Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select Trips (${selectedTripIds.size} / ${allTrips.size})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (allTrips.isNotEmpty()) {
                                TextButton(onClick = {
                                    if (selectedTripIds.size == allTrips.size) {
                                        selectedTripIds.clear()
                                    } else {
                                        selectedTripIds.clear()
                                        selectedTripIds.addAll(allTrips.map { it.id })
                                    }
                                }) {
                                    Text(if (selectedTripIds.size == allTrips.size) "Deselect All" else "Select All")
                                }
                            }
                        }
                    }

                    // Trip Checkbox Items
                    if (allTrips.isEmpty()) {
                        item {
                            Text("No trips logged for this vehicle yet. Please log trips first.", color = TextSecondary, fontSize = 14.sp)
                        }
                    } else {
                        items(allTrips, key = { it.id }) { trip ->
                            val isSelected = selectedTripIds.contains(trip.id)
                            val isBilled = trip.billingStatus == "BILLED"

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) {
                                            selectedTripIds.remove(trip.id)
                                        } else {
                                            if (isBilled) {
                                                tripToConfirmRebilling = trip
                                            } else {
                                                selectedTripIds.add(trip.id)
                                            }
                                        }
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFFFEBEE) else CardBg
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            if (checked == true) {
                                                if (isBilled) {
                                                    tripToConfirmRebilling = trip
                                                } else {
                                                    selectedTripIds.add(trip.id)
                                                }
                                            } else {
                                                selectedTripIds.remove(trip.id)
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = trip.tripDate, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            if (isBilled) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    color = BilledBadgeBg,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "BILLED (Bill #${trip.linkedBillNumber ?: ""})",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.DarkGray,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        if (isAc) {
                                            Text(text = "${trip.sourceLocation} TO ${trip.destinationLocation}", fontSize = 12.sp, color = TextPrimary)
                                            Text(text = "Amount: ₹ ${trip.baseAmount}  |  Toll: ₹ ${trip.escortTollCharges}", fontSize = 12.sp, color = TextSecondary)
                                        } else {
                                            Text(text = "${trip.kmTraveled} KM @ ₹ ${trip.ratePerKm}/KM (Net: ₹ ${trip.finalAmount})", fontSize = 12.sp, color = TextPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Summary & Actions Bar
                Surface(
                    color = CardBg,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val totalPayable = if (isAc) {
                            selectedTrips.sumOf { it.baseAmount + it.escortTollCharges }
                        } else {
                            selectedTrips.sumOf { it.finalAmount }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Selected: ${selectedTrips.size} trips", fontSize = 13.sp, color = TextSecondary)
                            Text(
                                text = "Total: ₹ ${String.format(java.util.Locale.ENGLISH, "%.2f", totalPayable)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ThombreRedDark
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                isGenerating = true
                                viewModel.generateBill(
                                    vehicle = v,
                                    clientName = clientName,
                                    customPeriodText = customPeriodText,
                                    selectedTrips = selectedTrips,
                                    onSuccess = { generatedPdfFile ->
                                        isGenerating = false
                                        Toast.makeText(context, "Bill generated successfully!", Toast.LENGTH_SHORT).show()
                                        // Immediately launch Android Native Share Sheet
                                        viewModel.sharePdf(context, generatedPdfFile.name)
                                        onBillGenerated()
                                    },
                                    onError = { err ->
                                        isGenerating = false
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            enabled = !isGenerating && selectedTrips.isNotEmpty() && clientName.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ThombreRed)
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Generate & Share Bill", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Popup for Selecting a Previously Billed Trip
    tripToConfirmRebilling?.let { billedTrip ->
        WarningDialog(
            title = "Confirm Re-Billing",
            message = "This trip was already included in Bill #${billedTrip.linkedBillNumber ?: ""}. Are you sure you want to bill it again?",
            confirmText = "Yes, Include",
            onConfirm = {
                selectedTripIds.add(billedTrip.id)
                tripToConfirmRebilling = null
            },
            onDismiss = { tripToConfirmRebilling = null }
        )
    }
}
