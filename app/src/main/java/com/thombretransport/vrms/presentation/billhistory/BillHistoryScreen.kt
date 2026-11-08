package com.thombretransport.vrms.presentation.billhistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thombretransport.vrms.data.local.entity.BillEntity
import com.thombretransport.vrms.domain.util.RegistrationNumberValidator
import com.thombretransport.vrms.presentation.MainViewModel
import com.thombretransport.vrms.presentation.components.OutOfSyncBanner
import com.thombretransport.vrms.presentation.components.ThombreTopAppBar
import com.thombretransport.vrms.presentation.theme.*
import java.text.DecimalFormat

@Composable
fun BillHistoryScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val bills by viewModel.allBills.collectAsState()
    val df = remember { DecimalFormat("#,##0.00") }

    Scaffold(
        topBar = {
            ThombreTopAppBar(
                title = "Generated Bills History",
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SurfaceBg)
        ) {
            if (bills.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No bills generated yet.", color = TextSecondary, fontSize = 15.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(bills, key = { it.id }) { bill ->
                        BillHistoryCard(
                            bill = bill,
                            df = df,
                            onOpen = { viewModel.openPdf(context, bill.pdfRelativePath) },
                            onShare = { viewModel.sharePdf(context, bill.pdfRelativePath) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BillHistoryCard(
    bill: BillEntity,
    df: DecimalFormat,
    onOpen: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Bill Number and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bill #${bill.billNumber}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ThombreRedDark
                )
                Text(
                    text = bill.billDate,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Client and Vehicle Info
            Text(
                text = "Client: ${bill.clientName}",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "Vehicle: ${RegistrationNumberValidator.formatForDisplay(bill.vehicleRegSnapshot)} (${bill.vehicleType})",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Amount Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Final Payable Amount:", fontSize = 13.sp, color = TextSecondary)
                Text(
                    text = "₹ ${df.format(bill.finalPayableAmount)}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThombreRed
                )
            }

            // Out-of-sync Warning Banner if trip mutated after bill creation
            if (bill.isOutOfSync) {
                Spacer(modifier = Modifier.height(10.dp))
                OutOfSyncBanner()
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onOpen,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View PDF")
                }
                Button(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ThombreRed)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Bill")
                }
            }
        }
    }
}
