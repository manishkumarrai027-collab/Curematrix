package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.LabResultRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.Patient
import com.example.data.model.Prescription
import com.example.data.model.TreatmentPlan
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data bundle required to compile a patient's printable medical report.
 */
data class PatientMedicalReportData(
    val patient: Patient,
    val medicalRecords: List<MedicalRecord> = emptyList(),
    val prescriptions: List<Prescription> = emptyList(),
    val treatmentPlans: List<TreatmentPlan> = emptyList(),
    val labResults: List<LabResultRecord> = emptyList(),
    val generatedByStaff: String = "Dr. Elena Rostova",
    val generatedDate: String = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date())
)

/**
 * High-quality PDF document generator and print/share utility
 * for Patient Medical History and Current Prescriptions.
 */
object PdfReportGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard width in points
    private const val PAGE_HEIGHT = 842 // A4 standard height in points
    private const val MARGIN_X = 36f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN_X * 2)

    private const val COLOR_PRIMARY = 0xFF1A5276.toInt() // Deep medical navy
    private const val COLOR_SECONDARY = 0xFF2E86C1.toInt() // Clinical blue
    private const val COLOR_TEXT_DARK = 0xFF212529.toInt() // Dark charcoal
    private const val COLOR_TEXT_MUTED = 0xFF5D6D7E.toInt() // Slate grey
    private const val COLOR_BG_LIGHT = 0xFFF4F6F7.toInt() // Light background
    private const val COLOR_BORDER = 0xFFD5DBDB.toInt() // Border light
    private const val COLOR_ALERT_RED = 0xFFC0392B.toInt() // Alert red
    private const val COLOR_SUCCESS_GREEN = 0xFF27AE60.toInt() // Success green

    /**
     * Generates a multi-page printable PDF file containing the patient's medical history,
     * current vital signs, active prescriptions, clinical notes, treatment plans, and lab tests.
     */
    fun generatePatientReportPdf(context: Context, data: PatientMedicalReportData): File {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }
        val safeMrn = data.patient.mrn.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val outputFile = File(reportsDir, "Medical_Report_${safeMrn}.pdf")

        val document = PdfDocument()

        val pages = mutableListOf<PdfDocument.Page>()
        var currentPageNumber = 1
        var currentPage = document.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
        )
        pages.add(currentPage)
        var canvas = currentPage.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Helper to handle multi-page overflow
        var yPos = 40f

        fun checkAndCreateNewPage(neededHeight: Float) {
            if (yPos + neededHeight > PAGE_HEIGHT - 50f) {
                // Finish current page
                document.finishPage(currentPage)
                currentPageNumber++
                currentPage = document.startPage(
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
                )
                pages.add(currentPage)
                canvas = currentPage.canvas
                yPos = 40f
                // Draw running header on subsequent pages
                drawRunningHeader(canvas, paint, data.patient, currentPageNumber)
                yPos += 30f
            }
        }

        // 1. HOSPITAL HEADER
        drawHospitalHeader(canvas, paint, data)
        yPos = 115f

        // 2. PATIENT DEMOGRAPHICS & VITALS CARD
        drawPatientDemographics(canvas, paint, data.patient, yPos)
        yPos += 135f

        // 3. PRIMARY MEDICAL HISTORY & ALLERGIES
        drawPrimaryMedicalHistory(canvas, paint, data.patient, yPos)
        yPos += 75f

        // 4. CURRENT PRESCRIPTIONS TABLE
        checkAndCreateNewPage(60f)
        yPos = drawPrescriptionsSection(canvas, paint, data.prescriptions, yPos, ::checkAndCreateNewPage) {
            canvas
        }

        // 5. CLINICAL MEDICAL HISTORY & DIAGNOSES NOTES
        checkAndCreateNewPage(60f)
        yPos = drawClinicalRecordsSection(canvas, paint, data.medicalRecords, yPos, ::checkAndCreateNewPage) {
            canvas
        }

        // 6. ACTIVE TREATMENT PLANS
        if (data.treatmentPlans.isNotEmpty()) {
            checkAndCreateNewPage(60f)
            yPos = drawTreatmentPlansSection(canvas, paint, data.treatmentPlans, yPos, ::checkAndCreateNewPage) {
                canvas
            }
        }

        // 7. LAB RESULTS & DIAGNOSTICS
        if (data.labResults.isNotEmpty()) {
            checkAndCreateNewPage(60f)
            yPos = drawLabResultsSection(canvas, paint, data.labResults, yPos, ::checkAndCreateNewPage) {
                canvas
            }
        }

        // 8. ATTESTATION & SIGN-OFF BLOCK
        checkAndCreateNewPage(70f)
        drawAttestationBlock(canvas, paint, data, yPos)

        // Finish last page
        document.finishPage(currentPage)

        // Draw footers with total page count on each page
        // (Note: in standard PdfDocument we finish pages in sequence. Footer was already written or we stamp total)

        // Write to output file
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(outputFile)
            document.writeTo(fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            fos?.close()
            document.close()
        }

        return outputFile
    }

    private fun drawHospitalHeader(canvas: Canvas, paint: Paint, data: PatientMedicalReportData) {
        // Top banner background
        paint.color = COLOR_PRIMARY
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 95f, paint)

        // Decorative medical cross icon
        paint.color = Color.WHITE
        val crossX = MARGIN_X + 16f
        val crossY = 48f
        val crossRadius = 14f
        paint.style = Paint.Style.FILL
        canvas.drawCircle(crossX, crossY, crossRadius, paint)
        paint.color = COLOR_PRIMARY
        paint.strokeWidth = 3f
        canvas.drawLine(crossX - 7f, crossY, crossX + 7f, crossY, paint)
        canvas.drawLine(crossX, crossY - 7f, crossX, crossY + 7f, paint)

        // Hospital Title
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 15f
        canvas.drawText("CARE REGIONAL MEDICAL CENTER & SPECIALTY HOSPITAL", MARGIN_X + 38f, 38f, paint)

        // Subtitle
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9.5f
        paint.color = 0xFFD4E6F1.toInt()
        canvas.drawText("Official Patient Medical History & Current Prescriptions Report", MARGIN_X + 38f, 53f, paint)
        canvas.drawText("100 Healthcare Boulevard, Suite 500 • Tel: +1 (555) 019-4800 • EMR System", MARGIN_X + 38f, 66f, paint)

        // Timestamp & Report Badge on Right
        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.WHITE
        paint.textSize = 8.5f
        canvas.drawText("Report Generated: ${data.generatedDate}", PAGE_WIDTH - MARGIN_X, 36f, paint)
        canvas.drawText("Attending: ${data.generatedByStaff}", PAGE_WIDTH - MARGIN_X, 49f, paint)
        paint.color = 0xFFFFD700.toInt() // Gold tag
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CONFIDENTIAL MEDICAL RECORD", PAGE_WIDTH - MARGIN_X, 64f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun drawRunningHeader(canvas: Canvas, paint: Paint, patient: Patient, pageNumber: Int) {
        paint.color = COLOR_PRIMARY
        paint.style = Paint.Style.FILL
        canvas.drawRect(MARGIN_X, 20f, PAGE_WIDTH - MARGIN_X, 22f, paint)

        paint.color = COLOR_TEXT_MUTED
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Care Hospital EMR • Patient: ${patient.fullName} (${patient.mrn})", MARGIN_X, 32f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Page $pageNumber", PAGE_WIDTH - MARGIN_X, 32f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun drawPatientDemographics(canvas: Canvas, paint: Paint, patient: Patient, top: Float) {
        val cardHeight = 120f
        val rect = RectF(MARGIN_X, top, PAGE_WIDTH - MARGIN_X, top + cardHeight)

        // Card background & border
        paint.color = COLOR_BG_LIGHT
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(rect, 8f, 8f, paint)

        paint.color = COLOR_BORDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, 8f, 8f, paint)

        // Header strip
        val headerRect = RectF(MARGIN_X, top, PAGE_WIDTH - MARGIN_X, top + 26f)
        paint.color = COLOR_SECONDARY
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(headerRect, 8f, 8f, paint)
        // Square bottom of header
        canvas.drawRect(MARGIN_X, top + 15f, PAGE_WIDTH - MARGIN_X, top + 26f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("1. PATIENT DEMOGRAPHICS & CLINICAL VITALS", MARGIN_X + 12f, top + 17f, paint)

        // Status chip on right
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("STATUS: ${patient.status.uppercase()}", PAGE_WIDTH - MARGIN_X - 12f, top + 17f, paint)
        paint.textAlign = Paint.Align.LEFT

        // Content layout (3 Columns)
        val col1X = MARGIN_X + 14f
        val col2X = MARGIN_X + 185f
        val col3X = MARGIN_X + 355f

        var lineY = top + 42f
        val lineSpacing = 16f

        fun drawField(colX: Float, y: Float, label: String, value: String) {
            paint.style = Paint.Style.FILL
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8.5f
            paint.color = COLOR_TEXT_MUTED
            canvas.drawText("$label:", colX, y, paint)

            val labelWidth = paint.measureText("$label: ")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = COLOR_TEXT_DARK
            canvas.drawText(value, colX + labelWidth, y, paint)
        }

        // Row 1
        drawField(col1X, lineY, "Full Name", patient.fullName)
        drawField(col2X, lineY, "MRN", patient.mrn)
        drawField(col3X, lineY, "Blood Group", patient.bloodGroup)

        // Row 2
        lineY += lineSpacing
        drawField(col1X, lineY, "Age / Gender", "${patient.age} Yrs / ${patient.gender}")
        drawField(col2X, lineY, "Date of Birth", patient.dateOfBirth.ifBlank { "Not Specified" })
        drawField(col3X, lineY, "Assigned Bed", "${patient.assignedWard ?: "N/A"} • ${patient.assignedBedNumber ?: "N/A"}")

        // Row 3
        lineY += lineSpacing
        drawField(col1X, lineY, "Primary Phone", patient.contactPhone)
        drawField(col2X, lineY, "Doctor", patient.assignedDoctorName ?: "General Attending")
        drawField(col3X, lineY, "Blood Pressure", patient.bloodPressure)

        // Row 4
        lineY += lineSpacing
        drawField(col1X, lineY, "Emergency Contact", patient.emergencyContact.take(24).ifBlank { "N/A" })
        drawField(col2X, lineY, "Pulse / SpO2", "${patient.pulseRate} bpm / ${patient.oxygenLevel}%")
        drawField(col3X, lineY, "Temperature", "${patient.temperature} °F")
    }

    private fun drawPrimaryMedicalHistory(canvas: Canvas, paint: Paint, patient: Patient, top: Float) {
        val cardHeight = 65f
        val rect = RectF(MARGIN_X, top, PAGE_WIDTH - MARGIN_X, top + cardHeight)

        paint.color = COLOR_BG_LIGHT
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(rect, 6f, 6f, paint)
        paint.color = COLOR_BORDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, 6f, 6f, paint)

        // Section Title
        paint.style = Paint.Style.FILL
        paint.color = COLOR_PRIMARY
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("2. PRIMARY MEDICAL HISTORY & KNOWN ALLERGIES", MARGIN_X + 12f, top + 16f, paint)

        // Allergies alert badge
        val allergies = patient.allergies.ifBlank { "None Known" }
        paint.color = if (allergies.equals("None Known", ignoreCase = true)) COLOR_SUCCESS_GREEN else COLOR_ALERT_RED
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("ALLERGIES: $allergies", PAGE_WIDTH - MARGIN_X - 12f, top + 16f, paint)
        paint.textAlign = Paint.Align.LEFT

        // Primary History details
        paint.color = COLOR_TEXT_DARK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f

        val historyText = "Past Medical History / Conditions: ${patient.primaryMedicalHistory.ifBlank { "No prior chronic medical conditions reported." }}"
        val diagnosisText = "Active Diagnosis: ${patient.diagnosis.ifBlank { "Under clinical observation" }}"
        val dailyMedsText = "Baseline Daily Medications: ${patient.currentMedications.ifBlank { "None reported" }}"

        canvas.drawText(historyText.take(110), MARGIN_X + 12f, top + 31f, paint)
        canvas.drawText(diagnosisText.take(110), MARGIN_X + 12f, top + 44f, paint)
        canvas.drawText(dailyMedsText.take(110), MARGIN_X + 12f, top + 57f, paint)
    }

    private fun drawPrescriptionsSection(
        initialCanvas: Canvas,
        paint: Paint,
        prescriptions: List<Prescription>,
        startTop: Float,
        checkNewPage: (Float) -> Unit,
        getCanvas: () -> Canvas
    ): Float {
        var canvas = initialCanvas
        var currentY = startTop

        // Header Title
        paint.style = Paint.Style.FILL
        paint.color = COLOR_PRIMARY
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("3. CURRENT PRESCRIPTIONS & PHARMACOTHERAPY (${prescriptions.size} Items)", MARGIN_X, currentY + 12f, paint)
        currentY += 18f

        // Table Header
        paint.color = COLOR_SECONDARY
        canvas.drawRect(MARGIN_X, currentY, PAGE_WIDTH - MARGIN_X, currentY + 18f, paint)

        paint.color = Color.WHITE
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val colMed = MARGIN_X + 8f
        val colDose = MARGIN_X + 140f
        val colFreq = MARGIN_X + 225f
        val colDur = MARGIN_X + 310f
        val colDoc = MARGIN_X + 375f
        val colInst = MARGIN_X + 445f

        canvas.drawText("MEDICATION", colMed, currentY + 12f, paint)
        canvas.drawText("DOSAGE", colDose, currentY + 12f, paint)
        canvas.drawText("FREQUENCY", colFreq, currentY + 12f, paint)
        canvas.drawText("DURATION", colDur, currentY + 12f, paint)
        canvas.drawText("PRESCRIBER", colDoc, currentY + 12f, paint)
        canvas.drawText("INSTRUCTIONS", colInst, currentY + 12f, paint)

        currentY += 18f

        if (prescriptions.isEmpty()) {
            paint.color = COLOR_BG_LIGHT
            canvas.drawRect(MARGIN_X, currentY, PAGE_WIDTH - MARGIN_X, currentY + 22f, paint)
            paint.color = COLOR_TEXT_MUTED
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText("No active prescription records found for this patient.", MARGIN_X + 12f, currentY + 14f, paint)
            currentY += 26f
            return currentY
        }

        var isEven = false
        prescriptions.forEach { rx ->
            checkNewPage(24f)
            canvas = getCanvas()

            val rowHeight = 22f
            paint.color = if (isEven) COLOR_BG_LIGHT else Color.WHITE
            canvas.drawRect(MARGIN_X, currentY, PAGE_WIDTH - MARGIN_X, currentY + rowHeight, paint)

            paint.color = COLOR_BORDER
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.5f
            canvas.drawLine(MARGIN_X, currentY + rowHeight, PAGE_WIDTH - MARGIN_X, currentY + rowHeight, paint)

            paint.style = Paint.Style.FILL
            paint.color = COLOR_TEXT_DARK
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            val medName = if (rx.medicationName.isNotBlank()) rx.medicationName else rx.medicines.take(24)
            canvas.drawText(medName.take(22), colMed, currentY + 14f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(rx.dosage.take(14).ifBlank { "Standard" }, colDose, currentY + 14f, paint)
            canvas.drawText(rx.frequency.take(16).ifBlank { "Daily" }, colFreq, currentY + 14f, paint)
            canvas.drawText(rx.duration.take(12).ifBlank { "Ongoing" }, colDur, currentY + 14f, paint)
            canvas.drawText(rx.doctorName.take(12).ifBlank { "Attending" }, colDoc, currentY + 14f, paint)
            canvas.drawText(rx.instructions.take(20).ifBlank { "As directed" }, colInst, currentY + 14f, paint)

            currentY += rowHeight
            isEven = !isEven
        }

        currentY += 10f
        return currentY
    }

    private fun drawClinicalRecordsSection(
        initialCanvas: Canvas,
        paint: Paint,
        records: List<MedicalRecord>,
        startTop: Float,
        checkNewPage: (Float) -> Unit,
        getCanvas: () -> Canvas
    ): Float {
        var canvas = initialCanvas
        var currentY = startTop

        paint.style = Paint.Style.FILL
        paint.color = COLOR_PRIMARY
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("4. CLINICAL MEDICAL HISTORY & DIAGNOSES NOTES (${records.size} Records)", MARGIN_X, currentY + 12f, paint)
        currentY += 20f

        if (records.isEmpty()) {
            paint.color = COLOR_BG_LIGHT
            canvas.drawRect(MARGIN_X, currentY, PAGE_WIDTH - MARGIN_X, currentY + 22f, paint)
            paint.color = COLOR_TEXT_MUTED
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText("No progress notes or diagnostic records recorded.", MARGIN_X + 12f, currentY + 14f, paint)
            currentY += 26f
            return currentY
        }

        records.take(6).forEach { rec ->
            checkNewPage(42f)
            canvas = getCanvas()

            val boxHeight = 38f
            val rect = RectF(MARGIN_X, currentY, PAGE_WIDTH - MARGIN_X, currentY + boxHeight)

            paint.color = COLOR_BG_LIGHT
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(rect, 4f, 4f, paint)

            paint.color = COLOR_BORDER
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.5f
            canvas.drawRoundRect(rect, 4f, 4f, paint)

            // Header line
            paint.style = Paint.Style.FILL
            paint.color = COLOR_PRIMARY
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(rec.dateMillis))
            canvas.drawText("${rec.recordType.uppercase()} • ${rec.title.take(45)}", MARGIN_X + 8f, currentY + 13f, paint)

            paint.color = COLOR_TEXT_MUTED
            paint.textSize = 8f
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("$dateStr | ${rec.doctorOrStaffName} (${rec.department})", PAGE_WIDTH - MARGIN_X - 8f, currentY + 13f, paint)
            paint.textAlign = Paint.Align.LEFT

            // Content note
            paint.color = COLOR_TEXT_DARK
            paint.textSize = 7.8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(rec.content.take(115), MARGIN_X + 8f, currentY + 28f, paint)

            currentY += boxHeight + 6f
        }

        currentY += 6f
        return currentY
    }

    private fun drawTreatmentPlansSection(
        initialCanvas: Canvas,
        paint: Paint,
        plans: List<TreatmentPlan>,
        startTop: Float,
        checkNewPage: (Float) -> Unit,
        getCanvas: () -> Canvas
    ): Float {
        var canvas = initialCanvas
        var currentY = startTop

        paint.style = Paint.Style.FILL
        paint.color = COLOR_PRIMARY
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("5. ACTIVE TREATMENT PLANS & REHABILITATION GOALS (${plans.size})", MARGIN_X, currentY + 12f, paint)
        currentY += 20f

        plans.take(3).forEach { plan ->
            checkNewPage(42f)
            canvas = getCanvas()

            val boxHeight = 36f
            val rect = RectF(MARGIN_X, currentY, PAGE_WIDTH - MARGIN_X, currentY + boxHeight)

            paint.color = COLOR_BG_LIGHT
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(rect, 4f, 4f, paint)

            paint.style = Paint.Style.FILL
            paint.color = COLOR_PRIMARY
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("${plan.planTitle} • Attending: ${plan.attendingDoctor}", MARGIN_X + 8f, currentY + 13f, paint)

            paint.color = COLOR_TEXT_DARK
            paint.textSize = 7.8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Goals: ${plan.clinicalGoals.take(110)}", MARGIN_X + 8f, currentY + 24f, paint)
            canvas.drawText("Interventions: ${plan.activeInterventions.take(110)}", MARGIN_X + 8f, currentY + 33f, paint)

            currentY += boxHeight + 6f
        }

        currentY += 6f
        return currentY
    }

    private fun drawLabResultsSection(
        initialCanvas: Canvas,
        paint: Paint,
        labs: List<LabResultRecord>,
        startTop: Float,
        checkNewPage: (Float) -> Unit,
        getCanvas: () -> Canvas
    ): Float {
        var canvas = initialCanvas
        var currentY = startTop

        paint.style = Paint.Style.FILL
        paint.color = COLOR_PRIMARY
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("6. DIAGNOSTIC & LABORATORY FINDINGS (${labs.size} Reports)", MARGIN_X, currentY + 12f, paint)
        currentY += 20f

        labs.take(3).forEach { lab ->
            checkNewPage(38f)
            canvas = getCanvas()

            val boxHeight = 32f
            val rect = RectF(MARGIN_X, currentY, PAGE_WIDTH - MARGIN_X, currentY + boxHeight)

            paint.color = COLOR_BG_LIGHT
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(rect, 4f, 4f, paint)

            paint.style = Paint.Style.FILL
            paint.color = COLOR_PRIMARY
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("${lab.testName} (${lab.category})", MARGIN_X + 8f, currentY + 13f, paint)

            paint.textAlign = Paint.Align.RIGHT
            paint.color = if (lab.status.contains("Critical", ignoreCase = true)) COLOR_ALERT_RED else COLOR_SUCCESS_GREEN
            canvas.drawText("STATUS: ${lab.status.uppercase()}", PAGE_WIDTH - MARGIN_X - 8f, currentY + 13f, paint)
            paint.textAlign = Paint.Align.LEFT

            paint.color = COLOR_TEXT_DARK
            paint.textSize = 7.8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Finding: ${lab.conclusion.take(110)}", MARGIN_X + 8f, currentY + 25f, paint)

            currentY += boxHeight + 6f
        }

        currentY += 6f
        return currentY
    }

    private fun drawAttestationBlock(canvas: Canvas, paint: Paint, data: PatientMedicalReportData, top: Float) {
        val blockHeight = 65f
        val rect = RectF(MARGIN_X, top, PAGE_WIDTH - MARGIN_X, top + blockHeight)

        paint.color = 0xFFEBF5FB.toInt() // Soft medical blue
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(rect, 6f, 6f, paint)

        paint.color = COLOR_SECONDARY
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, 6f, 6f, paint)

        paint.style = Paint.Style.FILL
        paint.color = COLOR_PRIMARY
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PHYSICIAN CERTIFICATION & CLINICAL ATTESTATION", MARGIN_X + 12f, top + 15f, paint)

        paint.color = COLOR_TEXT_DARK
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(
            "I hereby certify that this clinical summary and prescription record accurately reflects the patient's verified medical record",
            MARGIN_X + 12f,
            top + 28f,
            paint
        )
        canvas.drawText(
            "in Care Regional Hospital EMR as of the date indicated. Medications must be dispensed strictly per licensed doctor orders.",
            MARGIN_X + 12f,
            top + 39f,
            paint
        )

        // Signature line on right
        paint.color = COLOR_TEXT_DARK
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Authorized Signature: ____________________________", PAGE_WIDTH - MARGIN_X - 12f, top + 54f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    /**
     * Prints the PDF document using Android's native PrintManager service.
     */
    fun printPdfReport(context: Context, pdfFile: File, jobName: String = "Patient_Medical_Report") {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                Toast.makeText(context, "Print service not available on this device", Toast.LENGTH_SHORT).show()
                return
            }

            val printAdapter = object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }

                    val info = PrintDocumentInfo.Builder(jobName)
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                        .build()
                    callback?.onLayoutFinished(info, newAttributes != oldAttributes)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor?,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    var input: FileInputStream? = null
                    var output: FileOutputStream? = null

                    try {
                        input = FileInputStream(pdfFile)
                        output = FileOutputStream(destination?.fileDescriptor)

                        val buf = ByteArray(1024 * 8)
                        var bytesRead: Int
                        while (input.read(buf).also { bytesRead = it } > 0) {
                            if (cancellationSignal?.isCanceled == true) {
                                callback?.onWriteCancelled()
                                return
                            }
                            output.write(buf, 0, bytesRead)
                        }
                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    } finally {
                        try {
                            input?.close()
                            output?.close()
                        } catch (e: IOException) {
                            // ignore
                        }
                    }
                }
            }

            val printAttributes = PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .build()

            printManager.print(jobName, printAdapter, printAttributes)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to launch printer: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Shares the generated PDF file using standard Android Intent.ACTION_SEND
     * with zero broad storage permissions via FileProvider.
     */
    fun sharePdfReport(context: Context, pdfFile: File, title: String = "Patient Medical Report") {
        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Attached is the clinical medical report and prescription summary for your records.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Medical Report PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to share PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Opens the generated PDF in any external PDF viewer app installed on device.
     */
    fun viewPdfReport(context: Context, pdfFile: File) {
        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(viewIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "No PDF viewer app found. You can print or share instead.", Toast.LENGTH_LONG).show()
        }
    }
}
