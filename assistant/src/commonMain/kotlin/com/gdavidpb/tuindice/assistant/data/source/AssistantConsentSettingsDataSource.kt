package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantServerConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantConsentRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.chatcontract.consent.ConsentDecision
import com.gdavidpb.tuindice.chatcontract.consent.ConsentStatus
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// The consent the server accepted, kept on the device so the assistant reads it without a request.
class AssistantConsentSettingsDataSource(
	private val settings: Settings,
	private val api: KtorAssistantApiDataSource
) : AssistantConsentRepository, AssistantServerConsentRepository, SessionMemory {

	private val consent = MutableStateFlow(readStoredConsent())

	override val assistantConsentGranted: StateFlow<Boolean> = consent.asStateFlow()

	override fun isAssistantConsentGranted(): Boolean = consent.value

	// The server decides first, on the version it holds now: the answer is for the text the server shows. If it refuses,
	// the stored consent and the observed one stay as they were.
	override suspend fun setAssistantConsentGranted(granted: Boolean) {
		val version = api.getConsent().version
		val status = api.putConsent(ConsentDecision(version = version, accepted = granted))

		store(status)
	}

	// A reinstall, a new device and a sign-out all leave the device without the consent the server still holds; this
	// reads it for the screens that show the consent before the chat has been opened.
	override suspend fun refreshAssistantConsent() {
		store(api.getConsent())
	}

	// The consent the server reports is the one the device keeps, whatever the device held before.
	override fun reconcileConsent(status: ConsentStatus) {
		store(status)
	}

	private fun store(status: ConsentStatus) {
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
