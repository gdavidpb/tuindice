package com.gdavidpb.tuindice.domain.repository

interface SettingsRepository {
    fun setFirstRun()
    fun setCooldown()
    fun isFirstRun(): Boolean
    fun isCooldown(): Boolean
    fun getPrivacyPolicyUrl(): String
    fun setPrivacyPolicyUrl(url: String)
    fun clearPrivacyPolicyUrl()
}