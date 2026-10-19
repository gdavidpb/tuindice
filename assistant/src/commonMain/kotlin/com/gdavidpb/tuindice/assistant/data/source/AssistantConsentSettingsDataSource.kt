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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.Volatile

// The consent the server accepted, kept on the device so the assistant reads it without a request.
class AssistantConsentSettingsDataSource(
	private val settings: Settings,
	private val api: KtorAssistantApiDataSource
) : AssistantConsentRepository, AssistantServerConsentRepository, SessionMemory {

	private val consent = MutableStateFlow(readStoredConsent())

	// Held ONLY while the answer of a request is written locally (the settings and the mirror), never during a
	// request, and taken by clearSessionMemory to change the epoch. A write therefore ends before the session does
	// (and the wipe that follows removes it) or finds the new epoch and writes nothing.
	private val writeGate = Mutex()

	// Bumped by clearSessionMemory. A request remembers the one it started in: if it differs when the answer arrives,
	// the account that asked is gone and the answer must leave no trace.
	@Volatile
	private var sessionEpoch: Int = 0

	override val assistantConsentGranted: StateFlow<Boolean> = consent.asStateFlow()

	override fun isAssistantConsentGranted(): Boolean = consent.value

	// The server decides first, on the version it holds now: the answer is for the text the server shows. If it refuses,
	// the stored consent and the observed one stay as they were. If the session ends while the server answers, the
	// answer belongs to the account that is leaving and is not kept.
	override suspend fun setAssistantConsentGranted(granted: Boolean) {
		val epoch = sessionEpoch
		val version = api.getConsent().version
		val status = api.putConsent(ConsentDecision(version = version, accepted = granted))

		storeIfSameSession(epoch, status)
	}

	// A reinstall, a new device and a sign-out all leave the device without the consent the server still holds; this
	// reads it for the screens that show the consent before the chat has been opened.
	override suspend fun refreshAssistantConsent() {
		val epoch = sessionEpoch
		val status = api.getConsent()

		storeIfSameSession(epoch, status)
	}

	// The session mark is taken before the status is requested: a status that lands after the session ended is dropped.
	override fun consentSessionMark(): Int = sessionEpoch

	// The consent the server reports is the one the device keeps, whatever the device held before.
	override suspend fun reconcileConsent(status: ConsentStatus, consentSessionMark: Int) {
		storeIfSameSession(consentSessionMark, status)
	}

	private suspend fun storeIfSameSession(epoch: Int, status: ConsentStatus) {
		writeGate.withLock {
			if (epoch == sessionEpoch) store(status)
		}
	}

	private fun store(status: ConsentStatus) {
		settings.putBoolean(CONSENT_GRANTED_KEY, status.accepted)
		consent.value = status.accepted
	}

	// The stored consent goes with the rest of the data. The mirror follows it, so the assistant stops with the session.
	// Under writeGate: a request that lands after this point finds the new epoch and writes nothing.
	override suspend fun clearSessionMemory() {
		writeGate.withLock {
			sessionEpoch += 1
			consent.value = readStoredConsent()
		}
	}

	private fun readStoredConsent(): Boolean {
		return settings.getBooleanOrNull(CONSENT_GRANTED_KEY) ?: false
	}

	private companion object {
		const val CONSENT_GRANTED_KEY = "assistantConsentGranted"
	}
}
