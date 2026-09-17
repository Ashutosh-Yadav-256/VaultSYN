package com.vaultsync.data.filesystem

import com.vaultsync.domain.model.AppError
import com.vaultsync.domain.repository.FileStorage
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalFileStorage(
    private val rootDir: File
) : FileStorage {

    init {
        if (!rootDir.exists()) {
            rootDir.mkdirs()
        }
    }

    override suspend fun readFile(path: String): ByteArray = withContext(Dispatchers.IO) {
        val file = resolveFile(path)
        if (!file.exists()) {
            throw AppError.StorageError("File not found at: ${file.absolutePath}")
        }
        file.readBytes()
    }

    override suspend fun writeFile(path: String, data: ByteArray): String = withContext(Dispatchers.IO) {
        val targetFile = resolveFile(path)
        val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp")

        try {
            FileOutputStream(tempFile).use { it.write(data) }
            if (targetFile.exists()) {
                targetFile.delete()
            }
            if (!tempFile.renameTo(targetFile)) {

                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }
            targetFile.name
        } catch (e: Exception) {
            tempFile.delete()
            throw AppError.StorageError("Failed writing file to $path: ${e.message}", e)
        }
    }

    override suspend fun deleteFile(path: String): Boolean = withContext(Dispatchers.IO) {
        val file = resolveFile(path)
        if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    override suspend fun fileExists(path: String): Boolean = withContext(Dispatchers.IO) {
        resolveFile(path).exists()
    }

    override suspend fun getFileSize(path: String): Long = withContext(Dispatchers.IO) {
        val file = resolveFile(path)
        if (file.exists()) file.length() else 0L
    }

    override suspend fun copyFile(sourcePath: String, destinationPath: String): String = withContext(Dispatchers.IO) {
        val sourceFile = resolveFile(sourcePath)
        val destFile = resolveFile(destinationPath)
        if (!sourceFile.exists()) {
            throw AppError.StorageError("Source file does not exist: ${sourceFile.absolutePath}")
        }
        sourceFile.copyTo(destFile, overwrite = true)
        destFile.name
    }

    private fun resolveFile(path: String): File {
        return if (path.startsWith(rootDir.absolutePath)) {
            File(path)
        } else {
            File(rootDir, path)
        }
    }
}
