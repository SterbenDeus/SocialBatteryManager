package com.example.socialbatterymanager.data.repository

import android.content.Context
import com.example.socialbatterymanager.data.model.ActivityEntity
import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.opencsv.CSVWriter
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
                // Write header
                writer.writeNext(arrayOf(
                    "ID",
                    "Name",
                    "Type",
                    "Energy",
                    "People",
                    "Mood",
                    "Notes",
                    "Date",
                    "Created At",
                    "Updated At"
                ))
                
                // Write data
                activities.forEach { activity ->
                    writer.writeNext(arrayOf(
                        activity.id.toString(),
                        activity.name,
                        activity.type,
                        activity.energy.toString(),
                        activity.people,
                        activity.mood,
                        activity.notes,
                        dateFormat.format(Date(activity.date)),
                        dateFormat.format(Date(activity.createdAt)),
                        dateFormat.format(Date(activity.updatedAt))
                    ))
                }
            }
            
            file
        } catch (e: IOException) {
            null
        }
    }
    
    suspend fun exportToPdf(): File? {
        return try {
            val activities = dataRepository.getAllActivities().first()
            val fileName = "social_battery_export_${System.currentTimeMillis()}.pdf"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            val document = Document(PageSize.A4)
            PdfWriter.getInstance(document, FileOutputStream(file))
            
            document.open()
            
            // Title
            val titleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD)
            val title = Paragraph("Social Battery Manager - Activity Export", titleFont)
            title.alignment = Element.ALIGN_CENTER
            document.add(title)
            
            // Export date
            val dateFont = Font(Font.FontFamily.HELVETICA, 10f)
            val exportDate = Paragraph("Exported on: ${dateFormat.format(Date())}", dateFont)
            exportDate.alignment = Element.ALIGN_CENTER
            document.add(exportDate)
            document.add(Paragraph(" ")) // Empty line
            
            // Table
            val table = PdfPTable(7)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(1f, 3f, 2f, 1f, 2f, 2f, 3f))
            
            // Table headers
            val headerFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)
            table.addCell(createCell("ID", headerFont))
            table.addCell(createCell("Name", headerFont))
            table.addCell(createCell("Type", headerFont))
            table.addCell(createCell("Energy", headerFont))
            table.addCell(createCell("People", headerFont))
            table.addCell(createCell("Mood", headerFont))
            table.addCell(createCell("Date", headerFont))
            
            // Table data
            val dataFont = Font(Font.FontFamily.HELVETICA, 9f)
            activities.forEach { activity ->
                table.addCell(createCell(activity.id.toString(), dataFont))
                table.addCell(createCell(activity.name, dataFont))
                table.addCell(createCell(activity.type, dataFont))
                table.addCell(createCell(activity.energy.toString(), dataFont))
                table.addCell(createCell(activity.people, dataFont))
                table.addCell(createCell(activity.mood, dataFont))
                table.addCell(createCell(dateFormat.format(Date(activity.date)), dataFont))
            }
            
            document.add(table)
            document.close()
            
            file
        } catch (e: DocumentException) {
            null
        } catch (e: IOException) {
            null
        }
    }
    
    private fun createCell(content: String, font: Font): com.itextpdf.text.pdf.PdfPCell {
        val cell = com.itextpdf.text.pdf.PdfPCell(Paragraph(content, font))
        cell.setPadding(4f)
        return cell
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
                
                // Skip header
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
                                date = System.currentTimeMillis(), // Use current time for imported data
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