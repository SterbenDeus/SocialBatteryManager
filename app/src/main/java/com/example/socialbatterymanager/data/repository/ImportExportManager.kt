package com.example.socialbatterymanager.data.repository

import android.content.Context
import com.example.socialbatterymanager.data.model.ActivityEntity
import com.opencsv.CSVWriter
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImportExportManager private constructor(
    private val context: Context,
    private val dataRepository: DataRepository
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    suspend fun exportToCsv(): File? {
        return try {
            val activities = dataRepository.getAllActivities().first()
            val fileName = "social_battery_export_${System.currentTimeMillis()}.csv"
            val file = File(context.getExternalFilesDir(null), fileName)

            CSVWriter(FileWriter(file)).use { writer ->
                writer.writeNext(
                    arrayOf(
                        "ID", "Name", "Type", "Energy", "People",
                        "Mood", "Notes", "Date", "Created At", "Updated At"
                    )
                )
                activities.forEach { activity ->
                    writer.writeNext(
                        arrayOf(
                            activity.id.toString(),
                            activity.name,
                            activity.type,
                            activity.energy.toString(),
                            activity.people,
                            activity.mood,
                            activity.notes,
                            dateFormat.format(java.util.Date(activity.date)),
                            dateFormat.format(java.util.Date(activity.createdAt)),
                            dateFormat.format(java.util.Date(activity.updatedAt))
                        )
                    )
                }
            }
            file
        } catch (e: IOException) {
            null
        }
    }

    suspend fun exportToPdf(): File? {
        val activities = try {
            dataRepository.getAllActivities().first()
        } catch (e: Exception) {
            return null
        }
        val fileName = "social_battery_export_${System.currentTimeMillis()}.pdf"
        val file = File(context.getExternalFilesDir(null), fileName)

        val document = PDDocument()
        val page = PDPage(PDRectangle.A4)
        document.addPage(page)

        try {
            val margin = 40f
            val yStart = page.mediaBox.height - margin
            var yPosition = yStart
            val contentStream = PDPageContentStream(document, page)

            // Title
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16f)
            contentStream.newLineAtOffset(margin, yPosition)
            contentStream.showText("Social Battery Manager - Activity Export")
            contentStream.endText()
            yPosition -= 30f

            // Export Date
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA, 10f)
            contentStream.newLineAtOffset(margin, yPosition)
            contentStream.showText("Exported on: ${dateFormat.format(java.util.Date())}")
            contentStream.endText()
            yPosition -= 24f

            // Table Header
            val headers = listOf("ID", "Name", "Type", "Energy", "People", "Mood", "Date")
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10f)
            contentStream.newLineAtOffset(margin, yPosition)
            contentStream.showText(headers.joinToString("    "))
            contentStream.endText()
            yPosition -= 16f

            // Table Rows
            contentStream.setFont(PDType1Font.HELVETICA, 9f)
            for (activity in activities) {
                if (yPosition < margin + 20) break // Simple single-page support
                contentStream.beginText()
                contentStream.newLineAtOffset(margin, yPosition)
                contentStream.showText(
                    listOf(
                        activity.id.toString(),
                        activity.name,
                        activity.type,
                        activity.energy.toString(),
                        activity.people,
                        activity.mood,
                        dateFormat.format(java.util.Date(activity.date))
                    ).joinToString("    ")
                )
                contentStream.endText()
                yPosition -= 14f
            }

            contentStream.close()
            document.save(FileOutputStream(file))
            document.close()
            return file
        } catch (e: IOException) {
            document.close()
            return null
        }
    }

    suspend fun importFromCsv(file: File): ImportResult {
        return try {
            val activities = mutableListOf<ActivityEntity>()
            var successCount = 0
            var errorCount = 0

            file.bufferedReader().use { reader ->
                val lines = reader.readLines()
                if (lines.isEmpty()) {
                    return ImportResult(0, 0, "File is empty")
                }
                for (i in 1 until lines.size) {
                    try {
                        val parts = lines[i].split(",")
                        if (parts.size >= 7) {
                            val activity = ActivityEntity(
                                name = parts[1].trim('"'),
                                type = parts[2].trim('"'),
                                energy = parts[3].trim('"').toIntOrNull() ?: 0,
                                people = parts[4].trim('"'),
                                mood = parts[5].trim('"'),
                                notes = parts[6].trim('"'),
                                date = System.currentTimeMillis(),
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            dataRepository.insertActivity(activity, "import")
                            successCount++
                        } else {
                            errorCount++
                        }
                    } catch (e: Exception) {
                        errorCount++
                    }
                }
            }
            ImportResult(successCount, errorCount, "Import completed")
        } catch (e: IOException) {
            ImportResult(0, 0, "Error reading file: ${e.message}")
        }
    }

    suspend fun getExportFormats(): List<String> {
        return listOf("CSV", "PDF")
    }

    data class ImportResult(
        val successCount: Int,
        val errorCount: Int,
        val message: String
    )

    companion object {
        @Volatile
        private var INSTANCE: ImportExportManager? = null

        fun getInstance(context: Context, dataRepository: DataRepository): ImportExportManager {
            return INSTANCE ?: synchronized(this) {
                val instance = ImportExportManager(context, dataRepository)
                INSTANCE = instance
                instance
            }
        }
    }
}
