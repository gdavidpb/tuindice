package com.gdavidpb.tuindice.enrollmentproof.presentation.machine

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.FetchEnrollmentProofUseCase
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler.FetchEnrollmentProofExceptionHandler
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel.EnrollmentProofViewModel
import com.gdavidpb.tuindice.enrollmentproof.testing.DEFAULT_ENROLLMENT_PROOF
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeEnrollmentProofRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeEnrollmentProofTextProvider
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeFileRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import com.gdavidpb.tuindice.testkit.mvi.exportToMermaid
import kotlin.test.Test
import kotlin.test.assertTrue

// Static table contract (host): alphabet, reachability, Λ coverage and the Mermaid
// export — all pure reads of machine.table. The dynamic walk lives in
// EnrollmentProofViewModelContractTest.
class EnrollmentProofStateMachineContractTest {
	@Test
	fun machine_coversAlphabet_andStatesAreReachable() {
		val machine = createViewModel().machine

		assertMachineCoversAlphabet(
			machine,
			Enrollment.Action::class,
			EnrollmentProofInternalEvent::class
		)

		assertMachineStatesReachable(
			machine = machine,
			initialState = Enrollment.State.Fetching::class
		)

		assertMachineCoversEffects(machine, Enrollment.Effect::class)
	}

	@Test
	fun machine_exportsDeclaredTransitionsToMermaid() {
		val diagram = createViewModel().machine.exportToMermaid(
			machineName = "enrollment_proof",
			initialState = Enrollment.State.Fetching::class
		)

		// Captured from test output to publish the generated diagram as a docs artifact.
		println(diagram)

		val expectedFragments = listOf(
			"fetching",
			"FetchEnrollmentProof",
			"EnrollmentProofFetched / OpenEnrollmentProof",
			"EnrollmentProofFetchFailed / ShowSnackBar",
			"EnrollmentProofUnauthorized / NavigateToOutdatedCredentials"
		)

		for (fragment in expectedFragments) {
			assertTrue(
				diagram.contains(fragment),
				"Expected Mermaid export to mention '$fragment':\n$diagram"
			)
		}
	}

	private fun createViewModel(): EnrollmentProofViewModel {
		return EnrollmentProofViewModel(
			screenMachine = EnrollmentProofMachine(
				fetchEnrollmentProofUseCase = FetchEnrollmentProofUseCase(
					applicationRepository = FakeFileRepository(canOpen = true),
					enrollmentProofRepository = FakeEnrollmentProofRepository(
						enrollmentProof = DEFAULT_ENROLLMENT_PROOF
					),
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = FetchEnrollmentProofExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true)
					)
				),
				textProvider = FakeEnrollmentProofTextProvider()
			),
			eventPublisher = NoOpEventPublisher
		)
	}
}
