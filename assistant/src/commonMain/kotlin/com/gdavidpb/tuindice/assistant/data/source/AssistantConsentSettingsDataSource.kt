package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.utils.ASSISTANT_CONSENT_VERSION
import com.gdavidpb.tuindice.base.domain.repository.AssistantConsentRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.chatcontract.consent.ConsentDecision
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// The consent the server accepted, kept on the device so the assistant reads it without a request.
class AssistantConsentSettingsDataSource(
	private val settings: Settings,
	private val api: KtorAssistantApiDataSource
) : AssistantConsentRepository, SessionMemory {

	private val consent = MutableStateFlow(readStoredConsent())

	override val assistantConsentGranted: StateFlow<Boolean> = consent.asStateFlow()

	override fun isAssistantConsentGranted(): Boolean = consent.value

	// The server decides first. If it refuses, the stored consent and the observed one stay as they were.
	override suspend fun setAssistantConsentGranted(granted: Boolean) {
		val status = api.putConsent(ConsentDecision(version = ASSISTANT_CONSENT_VERSION, accepted = granted))

		settings.putBoolean(CONSENT_GRANTED_KEY, status.accepted)
		consent.value = status.accepted
	}

	// The stored consent goes with the rest of the data. The mirror follows it, so the assistant stops with the session.
	override suspend fun clearSessionMemory() {
		consent.value = readStoredConsent()
	}

	private fun readStoredConsent(): Boolean {
		return settings.getBooleanOrNull(CONSENT_GRANTED_KEY) ?: false
	}

	private companion object {
		const val CONSENT_GRANTED_KEY = "assistantConsentGranted"
	}
}
