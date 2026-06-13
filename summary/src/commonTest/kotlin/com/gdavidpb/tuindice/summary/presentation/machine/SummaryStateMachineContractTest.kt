package com.gdavidpb.tuindice.summary.presentation.machine

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.summary.testing.RecordingUserRepository
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import com.gdavidpb.tuindice.testkit.mvi.exportToMermaid
import io.github.vinceglb.filekit.PlatformFile
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class SummaryStateMachineContractTest {
	@Test
	fun machine_coversAlphabet_andStatesAreReachable() {
		val machine = createViewModel().machine

		assertMachineCoversAlphabet(
			machine,
			Summary.Action::class,
			SummaryInternalEvent::class
		)

		assertMachineStatesReachable(
			machine = machine,
			initialState = Summary.State.Idle::class
		)

		assertMachineCoversEffects(machine, Summary.Effect::class)
	}

	@Test
	fun machine_exportsDeclaredTransitionsToMermaid() {
		val diagram = createViewModel().machine.exportToMermaid(
			machineName = "summary",
			initialState = Summary.State.Idle::class
		)

		// Captured from test output to publish the generated diagram as a docs artifact.
		println(diagram)

		val expectedFragments = listOf(
			"idle",
			"loading",
			"content",
			"failed",
			"ObserveSummary",
			"RefreshSummary",
			"UserObserved",
			"ProfilePictureRemovalFailed / ShowSnackBar",
			"TakeProfilePicture / OpenCamera",
			"RemoveProfilePicture / ShowRemoveProfilePictureConfirmationDialog"
		)

		for (fragment in expectedFragments) {
			assertTrue(
				diagram.contains(fragment),
				"Expected Mermaid export to mention '$fragment':\n$diagram"
			)
		}
	}

	private fun createViewModel(): SummaryViewModel {
		val userRepository = RecordingUserRepository(users = flowOf(DEFAULT_SUMMARY_USER))
		val reportingRepository = RecordingReportingRepository()

		return SummaryViewModel(
			screenMachine = SummaryMachine(
				observeUserUseCase = ObserveUserUseCase(
					userRepository = userRepository,
					reportingRepository = reportingRepository
				),
				updateUserUseCase = UpdateUserUseCase(
					userRepository = userRepository,
					reportingRepository = reportingRepository,
					exceptionHandler = UpdateUserExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true)
					)
				),
				uploadProfilePictureUseCase = UploadProfilePictureUseCase(
					userRepository = userRepository,
					reportingRepository = reportingRepository,
					exceptionHandler = UploadProfilePictureExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true)
					)
				),
				removeProfilePictureUseCase = RemoveProfilePictureUseCase(
					userRepository = userRepository,
					reportingRepository = reportingRepository,
					exceptionHandler = RemoveProfilePictureExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true)
					)
				)
			),
			eventPublisher = NoOpEventPublisher
		)
	}
}
