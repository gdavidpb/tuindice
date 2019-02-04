package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.data.model.service.DstCredentials

interface SettingsRepository {
    suspend fun setCooldown(key: String)
    suspend fun isCooldown(key: String): Boolean

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

    suspend fun getCredentials(): DstCredentials
    suspend fun storeCredentials(credentials: DstCredentials)

    suspend fun clear()
}