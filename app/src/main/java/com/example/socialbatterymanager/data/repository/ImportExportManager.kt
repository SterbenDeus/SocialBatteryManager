package com.example.socialbatterymanager.data.repository

import android.content.Context
import com.example.socialbatterymanager.data.model.ActivityEntity
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Singleton
class ImportExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityRepository: ActivityRepository
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    suspend fun exportToCsv(): File? {
        return try {
            val activities = activityRepository.getAllActivities().first()
            val fileName = "social_battery_export_${System.currentTimeMillis()}.csv"
            val file = File(context.getExternalFilesDir(null), fileName)

            CSVWriter(FileWriter(file)).use { writer ->
                writer.writeNext(
                    arrayOf(
                        "ID", "Name", "Type", "Energy", "People",
                        "Mood", "Notes", "Date", "Last Modified", "Updated At"
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
                            dateFormat.format(Date(activity.date)),
                            dateFormat.format(Date(activity.lastModified)),
                            dateFormat.format(Date(activity.updatedAt))
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
            activityRepository.getAllActivities().first()
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
            var contentStream = PDPageContentStream(document, page)

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
            contentStream.showText("Exported on: ${dateFormat.format(Date())}")
            contentStream.endText()
            yPosition -= 24f

            // Table Header
            val headers = listOf("ID", "Name", "Type", "Energy", "People", "Mood", "Date")
            yPosition = writeRow(contentStream, margin, yPosition, headers, true)

            // Table Rows
            for (activity in activities) {
                if (yPosition < margin) {
                    contentStream.close()
                    val newPage = PDPage(PDRectangle.A4)
                    document.addPage(newPage)
                    contentStream = PDPageContentStream(document, newPage)
                    yPosition = yStart
                    yPosition = writeRow(contentStream, margin, yPosition, headers, true)
                }
                val row = listOf(
                    activity.id.toString(),
                    activity.name,
                    activity.type,
                    activity.energy.toString(),
                    activity.people,
                    activity.mood,
                    dateFormat.format(Date(activity.date))
                )
                yPosition = writeRow(contentStream, margin, yPosition, row)
            }

            contentStream.close()
            document.save(FileOutputStream(file))
            document.close()
            return file
        } catch (e: IOException) {
            try {
                contentStream.close()
            } catch (_: Exception) {
            }
            document.close()
            return null
        }
    }

    private fun writeRow(
        contentStream: PDPageContentStream,
        margin: Float,
        yPosition: Float,
        values: List<String>,
        isHeader: Boolean = false
    ): Float {
        contentStream.beginText()
        val font = if (isHeader) PDType1Font.HELVETICA_BOLD else PDType1Font.HELVETICA
        val fontSize = if (isHeader) 10f else 9f
        contentStream.setFont(font, fontSize)
        contentStream.newLineAtOffset(margin, yPosition)
        contentStream.showText(values.joinToString("    "))
        contentStream.endText()
        return yPosition - if (isHeader) 16f else 14f
    }

    suspend fun importFromCsv(file: File): ImportResult {
        return try {
            var successCount = 0
            var errorCount = 0

            file.bufferedReader().use { reader ->
                CSVReader(reader).use { csvReader ->
                    val headers = csvReader.readNext()?.map { it.trim() }
                    if (headers == null || headers.size < 7) {
                        return ImportResult(0, 0, "Invalid or missing headers")
                    }

                    var line = csvReader.readNext()
                    while (line != null) {
                        try {
                            if (line.size >= 7) {
                                val activity = ActivityEntity(
                                    name = line[1].trim(),
                                    type = line[2].trim(),
                                    energy = line[3].trim().toIntOrNull() ?: 0,
                                    people = line[4].trim(),
                                    mood = line[5].trim(),
                                    notes = line[6].trim(),
                                    date = System.currentTimeMillis(),
                                    lastModified = System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                                activityRepository.insertActivity(activity, "import")
                                successCount++
                            } else {
                                errorCount++
                            }
                        } catch (e: Exception) {
                            errorCount++
                        }
                        line = csvReader.readNext()
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

}
