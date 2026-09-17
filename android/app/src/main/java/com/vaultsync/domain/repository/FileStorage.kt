package com.vaultsync.domain.repository

interface FileStorage {
    suspend fun readFile(path: String): ByteArray
    suspend fun writeFile(path: String, data: ByteArray): String
    suspend fun deleteFile(path: String): Boolean
    suspend fun fileExists(path: String): Boolean
    suspend fun getFileSize(path: String): Long
    suspend fun copyFile(sourcePath: String, destinationPath: String): String
}
