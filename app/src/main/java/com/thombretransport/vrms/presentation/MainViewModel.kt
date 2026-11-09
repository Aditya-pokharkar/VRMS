package com.thombretransport.vrms.presentation

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thombretransport.vrms.data.local.database.AppDatabase
import com.thombretransport.vrms.data.local.entity.*
import com.thombretransport.vrms.data.local.model.ClientRevenueSummary
import com.thombretransport.vrms.data.local.model.RouteFrequency
import com.thombretransport.vrms.domain.util.IndianNumberToWordsConverter
import com.thombretransport.vrms.domain.util.InsightsHelper
import com.thombretransport.vrms.domain.util.RegistrationNumberValidator
import com.thombretransport.vrms.engine.backup.ZipBackupManager
import com.thombretransport.vrms.engine.pdf.PdfInvoiceGenerator
import com.thombretransport.vrms.presentation.insights.IdleVehicleItem
import com.thombretransport.vrms.presentation.insights.InsightsUiState
import com.thombretransport.vrms.presentation.insights.UnbilledTruckItem
import com.thombretransport.vrms.presentation.insights.VehicleRevenueItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val vehicleDao = db.vehicleDao()
    private val tripDao = db.tripDao()
    private val billDao = db.billDao()
    private val appConfigDao = db.appConfigDao()
    private val historyDao = db.historyDao()

    private val pdfGenerator = PdfInvoiceGenerator(application)
    private val backupManager = ZipBackupManager(application)

    // UI States
    val showDeactivated = MutableStateFlow(false)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val vehicles: StateFlow<List<VehicleEntity>> = showDeactivated.flatMapLatest { showAll ->
        if (showAll) vehicleDao.getAllVehicles() else vehicleDao.getActiveVehicles()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBills: StateFlow<List<BillEntity>> = billDao.getAllBills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appConfig: StateFlow<AppConfigEntity?> = appConfigDao.getConfigFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val clientSuggestions: StateFlow<List<String>> = historyDao.getClientSuggestions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val locationSuggestions: StateFlow<List<String>> = historyDao.getLocationSuggestions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- INSIGHTS & ANALYTICS ---
    private val startOfMonth = InsightsHelper.getStartOfMonthMillis()
    private val startOfLastMonth = InsightsHelper.getStartOfLastMonthMillis()
    private val currentMonthName = InsightsHelper.getCurrentMonthName()
    private val lastMonthName = InsightsHelper.getLastMonthName()

    private val financialFlow = combine(
        tripDao.getUnbilledTotalAmount(),
        tripDao.getUnbilledTripCount(),
        tripDao.getUnbilledTollTotal(),
        billDao.getBilledAmountSince(startOfMonth),
        billDao.getBillCountSince(startOfMonth)
    ) { unbilledTotal, unbilledTrips, unbilledToll, thisMonthBilled, thisMonthBills ->
        FinancialGroup(
            unbilledTotal = unbilledTotal,
            unbilledTrips = unbilledTrips,
            unbilledToll = unbilledToll,
            thisMonthBilled = thisMonthBilled,
            thisMonthBills = thisMonthBills
        )
    }

    private val monthlyDiscountFlow = combine(
        billDao.getBilledAmountBetween(startOfLastMonth, startOfMonth),
        billDao.getDiscountAmountSince(startOfMonth),
        billDao.getTotalDiscountAllTime(),
        billDao.getTotalBilledAllTime()
    ) { lastMonthBilled, thisMonthDiscount, allTimeDiscount, allTimeBilled ->
        MonthlyDiscountGroup(
            lastMonthBilled = lastMonthBilled,
            thisMonthDiscount = thisMonthDiscount,
            allTimeDiscount = allTimeDiscount,
            allTimeBilled = allTimeBilled
        )
    }

    private val fleetStatsFlow = combine(
        vehicleDao.getActiveVehicles(),
        tripDao.getAllVehicleTripStats(),
        tripDao.getVehicleLastTrips(),
        tripDao.getUnbilledVehicleSummaries()
    ) { activeVehicles, tripStats, lastTrips, unbilledSummaries ->
        val tripStatsMap = tripStats.associateBy { it.vehicleId }
        val lastTripMap = lastTrips.associateBy { it.vehicleId }
        val unbilledMap = unbilledSummaries.associateBy { it.vehicleId }

        val fleetTotalEarned = tripStats.sumOf { it.totalEarned }

        val leaderboard = activeVehicles.map { v ->
            val stats = tripStatsMap[v.id]
            val earned = stats?.totalEarned ?: 0.0
            val pct = if (fleetTotalEarned > 0) ((earned / fleetTotalEarned) * 100).toFloat() else 0f
            VehicleRevenueItem(
                vehicleId = v.id,
                registrationNumber = v.registrationNumber,
                vehicleType = v.vehicleType,
                totalEarned = earned,
                tripCount = stats?.tripCount ?: 0,
                totalKm = stats?.totalKm ?: 0.0,
                percentageOfFleet = pct
            )
        }.sortedByDescending { it.totalEarned }

        val idleTrucks = activeVehicles.mapNotNull { v ->
            val lastTrip = lastTripMap[v.id]
            val daysIdle = InsightsHelper.calculateDaysIdle(
                lastTrip?.lastTripDate,
                lastTrip?.lastTripCreatedAt,
                v.createdAt
            )
            if (daysIdle >= 7) {
                IdleVehicleItem(
                    vehicleId = v.id,
                    registrationNumber = v.registrationNumber,
                    vehicleType = v.vehicleType,
                    daysIdle = daysIdle,
                    lastTripDate = lastTrip?.lastTripDate
                )
            } else null
        }.sortedByDescending { it.daysIdle }

        val unbilledTrucks = activeVehicles.mapNotNull { v ->
            val unbilled = unbilledMap[v.id]
            if (unbilled != null && unbilled.tripCount > 0) {
                UnbilledTruckItem(
                    vehicleId = v.id,
                    registrationNumber = v.registrationNumber,
                    vehicleType = v.vehicleType,
                    tripCount = unbilled.tripCount,
                    totalAmount = unbilled.totalAmount
                )
            } else null
        }.sortedByDescending { it.totalAmount }

        FleetGroup(
            leaderboard = leaderboard,
            idleTrucks = idleTrucks,
            unbilledTrucks = unbilledTrucks
        )
    }

    private val clientAndRoutesFlow = combine(
        billDao.getTopClients(10),
        tripDao.getFrequentRoutes(10)
    ) { topClients, routes ->
        val totalClientEarnings = topClients.sumOf { it.totalAmount }
        ClientRoutesGroup(
            topClients = topClients,
            frequentRoutes = routes,
            totalClientEarnings = totalClientEarnings
        )
    }

    val insightsUiState: StateFlow<InsightsUiState> = combine(
        financialFlow,
        monthlyDiscountFlow,
        fleetStatsFlow,
        clientAndRoutesFlow
    ) { fin, monthly, fleet, clientRoutes ->
        InsightsUiState(
            unbilledTotalAmount = fin.unbilledTotal,
            unbilledTripCount = fin.unbilledTrips,
            thisMonthBilledAmount = fin.thisMonthBilled,
            thisMonthBillCount = fin.thisMonthBills,
            idleTrucksCount = fleet.idleTrucks.size,

            lastMonthBilledAmount = monthly.lastMonthBilled,
            totalBilledAllTime = monthly.allTimeBilled,
            unbilledTollAmount = fin.unbilledToll,
            thisMonthDiscountAmount = monthly.thisMonthDiscount,
            allTimeDiscountAmount = monthly.allTimeDiscount,
            unbilledTrucks = fleet.unbilledTrucks,

            truckLeaderboard = fleet.leaderboard,
            idleTrucks = fleet.idleTrucks,

            topClients = clientRoutes.topClients,
            frequentRoutes = clientRoutes.frequentRoutes,
            totalClientEarnings = clientRoutes.totalClientEarnings,

            currentMonthName = currentMonthName,
            lastMonthName = lastMonthName
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InsightsUiState())

    // --- VEHICLE ACTIONS ---

    suspend fun getVehicle(id: Long): VehicleEntity? {
        return withContext(Dispatchers.IO) {
            vehicleDao.getVehicleById(id)
        }
    }

    fun addVehicle(regNumber: String, vehicleType: String, fuelType: String, notes: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val normalized = RegistrationNumberValidator.normalize(regNumber)
            if (!RegistrationNumberValidator.isValid(normalized)) {
                onResult(false, "Invalid Registration Number format. Use standard Indian format e.g. MH14XX1234 or 22BH1234AA")
                return@launch
            }

            val existingActive = withContext(Dispatchers.IO) {
                vehicleDao.findActiveByRegistration(normalized)
            }
            if (existingActive != null) {
                onResult(false, "An active vehicle with registration $normalized already exists.")
                return@launch
            }

            withContext(Dispatchers.IO) {
                vehicleDao.insert(
                    VehicleEntity(
                        registrationNumber = normalized,
                        vehicleType = vehicleType,
                        fuelType = fuelType,
                        otherDetails = notes,
                        status = "ACTIVE"
                    )
                )
            }
            onResult(true, "Vehicle added successfully.")
        }
    }

    fun deactivateVehicle(vehicle: VehicleEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            vehicleDao.update(vehicle.copy(status = "DEACTIVATED"))
        }
    }

    fun reactivateVehicle(vehicle: VehicleEntity, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val existingActive = withContext(Dispatchers.IO) {
                vehicleDao.findActiveByRegistration(vehicle.registrationNumber)
            }
            if (existingActive != null && existingActive.id != vehicle.id) {
                onResult(false, "Cannot reactivate: Another active vehicle already uses registration ${vehicle.registrationNumber}")
                return@launch
            }
            withContext(Dispatchers.IO) {
                vehicleDao.update(vehicle.copy(status = "ACTIVE"))
            }
            onResult(true, "Vehicle reactivated.")
        }
    }

    fun deleteVehicle(vehicle: VehicleEntity, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val tripsCount = withContext(Dispatchers.IO) { vehicleDao.countTripsForVehicle(vehicle.id) }
            val billsCount = withContext(Dispatchers.IO) { vehicleDao.countBillsForVehicle(vehicle.id) }

            if (tripsCount > 0 || billsCount > 0) {
                onResult(false, "Cannot permanently delete: This vehicle has linked historical trips or bills. Keep it Deactivated.")
                return@launch
            }

            withContext(Dispatchers.IO) {
                vehicleDao.delete(vehicle)
            }
            onResult(true, "Vehicle deleted.")
        }
    }

    // --- TRIP ACTIONS ---

    fun getTripsForVehicle(vehicleId: Long): Flow<List<TripEntity>> {
        return tripDao.getTripsForVehicle(vehicleId)
    }

    fun getUnbilledTripsForVehicle(vehicleId: Long): Flow<List<TripEntity>> {
        return tripDao.getUnbilledTripsForVehicle(vehicleId)
    }

    fun saveTrip(trip: TripEntity, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            tripDao.insert(trip)
            // Save autocomplete history
            if (trip.sourceLocation.isNotBlank()) {
                historyDao.upsertLocation(LocationHistoryEntity(locationName = trip.sourceLocation.trim()))
            }
            if (trip.destinationLocation.isNotBlank()) {
                historyDao.upsertLocation(LocationHistoryEntity(locationName = trip.destinationLocation.trim()))
            }
            // If editing a trip marked as BILLED, flag the bill as OUT_OF_SYNC
            if (trip.billingStatus == "BILLED" && trip.linkedBillId != null) {
                billDao.markBillOutOfSync(trip.linkedBillId)
            }
            withContext(Dispatchers.Main) { onComplete() }
        }
    }

    fun deleteTrip(trip: TripEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            if (trip.billingStatus == "BILLED" && trip.linkedBillId != null) {
                billDao.markBillOutOfSync(trip.linkedBillId)
            }
            tripDao.delete(trip)
        }
    }

    // --- BILL GENERATION ENGINE ---

    fun generateBill(
        vehicle: VehicleEntity,
        clientName: String,
        customPeriodText: String,
        selectedTrips: List<TripEntity>,
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (clientName.isBlank()) {
                onError("Client name is required.")
                return@launch
            }
            if (selectedTrips.isEmpty()) {
                onError("Please select at least one trip.")
                return@launch
            }

            try {
                val pdfFile = withContext(Dispatchers.IO) {
                    val config = appConfigDao.getConfig() ?: AppConfigEntity()
                    val nextNum = config.nextBillNumber

                    val isAc = vehicle.vehicleType.equals("AC", ignoreCase = true)
                    val totalPrimary: Double
                    val totalGross: Double
                    val totalDiscount: Double
                    val finalPayable: Double

                    if (isAc) {
                        val toll = selectedTrips.sumOf { it.escortTollCharges }
                        val gross = selectedTrips.sumOf { it.baseAmount }
                        totalPrimary = toll
                        totalGross = gross
                        totalDiscount = 0.0
                        finalPayable = gross + toll // Out-of-pocket tolls reimbursed by client
                    } else {
                        val km = selectedTrips.sumOf { it.kmTraveled }
                        val orig = selectedTrips.sumOf { it.originalAmount }
                        val net = selectedTrips.sumOf { it.finalAmount }
                        totalPrimary = km
                        totalGross = orig
                        totalDiscount = orig - net
                        finalPayable = net
                    }

                    val billDateStr = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Date())

                    // 1. Generate PDF Canvas
                    val file = pdfGenerator.generateInvoicePdf(
                        billNumber = nextNum,
                        billDate = billDateStr,
                        vehicleReg = vehicle.registrationNumber,
                        vehicleType = vehicle.vehicleType,
                        clientName = clientName.trim(),
                        billingPeriodText = customPeriodText.trim(),
                        trips = selectedTrips,
                        config = config
                    )

                    // 2. Insert Bill Record
                    val billEntity = BillEntity(
                        billNumber = nextNum,
                        vehicleId = vehicle.id,
                        vehicleRegSnapshot = vehicle.registrationNumber,
                        clientName = clientName.trim(),
                        billDate = billDateStr,
                        billingPeriodText = customPeriodText.trim(),
                        vehicleType = vehicle.vehicleType,
                        totalPrimaryMetric = totalPrimary,
                        totalGrossAmount = totalGross,
                        totalDiscountAmount = totalDiscount,
                        finalPayableAmount = finalPayable,
                        amountInWords = IndianNumberToWordsConverter.convert(finalPayable),
                        pdfRelativePath = file.name,
                        isOutOfSync = false
                    )
                    val billId = billDao.insertBill(billEntity)

                    // 3. Insert Frozen Bill Items
                    val items = selectedTrips.mapIndexed { index, trip ->
                        BillItemEntity(
                            billId = billId,
                            originalTripId = trip.id,
                            sequenceNumber = index + 1,
                            frozenTripDate = trip.tripDate,
                            frozenDetailsJson = if (isAc) "${trip.sourceLocation} TO ${trip.destinationLocation}" else "${trip.kmTraveled} KM @ ₹${trip.ratePerKm}",
                            frozenGrossAmount = if (isAc) trip.baseAmount else trip.originalAmount,
                            frozenDeductionOrDiscount = if (isAc) trip.escortTollCharges else trip.discountPercentage,
                            frozenNetAmount = trip.finalAmount
                        )
                    }
                    billDao.insertBillItems(items)

                    // 4. Mark trips as BILLED
                    val tripIds = selectedTrips.map { it.id }
                    tripDao.markTripsAsBilled(tripIds, billId, nextNum)

                    // 5. Increment bill number and record client history
                    appConfigDao.incrementBillNumber()
                    historyDao.upsertClient(ClientHistoryEntity(clientName = clientName.trim()))

                    file
                }

                onSuccess(pdfFile)
            } catch (e: Exception) {
                onError("Failed to generate bill: ${e.localizedMessage}")
            }
        }
    }

    // --- SHARE PDF VIA ANDROID SHARE SHEET ---

    fun sharePdf(context: Context, pdfFileName: String) {
        val docsDir = File(context.getExternalFilesDir(null), "Documents/bills")
        val file = File(docsDir, pdfFileName)
        if (!file.exists()) return

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Transport Bill: ${file.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Invoice via"))
    }

    fun openPdf(context: Context, pdfFileName: String) {
        val docsDir = File(context.getExternalFilesDir(null), "Documents/bills")
        val file = File(docsDir, pdfFileName)
        if (!file.exists()) return

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            sharePdf(context, pdfFileName)
        }
    }

    // --- BACKUP & RESTORE ACTIONS ---

    fun exportBackup(outputStream: OutputStream, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = backupManager.exportBackup(outputStream)
            withContext(Dispatchers.Main) { onResult(success) }
        }
    }

    fun restoreBackup(inputStream: InputStream, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = backupManager.restoreBackup(inputStream)
            withContext(Dispatchers.Main) { onResult(success) }
        }
    }

    fun updatePan(pan: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appConfigDao.updatePanNumber(pan.uppercase().trim())
        }
    }
}

private data class FinancialGroup(
    val unbilledTotal: Double,
    val unbilledTrips: Int,
    val unbilledToll: Double,
    val thisMonthBilled: Double,
    val thisMonthBills: Int
)

private data class MonthlyDiscountGroup(
    val lastMonthBilled: Double,
    val thisMonthDiscount: Double,
    val allTimeDiscount: Double,
    val allTimeBilled: Double
)

private data class FleetGroup(
    val leaderboard: List<VehicleRevenueItem>,
    val idleTrucks: List<IdleVehicleItem>,
    val unbilledTrucks: List<UnbilledTruckItem>
)

private data class ClientRoutesGroup(
    val topClients: List<ClientRevenueSummary>,
    val frequentRoutes: List<RouteFrequency>,
    val totalClientEarnings: Double
)
