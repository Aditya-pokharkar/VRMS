package com.thombretransport.vrms.engine.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.thombretransport.vrms.data.local.entity.AppConfigEntity
import com.thombretransport.vrms.data.local.entity.TripEntity
import com.thombretransport.vrms.domain.util.IndianNumberToWordsConverter
import com.thombretransport.vrms.domain.util.RegistrationNumberValidator
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat

class PdfInvoiceGenerator(private val context: Context) {

    private val df = DecimalFormat("#,##0.00")
    private val kmDf = DecimalFormat("#,##0.0")

    fun generateInvoicePdf(
        billNumber: Long,
        billDate: String,
        vehicleReg: String,
        vehicleType: String,
        clientName: String,
        billingPeriodText: String,
        trips: List<TripEntity>,
        config: AppConfigEntity
    ): File {
        val pdfDocument = PdfDocument()

        // A4 page dimensions at 72 DPI: 595 x 842 points
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val marginLeft = 36f
        val marginRight = 559f // 595 - 36
        val printableWidth = marginRight - marginLeft // 523f

        // Paints
        val redPaint = Paint().apply {
            color = Color.parseColor("#D32F2F")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 22f
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            isAntiAlias = true
        }

        val boldTextPaint = Paint().apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 10f
            isAntiAlias = true
        }

        val headerSubPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 9.5f
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val darkBorderPaint = Paint().apply {
            color = Color.DKGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val tableHeaderBgPaint = Paint().apply {
            color = Color.parseColor("#EEEEEE")
            style = Paint.Style.FILL
        }

        // --- 1. HEADER SECTION ---
        var currentY = 55f
        canvas.drawText(config.companyName.uppercase(), marginLeft, currentY, redPaint)

        currentY += 16f
        canvas.drawText(config.companyAddress, marginLeft, currentY, headerSubPaint)

        currentY += 16f
        val propText = "Proprietor: ${config.proprietorName}"
        val panText = "PAN: ${config.panNumber}"
        canvas.drawText(propText, marginLeft, currentY, boldTextPaint)
        canvas.drawText(panText, marginRight - boldTextPaint.measureText(panText), currentY, boldTextPaint)

        // Divider
        currentY += 10f
        canvas.drawLine(marginLeft, currentY, marginRight, currentY, darkBorderPaint)

        // --- 2. METADATA SECTION ---
        currentY += 18f
        val billNoText = "Bill No: $billNumber"
        val billDateText = "Bill Date: $billDate"
        canvas.drawText(billNoText, marginLeft, currentY, boldTextPaint)
        canvas.drawText(billDateText, marginRight - boldTextPaint.measureText(billDateText), currentY, textPaint)

        currentY += 16f
        val displayVehicle = RegistrationNumberValidator.formatForDisplay(vehicleReg)
        val vehicleText = "Vehicle No: $displayVehicle"
        val clientText = "To: $clientName"
        canvas.drawText(vehicleText, marginLeft, currentY, boldTextPaint)
        canvas.drawText(clientText, marginRight - textPaint.measureText(clientText).coerceAtMost(250f), currentY, boldTextPaint)

        // Divider
        currentY += 10f
        canvas.drawLine(marginLeft, currentY, marginRight, currentY, borderPaint)

        // --- 3. BILLING PERIOD LABEL ---
        currentY += 20f
        val periodTextPaint = Paint().apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 11f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        val periodLabel = if (billingPeriodText.isNotBlank()) {
            billingPeriodText
        } else if (trips.size == 1) {
            "Transport Bill For Date: ${trips.first().tripDate}"
        } else {
            val minDate = trips.minOfOrNull { it.tripDate } ?: ""
            val maxDate = trips.maxOfOrNull { it.tripDate } ?: ""
            "Transport Bill From $minDate to $maxDate"
        }
        canvas.drawText(periodLabel, pageWidth / 2f, currentY, periodTextPaint)

        // --- 4. TABLE SECTION ---
        currentY += 15f
        val tableTopY = currentY
        val isAc = vehicleType.equals("AC", ignoreCase = true)

        if (isAc) {
            currentY = drawAcTable(
                canvas, marginLeft, marginRight, currentY,
                trips, textPaint, boldTextPaint, borderPaint, darkBorderPaint, tableHeaderBgPaint
            )
        } else {
            currentY = drawNonAcTable(
                canvas, marginLeft, marginRight, currentY,
                trips, textPaint, boldTextPaint, borderPaint, darkBorderPaint, tableHeaderBgPaint
            )
        }

        pdfDocument.finishPage(page)

        // Write to local sandbox directory
        val docsDir = File(context.getExternalFilesDir(null), "Documents/bills")
        if (!docsDir.exists()) docsDir.mkdirs()

        val cleanReg = RegistrationNumberValidator.normalize(vehicleReg)
        val fileName = "Bill_${billNumber}_${cleanReg}_${System.currentTimeMillis()}.pdf"
        val pdfFile = File(docsDir, fileName)

        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }

    private fun drawAcTable(
        canvas: Canvas,
        startX: Float,
        endX: Float,
        startY: Float,
        trips: List<TripEntity>,
        textPaint: Paint,
        boldPaint: Paint,
        borderPaint: Paint,
        darkBorderPaint: Paint,
        headerBgPaint: Paint
    ): Float {
        // 6 Columns: Sr(35), Date(65), Trip(165), Invoice(85), Toll(75), Amount(98) = 523
        val colWidths = floatArrayOf(35f, 65f, 165f, 85f, 75f, 98f)
        val headers = arrayOf("Sr. No.", "Date", "Trip", "Invoice No.", "Escort/Toll", "Amount (₹)")

        val rowHeight = 22f
        var currentY = startY

        // Header Background
        canvas.drawRect(startX, currentY, endX, currentY + rowHeight, headerBgPaint)
        canvas.drawRect(startX, currentY, endX, currentY + rowHeight, darkBorderPaint)

        // Draw Headers
        var curX = startX
        for (i in headers.indices) {
            val cellX = curX + 4f
            val textY = currentY + 15f
            when (i) {
                0 -> canvas.drawText(headers[i], curX + colWidths[i] / 2f - boldPaint.measureText(headers[i]) / 2f, textY, boldPaint)
                1 -> canvas.drawText(headers[i], curX + colWidths[i] / 2f - boldPaint.measureText(headers[i]) / 2f, textY, boldPaint)
                2 -> canvas.drawText(headers[i], cellX, textY, boldPaint)
                3 -> canvas.drawText(headers[i], curX + colWidths[i] / 2f - boldPaint.measureText(headers[i]) / 2f, textY, boldPaint)
                4 -> canvas.drawText(headers[i], curX + colWidths[i] - boldPaint.measureText(headers[i]) - 4f, textY, boldPaint)
                5 -> canvas.drawText(headers[i], curX + colWidths[i] - boldPaint.measureText(headers[i]) - 4f, textY, boldPaint)
            }
            if (i > 0) canvas.drawLine(curX, currentY, curX, currentY + rowHeight, darkBorderPaint)
            curX += colWidths[i]
        }

        currentY += rowHeight

        var totalToll = 0.0
        var totalGross = 0.0

        // Data Rows
        trips.forEachIndexed { index, trip ->
            totalToll += trip.escortTollCharges
            totalGross += trip.baseAmount

            canvas.drawRect(startX, currentY, endX, currentY + rowHeight, borderPaint)

            var colX = startX
            val textY = currentY + 15f

            // 0: Sr No
            val srText = (index + 1).toString()
            canvas.drawText(srText, colX + colWidths[0] / 2f - textPaint.measureText(srText) / 2f, textY, textPaint)
            colX += colWidths[0]

            // 1: Date
            canvas.drawText(trip.tripDate, colX + colWidths[1] / 2f - textPaint.measureText(trip.tripDate) / 2f, textY, textPaint)
            colX += colWidths[1]

            // 2: Trip [Source] TO [Destination]
            val tripRoute = "${trip.sourceLocation} TO ${trip.destinationLocation}".take(28)
            canvas.drawText(tripRoute, colX + 4f, textY, textPaint)
            colX += colWidths[2]

            // 3: Invoice No
            val inv = trip.clientInvoiceRef
            canvas.drawText(inv, colX + colWidths[3] / 2f - textPaint.measureText(inv) / 2f, textY, textPaint)
            colX += colWidths[3]

            // 4: Escort / Toll
            val tollStr = df.format(trip.escortTollCharges)
            canvas.drawText(tollStr, colX + colWidths[4] - textPaint.measureText(tollStr) - 4f, textY, textPaint)
            colX += colWidths[4]

            // 5: Amount
            val amtStr = df.format(trip.baseAmount)
            canvas.drawText(amtStr, colX + colWidths[5] - textPaint.measureText(amtStr) - 4f, textY, textPaint)

            currentY += rowHeight
        }

        // Summary Rows
        val finalPayable = totalGross + totalToll // Rationale: Tolls reimbursed by client

        currentY += 4f
        canvas.drawRect(startX, currentY, endX, currentY + 24f, headerBgPaint)
        canvas.drawRect(startX, currentY, endX, currentY + 24f, darkBorderPaint)

        val summaryText1 = "Total Toll: ₹ ${df.format(totalToll)}    |    Total Gross: ₹ ${df.format(totalGross)}"
        canvas.drawText(summaryText1, startX + 10f, currentY + 16f, boldPaint)

        val payableText = "Final Payable Amount: ₹ ${df.format(finalPayable)}"
        canvas.drawText(payableText, endX - boldPaint.measureText(payableText) - 10f, currentY + 16f, boldPaint)

        currentY += 34f
        val wordsText = "Amount in Words: ${IndianNumberToWordsConverter.convert(finalPayable)}"
        canvas.drawText(wordsText, startX, currentY, boldPaint)

        return currentY + 20f
    }

    private fun drawNonAcTable(
        canvas: Canvas,
        startX: Float,
        endX: Float,
        startY: Float,
        trips: List<TripEntity>,
        textPaint: Paint,
        boldPaint: Paint,
        borderPaint: Paint,
        darkBorderPaint: Paint,
        headerBgPaint: Paint
    ): Float {
        // 7 Columns: Sr(35), Date(65), KM(55), Rate(50), OrigAmt(95), Disc%(65), NetAmt(158) = 523
        val colWidths = floatArrayOf(35f, 65f, 55f, 50f, 95f, 65f, 158f)
        val headers = arrayOf("Sr. No.", "Date", "KM", "Rate", "Orig. Amt (₹)", "Disc %", "Net Amount (₹)")

        val rowHeight = 22f
        var currentY = startY

        // Header Background
        canvas.drawRect(startX, currentY, endX, currentY + rowHeight, headerBgPaint)
        canvas.drawRect(startX, currentY, endX, currentY + rowHeight, darkBorderPaint)

        // Draw Headers
        var curX = startX
        for (i in headers.indices) {
            val textY = currentY + 15f
            when (i) {
                0 -> canvas.drawText(headers[i], curX + colWidths[i] / 2f - boldPaint.measureText(headers[i]) / 2f, textY, boldPaint)
                1 -> canvas.drawText(headers[i], curX + colWidths[i] / 2f - boldPaint.measureText(headers[i]) / 2f, textY, boldPaint)
                2, 3, 4 -> canvas.drawText(headers[i], curX + colWidths[i] - boldPaint.measureText(headers[i]) - 4f, textY, boldPaint)
                5 -> canvas.drawText(headers[i], curX + colWidths[i] / 2f - boldPaint.measureText(headers[i]) / 2f, textY, boldPaint)
                6 -> canvas.drawText(headers[i], curX + colWidths[i] - boldPaint.measureText(headers[i]) - 4f, textY, boldPaint)
            }
            if (i > 0) canvas.drawLine(curX, currentY, curX, currentY + rowHeight, darkBorderPaint)
            curX += colWidths[i]
        }

        currentY += rowHeight

        var totalKm = 0.0
        var totalOriginal = 0.0
        var totalNet = 0.0

        // Data Rows
        trips.forEachIndexed { index, trip ->
            totalKm += trip.kmTraveled
            totalOriginal += trip.originalAmount
            totalNet += trip.finalAmount

            canvas.drawRect(startX, currentY, endX, currentY + rowHeight, borderPaint)

            var colX = startX
            val textY = currentY + 15f

            // 0: Sr No
            val srText = (index + 1).toString()
            canvas.drawText(srText, colX + colWidths[0] / 2f - textPaint.measureText(srText) / 2f, textY, textPaint)
            colX += colWidths[0]

            // 1: Date
            canvas.drawText(trip.tripDate, colX + colWidths[1] / 2f - textPaint.measureText(trip.tripDate) / 2f, textY, textPaint)
            colX += colWidths[1]

            // 2: KM
            val kmStr = kmDf.format(trip.kmTraveled)
            canvas.drawText(kmStr, colX + colWidths[2] - textPaint.measureText(kmStr) - 4f, textY, textPaint)
            colX += colWidths[2]

            // 3: Rate
            val rateStr = df.format(trip.ratePerKm)
            canvas.drawText(rateStr, colX + colWidths[3] - textPaint.measureText(rateStr) - 4f, textY, textPaint)
            colX += colWidths[3]

            // 4: Original Amount
            val origStr = df.format(trip.originalAmount)
            canvas.drawText(origStr, colX + colWidths[4] - textPaint.measureText(origStr) - 4f, textY, textPaint)
            colX += colWidths[4]

            // 5: Disc % (Explicitly print 0% if no discount)
            val discStr = if (trip.discountPercentage > 0) "${df.format(trip.discountPercentage)}%" else "0%"
            canvas.drawText(discStr, colX + colWidths[5] / 2f - textPaint.measureText(discStr) / 2f, textY, textPaint)
            colX += colWidths[5]

            // 6: Net Amount
            val netStr = df.format(trip.finalAmount)
            canvas.drawText(netStr, colX + colWidths[6] - textPaint.measureText(netStr) - 4f, textY, textPaint)

            currentY += rowHeight
        }

        val totalDiscount = totalOriginal - totalNet

        // Summary Rows
        currentY += 4f
        canvas.drawRect(startX, currentY, endX, currentY + 24f, headerBgPaint)
        canvas.drawRect(startX, currentY, endX, currentY + 24f, darkBorderPaint)

        val summaryText = "Total KM: ${kmDf.format(totalKm)}   |   Gross: ₹ ${df.format(totalOriginal)}   |   Discount: ₹ ${df.format(totalDiscount)}"
        canvas.drawText(summaryText, startX + 8f, currentY + 16f, boldPaint)

        val netSummaryText = "Final Net: ₹ ${df.format(totalNet)}"
        canvas.drawText(netSummaryText, endX - boldPaint.measureText(netSummaryText) - 8f, currentY + 16f, boldPaint)

        currentY += 34f
        val wordsText = "Amount in Words: ${IndianNumberToWordsConverter.convert(totalNet)}"
        canvas.drawText(wordsText, startX, currentY, boldPaint)

        return currentY + 20f
    }
}
