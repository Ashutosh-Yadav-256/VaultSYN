package com.vaultsync.domain.model

data class IntegrityResult(
    val documentId: String,
    val isValid: Boolean,
    val expectedHash: String,
    val actualHash: String,
    val checkedAt: Long = System.currentTimeMillis()
)
