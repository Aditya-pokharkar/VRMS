package com.thombretransport.vrms.presentation.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thombretransport.vrms.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ThombreTopAppBar(
    title: String,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        color = ThombreRed,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onNavigateBack != null) {
                    TextButton(onClick = onNavigateBack) {
                        Text("←", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Thombre Transport",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                actions()
            }
        }
    }
}

@Composable
fun IndianWordsCard(words: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "₹ in Words: ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SuccessGreen
            )
            Text(
                text = words,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2E7D32)
            )
        }
    }
}

@Composable
fun StrikeThroughDiscountAmount(
    originalAmount: Double,
    discountPercent: Double,
    netAmount: Double
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        if (discountPercent > 0.0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Original: ₹ ${String.format(Locale.ENGLISH, "%.2f", originalAmount)}",
                    fontSize = 13.sp,
                    textDecoration = TextDecoration.LineThrough,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${String.format(Locale.ENGLISH, "%.1f", discountPercent)}% off)",
                    fontSize = 12.sp,
                    color = ThombreRed,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Text(
            text = "Total Net Amount: ₹ ${String.format(Locale.ENGLISH, "%.2f", netAmount)}",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = ThombreRedDark
        )
    }
}

@Composable
fun DatePickerField(
    label: String,
    selectedDateIso: String,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.currentOrSelected(selectedDateIso)

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val dialog = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val selected = String.format(Locale.ENGLISH, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                        onDateSelected(selected)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                // Block future dates
                dialog.datePicker.maxDate = System.currentTimeMillis()
                dialog.show()
            },
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = label, fontSize = 11.sp, color = TextSecondary)
                Text(
                    text = if (selectedDateIso.isNotBlank()) selectedDateIso else "Select Date",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Pick Date", tint = ThombreRed)
        }
    }
}

@Composable
fun WarningDialog(
    title: String,
    message: String,
    confirmText: String = "Proceed",
    cancelText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Warning, contentDescription = null, tint = WarningYellowBorder, modifier = Modifier.size(36.dp))
        },
        title = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = { Text(text = message, fontSize = 14.sp) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ThombreRed)
            ) {
                Text(confirmText, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(cancelText)
            }
        }
    )
}

@Composable
fun OutOfSyncBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, WarningYellowBorder, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = WarningYellowBg),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = WarningYellowBorder,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Warning: The underlying trip data has changed since this invoice was generated.",
                fontSize = 12.sp,
                color = Color(0xFF7F6000),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun Calendar.Companion.currentOrSelected(isoString: String): Calendar {
    val cal = Calendar.getInstance()
    if (isoString.isNotBlank()) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            cal.time = sdf.parse(isoString) ?: java.util.Date()
        } catch (_: Exception) {}
    }
    return cal
}
