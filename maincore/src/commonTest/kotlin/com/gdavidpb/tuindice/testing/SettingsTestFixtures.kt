package com.gdavidpb.tuindice.testing

import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.about.domain.repository.StoreUrlRepository
import com.gdavidpb.tuindice.about.domain.usecase.DeleteAssistantConversationsUseCase
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantAvailabilityUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantConsentUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveProfileIdentityUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.domain.usecase.RefreshAssistantConsentUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SetAssistantConsentUseCase
import com.gdavidpb.tuindice.about.presentation.machine.SettingsAssistantUseCases
import com.gdavidpb.tuindice.about.presentation.machine.SettingsContentUseCases
import com.gdavidpb.tuindice.about.presentation.machine.SettingsExternalUseCases
import com.gdavidpb.tuindice.about.presentation.machine.SettingsMachine
import com.gdavidpb.tuindice.about.presentation.viewmodel.SettingsViewModel
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantAvailabilityRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantConversationsRepository
import com.gdavidpb.tuindice.base.domain.repository.ProfileIdentityRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingBrowserRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// The Settings screen of the app host, over fakes: the shell opens it from the avatar and signs out from it.
fun createSettingsViewModel(
	profileIdentityRepository: ProfileIdentityRepository = FakeProfileIdentityRepository(),
	assistantAvailabilityRepository: AssistantAvailabilityRepository = FakeAssistantAvailabilityRepository()
): SettingsViewModel {
	val reporting = RecordingReportingRepository()
	return SettingsViewModel(
		screenMachine = SettingsMachine(
			contentUseCases = SettingsContentUseCases(
				loadVersion = LoadVersionUseCase(
					aboutRepository = object : AboutRepository {
						override suspend fun getVersionDescription(): String = "1.0.0"
					},
					reportingRepository = reporting
				),
				observeProfileIdentity = ObserveProfileIdentityUseCase(
					profileIdentityRepository = profileIdentityRepository,
					reportingRepository = reporting
				)
			),
			appEnvironmentRepository = FakeAppEnvironmentRepository(),
			usageDataConsentRepository = InMemoryUsageDataConsentRepository(),
			externalUseCases = SettingsExternalUseCases(
				sendSupportEmail = SendSupportEmailUseCase(
					configRepository = FakeConfigRepository(),
					deviceInfoRepository = FakeDeviceInfoRepository(),
					reportingRepository = reporting
				),
				openStore = OpenStoreUseCase(
					storeUrlRepository = object : StoreUrlRepository {
						override fun getStoreUrl(): String = "market://details?id=com.gdavidpb.tuindice"
					},
					reportingRepository = reporting
				),
				openExternalUrl = OpenExternalUrlUseCase(
					browserRepository = RecordingBrowserRepository(),
					reportingRepository = reporting
				)
			),
			assistantUseCases = assistantUseCases(reporting, assistantAvailabilityRepository)
		),
		eventPublisher = NoOpEventPublisher
	)
}

private fun assistantUseCases(
	reporting: RecordingReportingRepository,
	assistantAvailabilityRepository: AssistantAvailabilityRepository
): SettingsAssistantUseCases {
	val consentRepository = InMemoryAssistantConsentRepository()

	return SettingsAssistantUseCases(
		observeAvailability = ObserveAssistantAvailabilityUseCase(
			assistantAvailabilityRepository = assistantAvailabilityRepository,
			reportingRepository = reporting
		),
		observeConsent = ObserveAssistantConsentUseCase(
			assistantConsentRepository = consentRepository,
			reportingRepository = reporting
		),
		setConsent = SetAssistantConsentUseCase(
			assistantConsentRepository = consentRepository,
			reportingRepository = reporting
		),
		refreshConsent = RefreshAssistantConsentUseCase(
			assistantConsentRepository = consentRepository,
			reportingRepository = reporting
		),
		deleteConversations = DeleteAssistantConversationsUseCase(
			assistantConversationsRepository = NoOpAssistantConversationsRepository(),
			reportingRepository = reporting
		)
	)
}

private class InMemoryAssistantConsentRepository : AssistantConsentRepository {
	private val consent = MutableStateFlow(false)

	override val assistantConsentGranted: StateFlow<Boolean> = consent.asStateFlow()

	override fun isAssistantConsentGranted(): Boolean = consent.value

	override suspend fun setAssistantConsentGranted(granted: Boolean) {
		consent.value = granted
	}

	override suspend fun refreshAssistantConsent() = Unit
}

private class NoOpAssistantConversationsRepository : AssistantConversationsRepository {
	override suspend fun deleteAssistantConversations() = Unit
}
