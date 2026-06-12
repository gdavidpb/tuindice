package $PACKAGE.presentation.machine

import $PACKAGE.domain.usecase.$OBSERVE_USE_CASE_NAME
import $PACKAGE.domain.usecase.$UPDATE_USE_CASE_NAME
import $PACKAGE.domain.usecase.exceptionhandler.$UPDATE_EXCEPTION_HANDLER_NAME
import $PACKAGE.presentation.contract.$FEATURE_NAME
import $PACKAGE.presentation.viewmodel.$VIEWMODEL_NAME
import $PACKAGE.testing.$RECORDING_REPOSITORY_NAME
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import kotlin.test.Test
import kotlin.test.assertTrue

class $MACHINE_CONTRACT_TEST_CLASS_NAME {
	@Test
	fun machine_coversAlphabet_andStatesAreReachable() {
		val machine = createViewModel().machine

		assertMachineCoversAlphabet(
			machine,
			$FEATURE_NAME.Action::class,
			$INTERNAL_EVENT_NAME::class
		)

		assertMachineStatesReachable(
			machine = machine,
			initialState = $FEATURE_NAME.State.Loading::class
		)

		assertMachineCoversEffects(machine, $FEATURE_NAME.Effect::class)
	}

	@Test
	fun machine_exportsDeclaredTransitionsToMermaid() {
		val diagram = createViewModel().exportMachineToMermaid()

		// Captured from test output to publish the generated diagram as a docs artifact.
		println(diagram)

		val expectedFragments = listOf(
			"loading",
			"content",
			"failed",
			"$OBSERVE_ACTION_NAME",
			"$REFRESH_ACTION_NAME",
			"$CONTENT_OBSERVED_EVENT_NAME",
			"$REFRESH_FAILED_EVENT_NAME / ShowSnackBar"
		)

		for (fragment in expectedFragments) {
			assertTrue(
				diagram.contains(fragment),
				"Expected Mermaid export to mention '$fragment':\n$diagram"
			)
		}
	}

	private fun createViewModel(): $VIEWMODEL_NAME {
		val repository = $RECORDING_REPOSITORY_NAME()
		val reportingRepository = RecordingReportingRepository()

		return $VIEWMODEL_NAME(
			screenMachine = $MACHINE_NAME(
				$OBSERVE_USE_CASE_PARAM_NAME = $OBSERVE_USE_CASE_NAME(
					$REPOSITORY_PARAM_NAME = repository,
					reportingRepository = reportingRepository
				),
				$UPDATE_USE_CASE_PARAM_NAME = $UPDATE_USE_CASE_NAME(
					$REPOSITORY_PARAM_NAME = repository,
					reportingRepository = reportingRepository,
					exceptionHandler = $UPDATE_EXCEPTION_HANDLER_NAME(
						networkRepository = FakeNetworkRepository(isAvailable = true),
						reportingRepository = reportingRepository
					)
				)
			),
			eventPublisher = NoOpEventPublisher
		)
	}
}
