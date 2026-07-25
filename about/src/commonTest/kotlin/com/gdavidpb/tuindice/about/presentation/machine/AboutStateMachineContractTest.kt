package com.gdavidpb.tuindice.about.presentation.machine

import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.about.testing.FakeAboutRepository
import com.gdavidpb.tuindice.about.testing.FakeStoreUrlDataSource
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
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import com.gdavidpb.tuindice.testkit.mvi.exportToMermaid
import kotlin.test.Test
import kotlin.test.assertTrue

// Static table contract (host): alphabet, reachability, Λ coverage and the Mermaid
// export — all pure reads of machine.table. The dynamic walk lives in
// AboutViewModelContractTest (iOS, where its getString-resolving rows can run).
class AboutStateMachineContractTest {
	@Test
	fun machine_coversAlphabet_andStatesAreReachable() {
		val machine = createViewModel().machine

		assertMachineCoversAlphabet(
			machine,
			About.Action::class,
			AboutInternalEvent::class
		)

		assertMachineHasNoShadowedRows(
			machine,
			About.Action::class,
			AboutInternalEvent::class
		)

		assertMachineStatesReachable(
			machine = machine,
			initialState = About.State.Idle::class
		)

		assertMachineCoversEffects(machine, About.Effect::class)
	}

	@Test
	fun machine_exportsDeclaredTransitionsToMermaid() {
		val diagram = createViewModel().machine.exportToMermaid(
			machineName = "about",
			initialState = About.State.Idle::class
		)

		// Captured from test output to publish the generated diagram as a docs artifact.
		println(diagram)

		val expectedFragments = listOf(
			"idle",
			"content",
			"LoadVersion",
			"AboutVersionLoaded",
			"OpenTermsAndConditions / NavigateToBrowser",
			"ShareApp / ShareText",
			"SupportEmailUriLoaded / OpenUri"
		)

		for (fragment in expectedFragments) {
			assertTrue(
				diagram.contains(fragment),
				"Expected Mermaid export to mention '$fragment':\n$diagram"
			)
		}
	}

	private fun createViewModel(): AboutViewModel {
		return AboutViewModel(
			screenMachine = AboutMachine(
				loadVersionUseCase = LoadVersionUseCase(
					aboutRepository = FakeAboutRepository(),
					reportingRepository = RecordingReportingRepository()
				),
				sendSupportEmailUseCase = SendSupportEmailUseCase(
					configRepository = FakeConfigRepository(),
					deviceInfoRepository = FakeDeviceInfoRepository(),
					reportingRepository = RecordingReportingRepository()
				),
				openStoreUseCase = OpenStoreUseCase(
					storeUrlRepository = FakeStoreUrlDataSource(),
					reportingRepository = RecordingReportingRepository()
				),
				openExternalUrlUseCase = OpenExternalUrlUseCase(
					browserRepository = RecordingBrowserRepository(),
					reportingRepository = RecordingReportingRepository()
				),
				appEnvironmentRepository = FakeAppEnvironmentRepository(),
				usageDataConsentRepository = InMemoryUsageDataConsentRepository()
			),
			eventPublisher = NoOpEventPublisher
		)
	}
}
