package com.thombretransport.vrms.presentation.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thombretransport.vrms.domain.util.InsightsHelper
import com.thombretransport.vrms.presentation.MainViewModel
import com.thombretransport.vrms.presentation.components.ThombreTopAppBar
import com.thombretransport.vrms.presentation.theme.*

@Composable
fun InsightsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToBillCompilation: (Long) -> Unit
) {
    val state by viewModel.insightsUiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            ThombreTopAppBar(
                title = "Business Insights",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. AT A GLANCE (Top 3 KPI Numbers)
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Business Summary",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Top numbers you need to know today",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        // KPI 1: Unbilled Cash
                        item {
                            KpiHighlightCard(
                                title = "Unbilled Cash",
                                marathiTitle = "बिल बनवायचे बाकी",
                                value = InsightsHelper.formatRupees(state.unbilledTotalAmount),
                                badgeText = "${state.unbilledTripCount} Trips Waiting",
                                subtitle = "Completed trips ready for bill",
                                icon = Icons.Default.HourglassTop,
                                containerColor = Color(0xFFFFF8E1),
                                borderColor = Color(0xFFFFB300),
                                iconColor = Color(0xFFF57F17)
                            )
                        }

                        // KPI 2: This Month's Bills
                        item {
                            KpiHighlightCard(
                                title = "This Month's Bills",
                                marathiTitle = "या महिन्याचे बिल",
                                value = InsightsHelper.formatRupees(state.thisMonthBilledAmount),
                                badgeText = "${state.thisMonthBillCount} Bills (${state.currentMonthName.split(" ").firstOrNull() ?: ""})",
                                subtitle = "Total billed to clients this month",
                                icon = Icons.Default.CheckCircle,
                                containerColor = Color(0xFFE8F5E9),
                                borderColor = Color(0xFF66BB6A),
                                iconColor = Color(0xFF2E7D32)
                            )
                        }

                        // KPI 3: Idle Trucks
                        item {
                            val isWarning = state.idleTrucksCount > 0
                            KpiHighlightCard(
                                title = "Idle Trucks",
                                marathiTitle = "रिकाम्या गाड्या",
                                value = "${state.idleTrucksCount} Trucks",
                                badgeText = if (isWarning) "Needs Trip" else "All Busy 👍",
                                subtitle = "No trips in last 7 days",
                                icon = Icons.Default.LocalShipping,
                                containerColor = if (isWarning) Color(0xFFFFEBEE) else Color(0xFFF1F8E9),
                                borderColor = if (isWarning) Color(0xFFEF5350) else Color(0xFF81C784),
                                iconColor = if (isWarning) ThombreRed else SuccessGreen
                            )
                        }
                    }
                }
            }

            // 2. TAB SELECTOR
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = ThombreRed,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "Money & Bills",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "Truck Earnings",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        icon = { Icon(Icons.Default.Leaderboard, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                text = "Clients & Routes",
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        icon = { Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                }
            }

            // 3. TAB CONTENTS
            when (selectedTab) {
                0 -> {
                    // TAB 1: MONEY & BILLS
                    item {
                        MoneyAndBillsTab(
                            state = state,
                            onNavigateToBillCompilation = onNavigateToBillCompilation
                        )
                    }
                }
                1 -> {
                    // TAB 2: TRUCK PERFORMANCE & IDLE TRUCKS
                    item {
                        TruckPerformanceTab(state = state)
                    }
                }
                2 -> {
                    // TAB 3: TOP CLIENTS & FREQUENT ROUTES
                    item {
                        ClientsAndRoutesTab(state = state)
                    }
                }
            }
        }
    }
}

// ==========================================
// AT-A-GLANCE KPI HIGHLIGHT CARD
// ==========================================

@Composable
private fun KpiHighlightCard(
    title: String,
    marathiTitle: String,
    value: String,
    badgeText: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    borderColor: Color,
    iconColor: Color
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(iconColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = marathiTitle, fontSize = 10.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = borderColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 14.sp
            )
        }
    }
}

// ==========================================
// TAB 1: MONEY & BILLS
// ==========================================

@Composable
private fun MoneyAndBillsTab(
    state: InsightsUiState,
    onNavigateToBillCompilation: (Long) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {

        // 1. Monthly Revenue Comparison
        SectionHeader(
            title = "Monthly Bill Totals",
            subtitle = "Compare this month's bills with last month"
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // This Month
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "This Month (${state.currentMonthName})", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = InsightsHelper.formatRupees(state.thisMonthBilledAmount),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                        Text(text = "${state.thisMonthBillCount} bills generated", fontSize = 11.sp, color = TextSecondary)
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(50.dp)
                            .background(Color(0xFFE0E0E0))
                    )

                    // Last Month
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Text(text = "Last Month (${state.lastMonthName})", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = InsightsHelper.formatRupees(state.lastMonthBilledAmount),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(text = "Previous cycle", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(10.dp))

                // Difference indicator
                val diff = state.thisMonthBilledAmount - state.lastMonthBilledAmount
                val isMore = diff >= 0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isMore) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isMore) SuccessGreen else Color(0xFF757575),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isMore) {
                                "+${InsightsHelper.formatRupees(diff)} vs last month"
                            } else {
                                "-${InsightsHelper.formatRupees(-diff)} vs last month"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isMore) SuccessGreen else Color(0xFF757575)
                        )
                    }

                    Text(
                        text = "All Time: ${InsightsHelper.formatRupees(state.totalBilledAllTime)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Unbilled Cash by Truck (Where is my money waiting?)
        SectionHeader(
            title = "Unbilled Cash Waiting by Truck",
            subtitle = "Completed trips with no bill made yet. Tap 'Make Bill' to compile invoice."
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (state.unbilledTrucks.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "All Finished Trips are Billed!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SuccessGreen
                        )
                        Text(
                            text = "No pending trips waiting for bill. Great job keeping your accounts up to date!",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.unbilledTrucks.forEach { truck ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = truck.registrationNumber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = Color(0xFFF5F5F5),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = truck.vehicleType,
                                            fontSize = 10.sp,
                                            color = TextSecondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${truck.tripCount} trips waiting  •  ${InsightsHelper.formatRupees(truck.totalAmount)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ThombreRed
                                )
                            }

                            Button(
                                onClick = { onNavigateToBillCompilation(truck.vehicleId) },
                                colors = ButtonDefaults.buttonColors(containerColor = ThombreRed),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(text = "Make Bill →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Toll & Out-of-pocket highway money
        SectionHeader(
            title = "Toll Money Paid by Drivers",
            subtitle = "Highway tolls waiting to be billed & recovered from clients"
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEDE7F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Toll,
                        contentDescription = null,
                        tint = Color(0xFF673AB7),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = InsightsHelper.formatRupees(state.unbilledTollAmount),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF673AB7)
                    )
                    Text(
                        text = "Out-of-pocket tolls on unbilled trips. Once billed, clients reimburse this amount.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Discount Given Tracker
        SectionHeader(
            title = "Discount Concessions Given",
            subtitle = "Total discount savings passed to regular clients"
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "This Month's Discounts", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = InsightsHelper.formatRupees(state.thisMonthDiscountAmount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(text = "Discount concessions", fontSize = 11.sp, color = TextSecondary)
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(45.dp)
                        .background(Color(0xFFEEEEEE))
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Text(text = "All-Time Total Discounts", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = InsightsHelper.formatRupees(state.allTimeDiscountAmount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(text = "Given across all bills", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }
    }
}

// ==========================================
// TAB 2: TRUCK PERFORMANCE & IDLE TRUCKS
// ==========================================

@Composable
private fun TruckPerformanceTab(state: InsightsUiState) {
    Column(modifier = Modifier.padding(16.dp)) {

        // 1. Truck Revenue Leaderboard
        SectionHeader(
            title = "Truck Earnings Leaderboard",
            subtitle = "Ranked by highest earning truck to lowest"
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (state.truckLeaderboard.isEmpty()) {
            EmptyStateCard(message = "No active trucks found. Add trucks to see earnings leaderboard.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.truckLeaderboard.forEachIndexed { index, truck ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Rank Badge
                                    RankBadge(rank = index + 1)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = truck.registrationNumber,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${truck.vehicleType}  •  ${truck.tripCount} Trips" +
                                                    if (truck.totalKm > 0) "  •  ${truck.totalKm.toInt()} KM" else "",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Text(
                                    text = InsightsHelper.formatRupees(truck.totalEarned),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Share of Fleet Earnings Progress Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LinearProgressIndicator(
                                    progress = { (truck.percentageOfFleet / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (index == 0) Color(0xFFFFB300) else SuccessGreen,
                                    trackColor = Color(0xFFEEEEEE)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${truck.percentageOfFleet.toInt()}% of fleet",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Idle Trucks Alert (Needs Work)
        SectionHeader(
            title = "Trucks Sitting Idle",
            subtitle = "Active trucks that have not logged any trips in the last 7 days"
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (state.idleTrucks.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "All Trucks Are Busy!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SuccessGreen
                        )
                        Text(
                            text = "Every active truck in your fleet has logged at least one trip this week. 🚚💨",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.idleTrucks.forEach { truck ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFCDD2)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = ThombreRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = truck.registrationNumber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (truck.lastTripDate != null) "Last trip: ${truck.lastTripDate}" else "Never logged a trip yet",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Surface(
                                color = ThombreRed,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${truck.daysIdle} days idle",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 3: CLIENTS & ROUTES
// ==========================================

@Composable
private fun ClientsAndRoutesTab(state: InsightsUiState) {
    Column(modifier = Modifier.padding(16.dp)) {

        // 1. Top Regular Clients
        SectionHeader(
            title = "Top Regular Clients",
            subtitle = "Clients who gave you the most business across all bills"
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (state.topClients.isEmpty()) {
            EmptyStateCard(message = "No bills generated yet. Make bills to see your top regular clients.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.topClients.forEachIndexed { index, client ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RankBadge(rank = index + 1)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = client.clientName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${client.billCount} bills made" +
                                                if (state.totalClientEarnings > 0) {
                                                    "  •  ${((client.totalAmount / state.totalClientEarnings) * 100).toInt()}% of business"
                                                } else "",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Text(
                                text = InsightsHelper.formatRupees(client.totalAmount),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Most Frequent Travel Routes
        SectionHeader(
            title = "Most Frequent Travel Routes",
            subtitle = "Your top transport corridors from logged trips"
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (state.frequentRoutes.isEmpty()) {
            EmptyStateCard(message = "No trip locations recorded yet. Enter pickup and drop locations to see top routes.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.frequentRoutes.forEachIndexed { index, route ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE3F2FD)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Route,
                                        contentDescription = null,
                                        tint = Color(0xFF1976D2),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    val source = route.sourceLocation.ifBlank { "Pickup" }
                                    val destination = route.destinationLocation.ifBlank { "Drop" }
                                    Text(
                                        text = "$source  ➔  $destination",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Top travel path #${index + 1}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Surface(
                                color = Color(0xFFE3F2FD),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${route.tripCount} Trips",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1976D2),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// REUSABLE HELPER COMPONENTS
// ==========================================

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun RankBadge(rank: Int) {
    val (bgColor, textColor) = when (rank) {
        1 -> Color(0xFFFFD54F) to Color(0xFF5D4037) // Gold
        2 -> Color(0xFFCFD8DC) to Color(0xFF37474F) // Silver
        3 -> Color(0xFFFFCCBC) to Color(0xFF4E342E) // Bronze
        else -> Color(0xFFF5F5F5) to Color(0xFF757575)
    }

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "#$rank",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = message,
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
    }
}
