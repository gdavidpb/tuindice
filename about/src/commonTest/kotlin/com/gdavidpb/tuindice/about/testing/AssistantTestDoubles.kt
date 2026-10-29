package com.gdavidpb.tuindice.about.testing

import com.gdavidpb.tuindice.about.domain.usecase.AssistantConsentWriteQueue
import com.gdavidpb.tuindice.about.domain.usecase.DeleteAssistantConversationsUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantAvailabilityUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantConsentUseCase
import com.gdavidpb.tuindice.about.domain.usecase.RefreshAssistantConsentUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SetAssistantConsentUseCase
import com.gdavidpb.tuindice.about.presentation.machine.SettingsAssistantUseCases
import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantAvailability
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import com.gdavidpb.tuindice.base.domain.repository.AssistantAvailabilityRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantConversationsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAssistantAvailabilityRepository(
	visible: Boolean = true
) : AssistantAvailabilityRepository {
	private val availability = MutableStateFlow(
		AssistantAvailability(
			mode = if (visible) AssistantMode.ON else AssistantMode.OFF,
			serverDisabled = false
		)
	)

	override fun observeAvailability(): Flow<AssistantAvailability> = availability

	override fun currentAvailability(): AssistantAvailability = availability.value

	// The configured mode or the server switch changes while the screen is up.
	fun publish(next: AssistantAvailability) {
		availability.value = next
	}
}

class FakeAssistantConsentRepository(
	granted: Boolean = false,
	private val writesFail: Boolean = false,
	private val serverGranted: Boolean? = null,
	private val refreshFails: Boolean = false,
	// Holds every write until the test releases it, so the write is still in flight while the test acts.
	private val writeGate: CompletableDeferred<Unit>? = null,
	// With writeGate: holds only the first write, so the writes after it can end while the first one is still in flight.
	private val holdsOnlyTheFirstWrite: Boolean = false
) : AssistantConsentRepository {
	var refreshes = 0
		private set

	// The writes that reached the store: a write cut short before it does not count.
	var writes = 0
		private set

	// The values the writes stored, in the order the store received them.
	val storedValues = mutableListOf<Boolean>()

	private var writeCalls = 0

	private val consent = MutableStateFlow(granted)

	override val assistantConsentGranted: StateFlow<Boolean> = consent.asStateFlow()

	override fun isAssistantConsentGranted(): Boolean = consent.value

	override suspend fun setAssistantConsentGranted(granted: Boolean) {
		val held = !holdsOnlyTheFirstWrite || writeCalls == 0
		writeCalls++

		if (held) writeGate?.await()

		check(!writesFail) { "The assistant consent store is unavailable" }

		consent.value = granted
		storedValues += granted
		writes++
	}

	// The store changes while a write is in flight, as it does when another path writes it (the server's answer, say).
	fun changeStore(granted: Boolean) {
		consent.value = granted
	}

	override suspend fun refreshAssistantConsent() {
		refreshes++

		check(!refreshFails) { "The assistant consent is unavailable" }

		serverGranted?.let { consent.value = it }
	}
}

class FakeAssistantConversationsRepository(
	private val deletesFail: Boolean = false,
	// Holds the deletion, the request to the server, until the test releases it.
	private val deleteGate: CompletableDeferred<Unit>? = null
) : AssistantConversationsRepository {
	var deletions = 0
		private set

	override suspend fun deleteAssistantConversations() {
		deleteGate?.await()

		check(!deletesFail) { "The assistant conversations are unavailable" }

		deletions++
	}
}

/** Assistant use cases over fresh fakes, for builders whose tests do not exercise the assistant. */
fun fakeObserveAssistantAvailabilityUseCase(): ObserveAssistantAvailabilityUseCase {
	return ObserveAssistantAvailabilityUseCase(
		assistantAvailabilityRepository = FakeAssistantAvailabilityRepository(),
		reportingRepository = RecordingReportingRepository()
	)
}

fun fakeObserveAssistantConsentUseCase(): ObserveAssistantConsentUseCase {
	return ObserveAssistantConsentUseCase(
		assistantConsentRepository = FakeAssistantConsentRepository(),
		reportingRepository = RecordingReportingRepository()
	)
}

fun fakeSetAssistantConsentUseCase(
	assistantConsentRepository: AssistantConsentRepository = FakeAssistantConsentRepository(),
	sessionCoroutineScope: SessionCoroutineScope = testSessionCoroutineScope()
): SetAssistantConsentUseCase {
	return SetAssistantConsentUseCase(
		assistantConsentRepository = assistantConsentRepository,
		sessionCoroutineScope = sessionCoroutineScope,
		reportingRepository = RecordingReportingRepository()
	)
}

fun fakeAssistantConsentWriteQueue(
	sessionCoroutineScope: SessionCoroutineScope = testSessionCoroutineScope()
): AssistantConsentWriteQueue {
	return AssistantConsentWriteQueue(sessionCoroutineScope = sessionCoroutineScope)
}

fun fakeRefreshAssistantConsentUseCase(
	assistantConsentRepository: AssistantConsentRepository = FakeAssistantConsentRepository()
): RefreshAssistantConsentUseCase {
	return RefreshAssistantConsentUseCase(
		assistantConsentRepository = assistantConsentRepository,
		reportingRepository = RecordingReportingRepository()
	)
}

/** The assistant use cases of the settings machine over fresh fakes. */
fun fakeSettingsAssistantUseCases(): SettingsAssistantUseCases {
	return SettingsAssistantUseCases(
		observeAvailability = fakeObserveAssistantAvailabilityUseCase(),
		observeConsent = fakeObserveAssistantConsentUseCase(),
		setConsent = fakeSetAssistantConsentUseCase(),
		refreshConsent = fakeRefreshAssistantConsentUseCase(),
		deleteConversations = fakeDeleteAssistantConversationsUseCase(),
		consentWrites = fakeAssistantConsentWriteQueue()
	)
}

fun fakeDeleteAssistantConversationsUseCase(
	assistantConversationsRepository: AssistantConversationsRepository = FakeAssistantConversationsRepository(),
	sessionCoroutineScope: SessionCoroutineScope = testSessionCoroutineScope()
): DeleteAssistantConversationsUseCase {
	return DeleteAssistantConversationsUseCase(
		assistantConversationsRepository = assistantConversationsRepository,
		sessionCoroutineScope = sessionCoroutineScope,
		reportingRepository = RecordingReportingRepository()
	)
}
