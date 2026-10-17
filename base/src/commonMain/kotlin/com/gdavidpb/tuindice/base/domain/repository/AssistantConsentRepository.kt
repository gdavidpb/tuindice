package com.gdavidpb.tuindice.base.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface AssistantConsentRepository {
	val assistantConsentGranted: StateFlow<Boolean>

	fun isAssistantConsentGranted(): Boolean

	suspend fun setAssistantConsentGranted(granted: Boolean)
}
