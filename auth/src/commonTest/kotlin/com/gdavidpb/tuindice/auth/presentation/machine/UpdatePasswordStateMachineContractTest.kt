package com.gdavidpb.tuindice.auth.presentation.machine

import com.gdavidpb.tuindice.auth.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.validator.UpdatePasswordParamsValidator
import com.gdavidpb.tuindice.auth.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.auth.presentation.viewmodel.UpdatePasswordViewModel
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.TransitionResult
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineHasNoShadowedRows
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import com.gdavidpb.tuindice.testkit.mvi.exportToMermaid
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

// Static table contract (host): alphabet, reachability, Λ coverage and the Mermaid
// export — all pure reads of machine.table. The dynamic walk lives in
// UpdatePasswordViewModelContractTest (iOS, where its getString-resolving rows can run).
class UpdatePasswordStateMachineContractTest {
	@Test
	fun machine_coversAlphabet_andStatesAreReachable() {
		val machine = createViewModel().machine

		assertMachineCoversAlphabet(
			machine,
			UpdatePassword.Action::class,
			UpdatePasswordInternalEvent::class
		)

		assertMachineHasNoShadowedRows(
			machine,
			UpdatePassword.Action::class,
			UpdatePasswordInternalEvent::class
		)

		assertMachineStatesReachable(
			machine = machine,
			initialState = UpdatePassword.State.Idle::class
		)

		assertMachineCoversEffects(machine, UpdatePassword.Effect::class)
	}

	@Test
	fun machine_exportsDeclaredTransitionsToMermaid() {
		val diagram = createViewModel().machine.exportToMermaid(
			machineName = "update_password",
			initialState = UpdatePassword.State.Idle::class
		)

		// Captured from test output to publish the generated diagram as a docs artifact.
		println(diagram)

		val expectedFragments = listOf(
			"idle",
			"updating",
			"SetPassword",
			"ClickSignIn",
			"PasswordUpdateSucceeded / PasswordUpdated",
			"PasswordUpdateFailed"
		)

		for (fragment in expectedFragments) {
			assertTrue(
				diagram.contains(fragment),
				"Expected Mermaid export to mention '$fragment':\n$diagram"
			)
		}
	}

	// The field draws the message it is given and keeps none of its own, so the machine is what takes the
	// message of a refused password away when that password is edited.
	@Test
	fun editingThePassword_takesTheErrorAway() = runTest {
		val machine = createViewModel().machine
		val refused = UpdatePassword.State.Idle(password = "vieja", error = "Revisa tu contraseña")

		val result = machine.process(refused, UpdatePassword.Action.SetPassword(password = "nueva"))

		val after = assertIs<UpdatePassword.State.Idle>(
			assertIs<TransitionResult.Transitioned<UpdatePassword.State>>(result).toState
		)
		assertEquals("nueva", after.password)
		assertEquals(null, after.error)
	}

	private fun createViewModel(): UpdatePasswordViewModel {
		return UpdatePasswordViewModel(
			screenMachine = UpdatePasswordMachine(
				updatePasswordUseCase = UpdatePasswordUseCase(
					authRepository = RecordingAuthRepository(),
					sessionRepository = FakeSessionRepository(usbId = "20261234"),
					syncRepository = FakeSyncRepository(),
					credentialsRepository = FakeCredentialsRepository(),
					syncStatusRepository = FakeSyncStatusRepository(),
					attestationRepository = FakeAttestationRepository(),
					reportingRepository = RecordingReportingRepository(),
					paramsValidator = UpdatePasswordParamsValidator(),
					exceptionHandler = UpdatePasswordExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true)
					)
				),
				configRepository = FakeConfigRepository()
			),
			eventPublisher = NoOpEventPublisher
		)
	}
}
