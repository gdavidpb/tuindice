package com.gdavidpb.tuindice.about.presentation.machine

import com.gdavidpb.tuindice.about.domain.usecase.AssistantConsentWriteQueue
import com.gdavidpb.tuindice.about.domain.usecase.DeleteAssistantConversationsUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantAvailabilityUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantConsentUseCase
import com.gdavidpb.tuindice.about.domain.usecase.RefreshAssistantConsentUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SetAssistantConsentUseCase

// The assistant use cases the settings machine drives, grouped to keep the machine's constructor short. The queue of
// the consent writes goes with them: the switch's writes are queued in the transition that moves it.
class SettingsAssistantUseCases(
	val observeAvailability: ObserveAssistantAvailabilityUseCase,
	val observeConsent: ObserveAssistantConsentUseCase,
	val setConsent: SetAssistantConsentUseCase,
	val refreshConsent: RefreshAssistantConsentUseCase,
	val deleteConversations: DeleteAssistantConversationsUseCase,
	val consentWrites: AssistantConsentWriteQueue
)
