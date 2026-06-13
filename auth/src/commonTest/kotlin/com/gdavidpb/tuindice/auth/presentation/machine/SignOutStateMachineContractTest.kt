package com.gdavidpb.tuindice.auth.presentation.machine

import com.gdavidpb.tuindice.auth.domain.usecase.ConfirmSignOutUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.FlushPendingChangesUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignOutViewModel
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.testkit.base.repository.FakePendingChangesRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import com.gdavidpb.tuindice.testkit.mvi.exportToMermaid
import kotlin.test.Test
import kotlin.test.assertTrue

// Static table contract (host): alphabet, reachability, Λ coverage and the Mermaid
// export — all pure reads of machine.table. The dynamic walk lives in
// SignOutViewModelContractTest.
class SignOutStateMachineContractTest {
	@Test
	fun machine_coversAlphabet_andStatesAreReachable() {
		val machine = createViewModel().machine

		assertMachineCoversAlphabet(
			machine,
			SignOut.Action::class,
			SignOutInternalEvent::class
		)

		assertMachineStatesReachable(
			machine = machine,
			initialState = SignOut.State.Plain::class
		)

		assertMachineCoversEffects(machine, SignOut.Effect::class)
	}

	@Test
	fun machine_exportsDeclaredTransitionsToMermaid() {
		val diagram = createViewModel().machine.exportToMermaid(
			machineName = "sign_out",
			initialState = SignOut.State.Plain::class
		)

		// Captured from test output to publish the generated diagram as a docs artifact.
		println(diagram)

		val expectedFragments = listOf(
			"plain",
			"pending",
			"flush_failed",
			"logging_out",
			"Initialize",
			"ClickSignOut",
			"SignOutSucceeded / NavigateToSignIn"
		)

		for (fragment in expectedFragments) {
			assertTrue(
				diagram.contains(fragment),
				"Expected Mermaid export to mention '$fragment':\n$diagram"
			)
		}
	}

	private fun createViewModel(): SignOutViewModel {
		val pendingChanges = PendingChanges(
			totalCount = 0,
			recordCount = 0,
			evaluationsCount = 0,
			hasFailedMutations = false
		)

		return SignOutViewModel(
			screenMachine = SignOutMachine(
				confirmSignOutUseCase = ConfirmSignOutUseCase(
					pendingChangesRepository = FakePendingChangesRepository(pendingChanges = pendingChanges),
					reportingRepository = RecordingReportingRepository()
				),
				signOutUseCase = SignOutUseCase(
					authRepository = RecordingAuthRepository(),
					attestationRepository = FakeAttestationRepository(),
					sessionRepository = FakeSessionRepository(),
					sessionInvalidationRepository = FakeSessionInvalidationRepository(),
					applicationRepository = RecordingApplicationRepository(),
					syncStatusRepository = FakeSyncStatusRepository(),
					reportingRepository = RecordingReportingRepository()
				),
				flushPendingChangesUseCase = FlushPendingChangesUseCase(
					pendingChangesRepository = FakePendingChangesRepository(pendingChanges = pendingChanges),
					reportingRepository = RecordingReportingRepository()
				)
			),
			eventPublisher = NoOpEventPublisher
		)
	}
}
