package com.thombretransport.vrms.presentation.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thombretransport.vrms.presentation.MainViewModel
import com.thombretransport.vrms.presentation.components.ThombreTopAppBar
import com.thombretransport.vrms.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val config by viewModel.appConfig.collectAsState()
    var panText by remember(config) { mutableStateOf(config?.panNumber ?: "AIRPT2980M") }
    val scrollState = rememberScrollState()

    var isProcessingBackup by remember { mutableStateOf(false) }

    // SAF File Creator for Exporting .vrms (.zip)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            isProcessingBackup = true
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    viewModel.exportBackup(outputStream) { success ->
                        isProcessingBackup = false
                        if (success) {
                            Toast.makeText(context, "Full Backup exported successfully (.vrms)!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Failed to create backup.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                isProcessingBackup = false
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // SAF File Picker for Restoring .vrms (.zip)
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            isProcessingBackup = true
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    viewModel.restoreBackup(inputStream) { success ->
                        isProcessingBackup = false
                        if (success) {
                            Toast.makeText(context, "Backup restored successfully! Please restart the app if needed.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Failed to restore backup archive.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                isProcessingBackup = false
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            ThombreTopAppBar(
                title = "Settings & Maintenance",
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SurfaceBg)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Company Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ThombreRedDark)

                    Text(text = "Thombre Transport", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = ThombreRed)
                    Text(text = "Address: Baur Gaon, Taluka - Maval, Dist - Pune", fontSize = 13.sp, color = TextSecondary)
                    Text(text = "Proprietor: Mr. Thombre", fontSize = 13.sp, color = TextSecondary)
                    Text(text = "Starting Bill Sequence: 10001 (Monotonic)", fontSize = 13.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = panText,
                        onValueChange = { panText = it.uppercase() },
                        label = { Text("PAN Number (Editable)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            viewModel.updatePan(panText)
                            Toast.makeText(context, "PAN updated successfully", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ThombreRed)
                    ) {
                        Text("Save PAN")
                    }
                }
            }

            // Unified Backup & Restore Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Unified Backup & Recovery", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ThombreRedDark)
                    Text(
                        text = "Because this app operates 100% locally on your phone without internet servers, you can export a complete compressed .vrms archive (including SQLite database and all saved PDF invoices) to Google Drive, SD Card, or Internal Downloads.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    if (isProcessingBackup) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = ThombreRed, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Processing archive...", fontSize = 14.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.ENGLISH).format(Date())
                                val fileName = "ThombreTransport_Backup_$timestamp.vrms"
                                exportLauncher.launch(fileName)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ThombreRed)
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Full Backup (.vrms)")
                        }

                        OutlinedButton(
                            onClick = {
                                restoreLauncher.launch(arrayOf("application/zip", "application/octet-stream", "*/*"))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Restore from Backup Archive")
                        }
                    }
                }
            }
        }
    }
}
