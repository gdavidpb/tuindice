package com.gdavidpb.tuindice.base.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface AssistantConsentRepository {
	val assistantConsentGranted: StateFlow<Boolean>

	fun isAssistantConsentGranted(): Boolean

	suspend fun setAssistantConsentGranted(granted: Boolean)

	// Reads the consent the server holds and keeps it, for a device that has not heard it yet.
	suspend fun refreshAssistantConsent()
}
