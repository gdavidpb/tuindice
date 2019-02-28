package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.service.DstCredentials
import java.util.*

interface SettingsRepository {
    suspend fun getLastSync(): Date
    suspend fun setSyncCooldown()
    suspend fun isSyncCooldown(): Boolean

    suspend fun awaitingEmail(): String
    suspend fun awaitingPassword(): String

    suspend fun setIsAwaitingForReset(email: String, password: String)
    suspend fun isAwaitingForReset(): Boolean
    suspend fun clearIsAwaitingForReset()

    suspend fun getCountdown(): Long
    suspend fun startCountdown(): Long
    suspend fun clearCountdown()

    suspend fun getCredentials(): DstCredentials
    suspend fun storeCredentials(credentials: DstCredentials)

    suspend fun clear()
}