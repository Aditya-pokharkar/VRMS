package com.thombretransport.vrms.presentation.triplogger

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thombretransport.vrms.data.local.entity.TripEntity
import com.thombretransport.vrms.data.local.entity.VehicleEntity
import com.thombretransport.vrms.domain.util.IndianNumberToWordsConverter
import com.thombretransport.vrms.domain.util.RegistrationNumberValidator
import com.thombretransport.vrms.presentation.MainViewModel
import com.thombretransport.vrms.presentation.components.*
import com.thombretransport.vrms.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripLoggerScreen(
    vehicleId: Long,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var vehicle by remember { mutableStateOf<VehicleEntity?>(null) }

    LaunchedEffect(vehicleId) {
        vehicle = viewModel.getVehicle(vehicleId)
    }

    val trips by viewModel.getTripsForVehicle(vehicleId).collectAsState(initial = emptyList())
    val locationSuggestions by viewModel.locationSuggestions.collectAsState()

    var showTripFormSheet by remember { mutableStateOf(false) }
    var tripToEdit by remember { mutableStateOf<TripEntity?>(null) }
    var tripPendingModificationWarning by remember { mutableStateOf<TripEntity?>(null) }
    var tripPendingDeletionWarning by remember { mutableStateOf<TripEntity?>(null) }

    Scaffold(
        topBar = {
            ThombreTopAppBar(
                title = vehicle?.let { "${RegistrationNumberValidator.formatForDisplay(it.registrationNumber)} (${it.vehicleType})" } ?: "Trip History",
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    tripToEdit = null
                    showTripFormSheet = true
                },
                containerColor = ThombreRed,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log New Trip")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log Trip", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    ) { padding ->
        val v = vehicle
        if (v == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ThombreRed)
            }
        } else {
            val unbilledCount = trips.count { it.billingStatus == "UNBILLED" }
            val billedCount = trips.count { it.billingStatus == "BILLED" }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(SurfaceBg)
            ) {
                // Vehicle Stats Header Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Total Trips", fontSize = 11.sp, color = TextSecondary)
                            Text(text = "${trips.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Divider(modifier = Modifier.height(30.dp).width(1.dp), color = Color.LightGray)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Unbilled", fontSize = 11.sp, color = TextSecondary)
                            Text(text = "$unbilledCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ThombreRedDark)
                        }
                        Divider(modifier = Modifier.height(30.dp).width(1.dp), color = Color.LightGray)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Billed", fontSize = 11.sp, color = TextSecondary)
                            Text(text = "$billedCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                    }
                }

                // Trips History List Section
                if (trips.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No trips logged yet for this vehicle.",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap the '+ Log Trip' button below to record the first trip.",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "Logged Trips History (${trips.size})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(trips, key = { it.id }) { trip ->
                            TripItemCard(
                                trip = trip,
                                isAc = v.vehicleType.equals("AC", ignoreCase = true),
                                onEdit = {
                                    if (trip.billingStatus == "BILLED") {
                                        tripPendingModificationWarning = trip
                                    } else {
                                        tripToEdit = trip
                                        showTripFormSheet = true
                                    }
                                },
                                onDelete = {
                                    if (trip.billingStatus == "BILLED") {
                                        tripPendingDeletionWarning = trip
                                    } else {
                                        viewModel.deleteTrip(trip)
                                        Toast.makeText(context, "Trip deleted", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Bottom Sheet: Opens cleanly when + is clicked or trip is edited
            if (showTripFormSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        showTripFormSheet = false
                        tripToEdit = null
                    },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 36.dp)
                    ) {
                        Text(
                            text = if (tripToEdit != null) "Edit Trip Entry" else "Log New Trip Details",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThombreRedDark
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        val isAc = v.vehicleType.equals("AC", ignoreCase = true)
                        if (isAc) {
                            AcTripForm(
                                vehicleId = v.id,
                                vehicleType = v.vehicleType,
                                tripToEdit = tripToEdit,
                                locationSuggestions = locationSuggestions,
                                onSave = { trip ->
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    val isEditing = tripToEdit != null
                                    viewModel.saveTrip(trip) {
                                        tripToEdit = null
                                        showTripFormSheet = false
                                        Toast.makeText(
                                            context,
                                            if (isEditing) "Trip updated successfully" else "Trip logged successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                onCancelEdit = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    tripToEdit = null
                                    showTripFormSheet = false
                                }
                            )
                        } else {
                            NonAcTripForm(
                                vehicleId = v.id,
                                vehicleType = v.vehicleType,
                                tripToEdit = tripToEdit,
                                onSave = { trip ->
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    val isEditing = tripToEdit != null
                                    viewModel.saveTrip(trip) {
                                        tripToEdit = null
                                        showTripFormSheet = false
                                        Toast.makeText(
                                            context,
                                            if (isEditing) "Trip updated successfully" else "Trip logged successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                onCancelEdit = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    tripToEdit = null
                                    showTripFormSheet = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Warning Dialog for Editing Billed Trip
    tripPendingModificationWarning?.let { billedTrip ->
        WarningDialog(
            title = "Warning: Billed Trip",
            message = "This trip is linked to Bill #${billedTrip.linkedBillNumber ?: "N/A"}. Modifying it will cause your database to mismatch the saved PDF bill.",
            confirmText = "Proceed to Edit",
            onConfirm = {
                tripToEdit = billedTrip
                showTripFormSheet = true
                tripPendingModificationWarning = null
            },
            onDismiss = { tripPendingModificationWarning = null }
        )
    }

    // Warning Dialog for Deleting Billed Trip
    tripPendingDeletionWarning?.let { billedTrip ->
        WarningDialog(
            title = "Warning: Billed Trip",
            message = "This trip is linked to Bill #${billedTrip.linkedBillNumber ?: "N/A"}. Modifying it will cause your database to mismatch the saved PDF bill.",
            confirmText = "Delete Anyway",
            onConfirm = {
                viewModel.deleteTrip(billedTrip)
                tripPendingDeletionWarning = null
                Toast.makeText(context, "Trip deleted", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { tripPendingDeletionWarning = null }
        )
    }
}

@Composable
fun AcTripForm(
    vehicleId: Long,
    vehicleType: String,
    tripToEdit: TripEntity?,
    locationSuggestions: List<String>,
    onSave: (TripEntity) -> Unit,
    onCancelEdit: () -> Unit
) {
    var tripDate by remember(tripToEdit) {
        mutableStateOf(tripToEdit?.tripDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()))
    }
    var source by remember(tripToEdit) { mutableStateOf(tripToEdit?.sourceLocation ?: "") }
    var destination by remember(tripToEdit) { mutableStateOf(tripToEdit?.destinationLocation ?: "") }
    var invoiceNumber by remember(tripToEdit) { mutableStateOf(tripToEdit?.clientInvoiceRef ?: "") }
    var tollChargesStr by remember(tripToEdit) { mutableStateOf(tripToEdit?.let { it.escortTollCharges.toString() } ?: "") }
    var amountStr by remember(tripToEdit) { mutableStateOf(tripToEdit?.let { it.baseAmount.toString() } ?: "") }

    var sourceExpanded by remember { mutableStateOf(false) }
    var destExpanded by remember { mutableStateOf(false) }

    val filteredSource = remember(source, locationSuggestions) {
        if (source.isBlank()) emptyList()
        else locationSuggestions.filter { it.contains(source, ignoreCase = true) && !it.equals(source, ignoreCase = true) }.take(5)
    }

    val filteredDest = remember(destination, locationSuggestions) {
        if (destination.isBlank()) emptyList()
        else locationSuggestions.filter { it.contains(destination, ignoreCase = true) && !it.equals(destination, ignoreCase = true) }.take(5)
    }

    val amount = amountStr.toDoubleOrNull() ?: 0.0
    val tollCharges = tollChargesStr.toDoubleOrNull() ?: 0.0

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DatePickerField(
            label = "Trip Date (Current or Past Date)",
            selectedDateIso = tripDate,
            onDateSelected = { tripDate = it }
        )

        // Source Location with Autocomplete
        @OptIn(ExperimentalMaterial3Api::class)
        ExposedDropdownMenuBox(
            expanded = sourceExpanded && filteredSource.isNotEmpty(),
            onExpandedChange = { sourceExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = source,
                onValueChange = {
                    source = it
                    sourceExpanded = it.isNotBlank()
                },
                label = { Text("Source Location") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = sourceExpanded && filteredSource.isNotEmpty(),
                onDismissRequest = { sourceExpanded = false }
            ) {
                filteredSource.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            source = suggestion
                            sourceExpanded = false
                        }
                    )
                }
            }
        }

        // Destination Location with Autocomplete
        @OptIn(ExperimentalMaterial3Api::class)
        ExposedDropdownMenuBox(
            expanded = destExpanded && filteredDest.isNotEmpty(),
            onExpandedChange = { destExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = destination,
                onValueChange = {
                    destination = it
                    destExpanded = it.isNotBlank()
                },
                label = { Text("Destination Location") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = destExpanded && filteredDest.isNotEmpty(),
                onDismissRequest = { destExpanded = false }
            ) {
                filteredDest.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            destination = suggestion
                            destExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = invoiceNumber,
            onValueChange = { invoiceNumber = it },
            label = { Text("Invoice Number (Client Reference)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = tollChargesStr,
            onValueChange = { tollChargesStr = it },
            label = { Text("Escort / Toll Charges (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = amountStr,
            onValueChange = { amountStr = it },
            label = { Text("Core Amount (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        // Live Dynamic Indian Amount in Words
        IndianWordsCard(words = IndianNumberToWordsConverter.convert(amount))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(onClick = onCancelEdit, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    if (amount <= 0.0) return@Button
                    val trip = TripEntity(
                        id = tripToEdit?.id ?: 0L,
                        vehicleId = vehicleId,
                        tripDate = tripDate,
                        vehicleTypeAtTrip = vehicleType,
                        billingStatus = tripToEdit?.billingStatus ?: "UNBILLED",
                        sourceLocation = source.trim(),
                        destinationLocation = destination.trim(),
                        clientInvoiceRef = invoiceNumber.trim(),
                        escortTollCharges = tollCharges,
                        baseAmount = amount,
                        finalAmount = amount,
                        linkedBillId = tripToEdit?.linkedBillId,
                        linkedBillNumber = tripToEdit?.linkedBillNumber
                    )
                    onSave(trip)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = ThombreRed)
            ) {
                Text(if (tripToEdit != null) "Update Trip" else "Save Trip", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun NonAcTripForm(
    vehicleId: Long,
    vehicleType: String,
    tripToEdit: TripEntity?,
    onSave: (TripEntity) -> Unit,
    onCancelEdit: () -> Unit
) {
    var tripDate by remember(tripToEdit) {
        mutableStateOf(tripToEdit?.tripDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()))
    }
    var kmStr by remember(tripToEdit) { mutableStateOf(tripToEdit?.let { it.kmTraveled.toString() } ?: "") }
    var rateStr by remember(tripToEdit) { mutableStateOf(tripToEdit?.let { it.ratePerKm.toString() } ?: "") }
    var discountStr by remember(tripToEdit) { mutableStateOf(tripToEdit?.let { it.discountPercentage.toString() } ?: "") }

    val km = kmStr.toDoubleOrNull() ?: 0.0
    val rate = rateStr.toDoubleOrNull() ?: 0.0
    val discountPercent = discountStr.toDoubleOrNull() ?: 0.0

    val originalTotal = km * rate
    val netAmount = if (discountPercent in 0.01..99.99) {
        originalTotal - (originalTotal * (discountPercent / 100.0))
    } else {
        originalTotal
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DatePickerField(
            label = "Trip Date (Current or Past Date)",
            selectedDateIso = tripDate,
            onDateSelected = { tripDate = it }
        )

        OutlinedTextField(
            value = kmStr,
            onValueChange = { kmStr = it },
            label = { Text("KM (Kilometres)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = rateStr,
            onValueChange = { rateStr = it },
            label = { Text("Rate per KM (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = discountStr,
            onValueChange = { discountStr = it },
            label = { Text("Discount (%) (Optional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        // Strike-Through Visual Requirement
        StrikeThroughDiscountAmount(
            originalAmount = originalTotal,
            discountPercent = discountPercent,
            netAmount = netAmount
        )

        // Dynamic Amount in Words
        IndianWordsCard(words = IndianNumberToWordsConverter.convert(netAmount))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(onClick = onCancelEdit, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    if (km <= 0.0 || rate <= 0.0) return@Button
                    val trip = TripEntity(
                        id = tripToEdit?.id ?: 0L,
                        vehicleId = vehicleId,
                        tripDate = tripDate,
                        vehicleTypeAtTrip = vehicleType,
                        billingStatus = tripToEdit?.billingStatus ?: "UNBILLED",
                        kmTraveled = km,
                        ratePerKm = rate,
                        originalAmount = originalTotal,
                        discountPercentage = discountPercent,
                        finalAmount = netAmount,
                        linkedBillId = tripToEdit?.linkedBillId,
                        linkedBillNumber = tripToEdit?.linkedBillNumber
                    )
                    onSave(trip)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = ThombreRed)
            ) {
                Text(if (tripToEdit != null) "Update Trip" else "Save Trip", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TripItemCard(
    trip: TripEntity,
    isAc: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isBilled = trip.billingStatus == "BILLED"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = trip.tripDate, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
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
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ThombreRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (isAc) {
                Text(
                    text = "${trip.sourceLocation} → ${trip.destinationLocation}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                if (trip.clientInvoiceRef.isNotBlank()) {
                    Text(text = "Inv Ref: ${trip.clientInvoiceRef}", fontSize = 12.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Toll: ₹ ${trip.escortTollCharges}", fontSize = 13.sp, color = TextSecondary)
                    Text(text = "Amount: ₹ ${trip.baseAmount}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ThombreRedDark)
                }
            } else {
                Text(
                    text = "${trip.kmTraveled} KM @ ₹ ${trip.ratePerKm}/KM",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (trip.discountPercentage > 0) {
                        Text(text = "Discount: ${trip.discountPercentage}%", fontSize = 13.sp, color = TextSecondary)
                    } else {
                        Text(text = "No Discount", fontSize = 13.sp, color = TextSecondary)
                    }
                    Text(text = "Net: ₹ ${trip.finalAmount}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ThombreRedDark)
                }
            }
        }
    }
}
