package com.gdavidpb.tuindice.about.testing

import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantAvailabilityUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantConsentUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SetAssistantConsentUseCase
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantAvailability
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import com.gdavidpb.tuindice.base.domain.repository.AssistantAvailabilityRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantConsentRepository
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
	private val writesFail: Boolean = false
) : AssistantConsentRepository {
	private val consent = MutableStateFlow(granted)

	override val assistantConsentGranted: StateFlow<Boolean> = consent.asStateFlow()

	override fun isAssistantConsentGranted(): Boolean = consent.value

	override suspend fun setAssistantConsentGranted(granted: Boolean) {
		check(!writesFail) { "The assistant consent store is unavailable" }

		consent.value = granted
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

fun fakeSetAssistantConsentUseCase(): SetAssistantConsentUseCase {
	return SetAssistantConsentUseCase(
		assistantConsentRepository = FakeAssistantConsentRepository(),
		reportingRepository = RecordingReportingRepository()
	)
}
