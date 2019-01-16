package com.gdavidpb.tuindice.domain.repository

interface SettingsRepository {
    suspend fun getEmailSentTo(): String
    suspend fun setEmailSentTo(email: String)
    suspend fun clearEmailSentTo()
    suspend fun setFirstRun()
    suspend fun setCooldown(key: String)
    suspend fun isCooldown(key: String): Boolean
    suspend fun isFirstRun(): Boolean
    suspend fun clear()
}