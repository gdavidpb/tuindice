package com.gdavidpb.tuindice.domain.repository

interface SettingsRepository {
    suspend fun setFirstRun()
    suspend fun setCooldown(key: String)
    suspend fun isCooldown(key: String): Boolean
    suspend fun isFirstRun(): Boolean
    suspend fun awaitingEmail(): String
    suspend fun awaitingPassword(): String
    suspend fun setIsAwaitingForReset(email: String, password: String)
    suspend fun setIsAwaitingForVerify(email: String)
    suspend fun isAwaitingForReset(): Boolean
    suspend fun isAwaitingForVerify(): Boolean
    suspend fun clearIsAwaitingForReset()
    suspend fun clearIsAwaitingForVerify()
    suspend fun getCountdown(): Long
    suspend fun startCountdown(): Long
    suspend fun clearCountdown()
    suspend fun clear()
}