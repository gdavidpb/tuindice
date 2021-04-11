package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Credentials

interface SettingsRepository {
    fun startCountdown(reset: Boolean): Long
    fun resetCountdown()

    fun getEmail(): String
    fun hasCredentials(): Boolean
    fun getCredentials(): Credentials
    fun storeCredentials(credentials: Credentials)
    fun getCredentialYear(): Int
    fun isReviewSuggested(value: Int): Boolean

    fun getLastScreen(): Int
    fun setLastScreen(screen: Int)

    fun clear()
}