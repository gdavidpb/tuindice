package com.gdavidpb.tuindice.about.presentation.machine

import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.presentation.contract.Settings
import com.gdavidpb.tuindice.about.presentation.resource.SettingsTextProvider
import com.gdavidpb.tuindice.about.presentation.viewmodel.SettingsViewModel
import com.gdavidpb.tuindice.about.testing.ANA_PROFILE_IDENTITY
import com.gdavidpb.tuindice.about.testing.FakeAboutRepository
import com.gdavidpb.tuindice.about.testing.FakeSettingsTextProvider
import com.gdavidpb.tuindice.about.testing.FakeStoreUrlDataSource
import com.gdavidpb.tuindice.about.testing.fakeObserveProfileIdentityUseCase
import com.gdavidpb.tuindice.about.testing.fakeSettingsAssistantUseCases
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingBrowserRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineHasNoShadowedRows
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import com.gdavidpb.tuindice.testkit.mvi.exportToMermaid
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

// Static table contract (host): alphabet, reachability, Λ coverage and the Mermaid
// export — all pure reads of machine.table. The dynamic walk lives in
// SettingsViewModelContractTest (iOS, where its getString-resolving rows can run).
class SettingsStateMachineContractTest {
	@Test
	fun machine_coversAlphabet_andStatesAreReachable() {
		val machine = createViewModel().machine

		assertMachineCoversAlphabet(
			machine,
			Settings.Action::class,
			SettingsInternalEvent::class
		)

		assertMachineHasNoShadowedRows(
			machine,
			Settings.Action::class,
			SettingsInternalEvent::class
		)

		assertMachineStatesReachable(
			machine = machine,
			initialState = Settings.State.Idle::class
		)

		assertMachineCoversEffects(machine, Settings.Effect::class)
	}

	@Test
	fun machine_exportsDeclaredTransitionsToMermaid() {
		val diagram = createViewModel().machine.exportToMermaid(
			machineName = "settings",
			initialState = Settings.State.Idle::class
		)

		// Captured from test output to publish the generated diagram as a docs artifact.
		println(diagram)

		val expectedFragments = listOf(
			"idle",
			"content",
			"LoadSettings",
			"SettingsVersionLoaded",
			"OpenTermsAndConditions / NavigateToBrowser",
			"ShareApp / ShareText",
			"OpenAbout / NavigateToAbout",
			"RequestSignOut / RequestSignOut",
			"ProfileIdentityObserved",
			"SupportEmailUriLoaded / OpenUri"
		)

		for (fragment in expectedFragments) {
			assertTrue(
				diagram.contains(fragment),
				"Expected Mermaid export to mention '$fragment':\n$diagram"
			)
		}
	}

	@Test
	fun machine_survivesSeededRandomWalk() = runTest {
		assertMachineRandomWalk(
			screenMachine = createMachine(),
			sampleEvents = listOf(
				Settings.Action.LoadSettings,
				Settings.Action.ShareApp,
				Settings.Action.OpenTermsAndConditions,
				Settings.Action.OpenPrivacyPolicy,
				Settings.Action.OpenSupport,
				Settings.Action.OpenUrl(url = "https://example.test"),
				Settings.Action.SetUsageDataCollectionEnabled(enabled = true),
				Settings.Action.SetAssistantConsent(granted = true),
				Settings.Action.RateOnStore,
				Settings.Action.ReportBug,
				Settings.Action.OpenAbout,
				Settings.Action.RequestSignOut,
				Settings.Action.OpenAssistantMemory,
				Settings.Action.RequestDeleteAssistantConversations,
				Settings.Action.ConfirmDeleteAssistantConversations,
				SettingsInternalEvent.SettingsVersionLoaded(versionText = "1.0", usageDataCollectionEnabled = false),
				SettingsInternalEvent.SettingsVersionLoadFailed(versionFallbackText = "Sin versión", usageDataCollectionEnabled = false),
				SettingsInternalEvent.ProfileIdentityObserved(identity = ANA_PROFILE_IDENTITY),
				SettingsInternalEvent.SupportEmailUriLoaded(uri = "mailto:soporte@example.test"),
				SettingsInternalEvent.StoreUriLoaded(uri = "https://example.test/store"),
				SettingsInternalEvent.AssistantAvailabilityObserved(visible = true),
				SettingsInternalEvent.AssistantConsentObserved(granted = true),
				SettingsInternalEvent.AssistantConsentWriteFailed(restoredGranted = false, message = "No se guardó"),
				SettingsInternalEvent.AssistantConversationsDeleted(message = "Borradas"),
				SettingsInternalEvent.AssistantConversationsDeleteFailed(message = "No se borraron")
			),
			coroutineScope = backgroundScope
		)
	}

	private fun createMachine(textProvider: SettingsTextProvider = FakeSettingsTextProvider()): SettingsMachine {
		return SettingsMachine(
			loadVersionUseCase = LoadVersionUseCase(
				aboutRepository = FakeAboutRepository(),
				reportingRepository = RecordingReportingRepository()
			),
			externalUseCases = SettingsExternalUseCases(
				sendSupportEmail = SendSupportEmailUseCase(
					configRepository = FakeConfigRepository(),
					deviceInfoRepository = FakeDeviceInfoRepository(),
					reportingRepository = RecordingReportingRepository()
				),
				openStore = OpenStoreUseCase(
					storeUrlRepository = FakeStoreUrlDataSource(),
					reportingRepository = RecordingReportingRepository()
				),
				openExternalUrl = OpenExternalUrlUseCase(
					browserRepository = RecordingBrowserRepository(),
					reportingRepository = RecordingReportingRepository()
				)
			),
			observeProfileIdentityUseCase = fakeObserveProfileIdentityUseCase(),
			appEnvironmentRepository = FakeAppEnvironmentRepository(),
			usageDataConsentRepository = InMemoryUsageDataConsentRepository(),
			assistantUseCases = fakeSettingsAssistantUseCases(),
			textProvider = textProvider
		)
	}

	private fun createViewModel(): SettingsViewModel {
		return SettingsViewModel(
			screenMachine = createMachine(),
			eventPublisher = NoOpEventPublisher
		)
	}
}
