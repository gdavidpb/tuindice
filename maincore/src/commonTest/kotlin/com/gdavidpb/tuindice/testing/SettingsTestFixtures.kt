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
	val consentRepository = object : AssistantConsentRepository {
		private val consent = MutableStateFlow(false)

		override val assistantConsentGranted: StateFlow<Boolean> = consent.asStateFlow()

		override fun isAssistantConsentGranted(): Boolean = consent.value

		override suspend fun setAssistantConsentGranted(granted: Boolean) {
			consent.value = granted
		}

		override suspend fun refreshAssistantConsent() = Unit
	}
	val conversationsRepository = object : AssistantConversationsRepository {
		override suspend fun deleteAssistantConversations() = Unit
	}

	return SettingsViewModel(
		screenMachine = SettingsMachine(
			loadVersionUseCase = LoadVersionUseCase(
				aboutRepository = object : AboutRepository {
					override suspend fun getVersionDescription(): String = "1.0.0"
				},
				reportingRepository = reporting
			),
			observeProfileIdentityUseCase = ObserveProfileIdentityUseCase(
				profileIdentityRepository = profileIdentityRepository,
				reportingRepository = reporting
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
			assistantUseCases = SettingsAssistantUseCases(
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
					assistantConversationsRepository = conversationsRepository,
					reportingRepository = reporting
				)
			)
		),
		eventPublisher = NoOpEventPublisher
	)
}
