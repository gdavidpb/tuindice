package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.service.DstCredentials

interface SettingsRepository {
    fun startCountdown(reset: Boolean): Long
    fun resetCountdown()

    fun getEmail(): String
    fun hasCredentials(): Boolean
    fun getCredentials(): DstCredentials
    fun storeCredentials(credentials: DstCredentials)
    fun getCredentialYear(): Int
    fun isReviewSuggested(value: Int): Boolean

    fun getLastScreen(): Int
    fun setLastScreen(screen: Int)

    fun clear()
}