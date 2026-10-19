package com.gdavidpb.tuindice.about.testing

import com.gdavidpb.tuindice.about.domain.usecase.DeleteAssistantConversationsUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantAvailabilityUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantConsentUseCase
import com.gdavidpb.tuindice.about.domain.usecase.RefreshAssistantConsentUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SetAssistantConsentUseCase
import com.gdavidpb.tuindice.about.presentation.machine.SettingsAssistantUseCases
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantAvailability
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import com.gdavidpb.tuindice.base.domain.repository.AssistantAvailabilityRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantConversationsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
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
}

class FakeAssistantConsentRepository(
	granted: Boolean = false,
	private val writesFail: Boolean = false,
	private val serverGranted: Boolean? = null,
	private val refreshFails: Boolean = false
) : AssistantConsentRepository {
	var refreshes = 0
		private set

	private val consent = MutableStateFlow(granted)

	override val assistantConsentGranted: StateFlow<Boolean> = consent.asStateFlow()

	override fun isAssistantConsentGranted(): Boolean = consent.value

	override suspend fun setAssistantConsentGranted(granted: Boolean) {
		check(!writesFail) { "The assistant consent store is unavailable" }

		consent.value = granted
	}

	override suspend fun refreshAssistantConsent() {
		refreshes++

		check(!refreshFails) { "The assistant consent is unavailable" }

		serverGranted?.let { consent.value = it }
	}
}

class FakeAssistantConversationsRepository(
	private val deletesFail: Boolean = false
) : AssistantConversationsRepository {
	var deletions = 0
		private set

	override suspend fun deleteAssistantConversations() {
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
	assistantConsentRepository: AssistantConsentRepository = FakeAssistantConsentRepository()
): SetAssistantConsentUseCase {
	return SetAssistantConsentUseCase(
		assistantConsentRepository = assistantConsentRepository,
		reportingRepository = RecordingReportingRepository()
	)
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
		deleteConversations = fakeDeleteAssistantConversationsUseCase()
	)
}

fun fakeDeleteAssistantConversationsUseCase(
	assistantConversationsRepository: AssistantConversationsRepository = FakeAssistantConversationsRepository()
): DeleteAssistantConversationsUseCase {
	return DeleteAssistantConversationsUseCase(
		assistantConversationsRepository = assistantConversationsRepository,
		reportingRepository = RecordingReportingRepository()
	)
}
