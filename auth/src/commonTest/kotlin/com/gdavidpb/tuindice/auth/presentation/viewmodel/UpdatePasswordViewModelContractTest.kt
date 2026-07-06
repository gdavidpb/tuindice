package com.gdavidpb.tuindice.auth.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.auth.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.validator.UpdatePasswordParamsValidator
import com.gdavidpb.tuindice.auth.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.auth.presentation.machine.UpdatePasswordInternalEvent
import com.gdavidpb.tuindice.auth.presentation.machine.UpdatePasswordMachine
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.snack_password_updated
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class UpdatePasswordViewModelContractTest {
	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun publicActions_updatePasswordState_andEmitPasswordUpdatedEffect() = runTest {
		val viewModel = UpdatePasswordViewModel(
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
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(UpdatePassword.State.Idle(), awaitItem())

				viewModel.togglePasswordVisibilityAction()
				assertEquals(UpdatePassword.State.Idle(isPasswordVisible = true), awaitItem())

				viewModel.setPasswordAction("new-secret")
				assertEquals(
					UpdatePassword.State.Idle(
						password = "new-secret",
						isPasswordVisible = true
					),
					awaitItem()
				)

				viewModel.signInAction()
				val updating = assertIs<UpdatePassword.State.Updating>(awaitItem())
				assertEquals("new-secret", updating.password)
				assertEquals(true, updating.isPasswordVisible)

				cancelAndIgnoreRemainingEvents()
			}

			viewModel.effect.test {
				val effect = assertIs<UpdatePassword.Effect.PasswordUpdated>(awaitItem())
				assertEquals(getString(Res.string.snack_password_updated), effect.message)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun machine_survivesSeededRandomWalk() = runTest {
		val screenMachine = UpdatePasswordMachine(
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
		)

		assertMachineRandomWalk(
			screenMachine = screenMachine,
			sampleEvents = listOf(
				UpdatePassword.Action.SetPassword(password = "new-secret"),
				UpdatePassword.Action.TogglePasswordVisibility,
				UpdatePassword.Action.ClickSignIn,
				UpdatePasswordInternalEvent.PasswordUpdateSucceeded(
					message = "Contraseña actualizada"
				),
				UpdatePasswordInternalEvent.PasswordUpdateFailed(
					message = "No se pudo actualizar"
				)
			),
			coroutineScope = backgroundScope,
			// Conservative floor: every internal event is sampled by hand; raise to the
			// observed coverage once the walk has run on CI.
			minRowCoverage = 0.4
		)
	}
}
