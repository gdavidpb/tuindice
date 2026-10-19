package com.gdavidpb.tuindice.about.presentation.machine

import com.gdavidpb.tuindice.about.domain.usecase.DeleteAssistantConversationsUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantAvailabilityUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantConsentUseCase
import com.gdavidpb.tuindice.about.domain.usecase.RefreshAssistantConsentUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SetAssistantConsentUseCase

// The assistant use cases the about machine drives, grouped to keep the machine's constructor short.
class AboutAssistantUseCases(
	val observeAvailability: ObserveAssistantAvailabilityUseCase,
	val observeConsent: ObserveAssistantConsentUseCase,
	val setConsent: SetAssistantConsentUseCase,
	val refreshConsent: RefreshAssistantConsentUseCase,
	val deleteConversations: DeleteAssistantConversationsUseCase
)
