package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.service.DstCredentials

interface SettingsRepository {
    fun awaitingEmail(): String
    fun awaitingPassword(): String

    fun setIsAwaitingForReset(email: String, password: String)
    fun isAwaitingForReset(): Boolean
    fun clearIsAwaitingForReset()

    fun startCountdown(reset: Boolean): Long

    fun getCredentials(): DstCredentials
    fun storeCredentials(credentials: DstCredentials)
    fun getCredentialYear(): Int

    fun getLastScreen(): Int
    fun setLastScreen(screen: Int)

    fun clear()
}