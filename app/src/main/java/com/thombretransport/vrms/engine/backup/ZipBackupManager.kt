package com.thombretransport.vrms.engine.backup

import android.content.Context
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ZipBackupManager(private val context: Context) {

    /**
     * Packages the Room database file and all saved PDF invoices into a unified .vrms (zip) stream.
     */
    fun exportBackup(outputStream: OutputStream): Boolean {
        return try {
            ZipOutputStream(BufferedOutputStream(outputStream)).use { zos ->
                // 1. Add Room Database file
                val dbFile = context.getDatabasePath("vrms_database.db")
                if (dbFile.exists()) {
                    addFileToZip(zos, dbFile, "database/vrms_database.db")
                }

                // Add WAL and SHM if present
                val walFile = File(dbFile.path + "-wal")
                if (walFile.exists()) addFileToZip(zos, walFile, "database/vrms_database.db-wal")

                val shmFile = File(dbFile.path + "-shm")
                if (shmFile.exists()) addFileToZip(zos, shmFile, "database/vrms_database.db-shm")

                // 2. Add PDF Invoices folder
                val billsDir = File(context.getExternalFilesDir(null), "Documents/bills")
                if (billsDir.exists() && billsDir.isDirectory) {
                    billsDir.listFiles()?.forEach { file ->
                        if (file.isFile && file.name.endsWith(".pdf", ignoreCase = true)) {
                            addFileToZip(zos, file, "bills/${file.name}")
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Restores database and PDF documents from a .vrms (zip) input stream.
     */
    fun restoreBackup(inputStream: InputStream): Boolean {
        return try {
            val dbFile = context.getDatabasePath("vrms_database.db")
            val dbDir = dbFile.parentFile ?: return false
            if (!dbDir.exists()) dbDir.mkdirs()

            val billsDir = File(context.getExternalFilesDir(null), "Documents/bills")
            if (!billsDir.exists()) billsDir.mkdirs()

            ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
                var entry = zis.nextEntry
                val buffer = ByteArray(4096)

                while (entry != null) {
                    val entryName = entry.name

                    val targetFile: File? = when {
                        entryName.startsWith("database/vrms_database.db-wal") -> File(dbFile.path + "-wal")
                        entryName.startsWith("database/vrms_database.db-shm") -> File(dbFile.path + "-shm")
                        entryName.startsWith("database/vrms_database.db") -> dbFile
                        entryName.startsWith("bills/") -> {
                            val fileName = entryName.removePrefix("bills/")
                            if (fileName.isNotBlank()) File(billsDir, fileName) else null
                        }
                        else -> null
                    }

                    if (targetFile != null && !entry.isDirectory) {
                        targetFile.parentFile?.mkdirs()
                        FileOutputStream(targetFile).use { fos ->
                            var count: Int
                            while (zis.read(buffer).also { count = it } != -1) {
                                fos.write(buffer, 0, count)
                            }
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun addFileToZip(zos: ZipOutputStream, file: File, zipPath: String) {
        FileInputStream(file).use { fis ->
            val entry = ZipEntry(zipPath)
            zos.putNextEntry(entry)
            val buffer = ByteArray(4096)
            var count: Int
            while (fis.read(buffer).also { count = it } != -1) {
                zos.write(buffer, 0, count)
            }
            zos.closeEntry()
        }
    }
}
