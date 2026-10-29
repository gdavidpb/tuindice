package com.gdavidpb.tuindice.about.presentation.machine

import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.about.testing.FakeAboutRepository
import com.gdavidpb.tuindice.about.testing.FakeAboutTextProvider
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
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

// Static table contract (host): alphabet, reachability, Λ coverage and the Mermaid export, all pure reads of
// machine.table. The dynamic walk is here too: the about machine resolves no platform resources.
class AboutStateMachineContractTest {
	@Test
	fun machine_coversAlphabet_andStatesAreReachable() {
		val machine = createViewModel().machine

		assertMachineCoversAlphabet(machine, About.Action::class, AboutInternalEvent::class)
		assertMachineHasNoShadowedRows(machine, About.Action::class, AboutInternalEvent::class)
		assertMachineStatesReachable(machine = machine, initialState = About.State.Idle::class)
		assertMachineCoversEffects(machine, About.Effect::class)
	}

	@Test
	fun machine_exportsDeclaredTransitionsToMermaid() {
		val diagram = createViewModel().machine.exportToMermaid(
			machineName = "about",
			initialState = About.State.Idle::class
		)

		listOf(
			"idle",
			"content",
			"LoadVersion",
			"AboutVersionLoaded",
			"ContactEmailUriLoaded / OpenUri"
		).forEach { fragment ->
			assertTrue(diagram.contains(fragment), "Expected Mermaid export to mention '$fragment':\n$diagram")
		}
	}

	@Test
	fun machine_survivesSeededRandomWalk() = runTest {
		assertMachineRandomWalk(
			screenMachine = createMachine(),
			sampleEvents = listOf(
				About.Action.LoadVersion,
				About.Action.OpenUrl(url = "https://example.test"),
				About.Action.ContactDeveloper,
				AboutInternalEvent.AboutVersionLoaded(versionText = "1.0"),
				AboutInternalEvent.AboutVersionLoadFailed(versionFallbackText = "Sin versión"),
				AboutInternalEvent.ContactEmailUriLoaded(uri = "mailto:soporte@example.test")
			),
			coroutineScope = backgroundScope
		)
	}

	private fun createMachine(): AboutMachine {
		return AboutMachine(
			loadVersionUseCase = LoadVersionUseCase(
				aboutRepository = FakeAboutRepository(),
				reportingRepository = RecordingReportingRepository()
			),
			externalUseCases = AboutExternalUseCases(
				sendSupportEmail = SendSupportEmailUseCase(
					configRepository = FakeConfigRepository(),
					deviceInfoRepository = FakeDeviceInfoRepository(),
					reportingRepository = RecordingReportingRepository()
				),
				openExternalUrl = OpenExternalUrlUseCase(
					browserRepository = RecordingBrowserRepository(),
					reportingRepository = RecordingReportingRepository()
				)
			),
			textProvider = FakeAboutTextProvider()
		)
	}

	private fun createViewModel(): AboutViewModel {
		return AboutViewModel(
			screenMachine = createMachine(),
			eventPublisher = NoOpEventPublisher
		)
	}
}
