package com.gdavidpb.tuindice.auth.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.auth.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.validator.UpdatePasswordParamsValidator
import com.gdavidpb.tuindice.auth.presentation.action.SetUpdatePasswordActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.UpdatePasswordActionProcessor
import com.gdavidpb.tuindice.auth.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.auth.testing.FakeSessionRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.testing.RecordingReportingRepository
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
	fun publicActions_updatePasswordState_andEmitSuccessSnackBar() = runTest {
		val viewModel = UpdatePasswordViewModel(
			setUpdatePasswordActionProcessor = SetUpdatePasswordActionProcessor(),
			updatePasswordActionProcessor = UpdatePasswordActionProcessor(
				updatePasswordUseCase = UpdatePasswordUseCase(
					authRepository = RecordingAuthRepository(),
					sessionRepository = FakeSessionRepository(usbId = "20261234"),
					riskAttestationRepository = FakeAttestationRepository(),
					paramsValidator = UpdatePasswordParamsValidator(),
					exceptionHandler = UpdatePasswordExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true),
						reportingRepository = RecordingReportingRepository()
					)
				)
			)
		)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(UpdatePassword.State.Idle(), awaitItem())

				viewModel.setPasswordAction("new-secret")
				assertEquals(UpdatePassword.State.Idle(password = "new-secret"), awaitItem())

				viewModel.signInAction("new-secret")
				val updating = assertIs<UpdatePassword.State.Updating>(awaitItem())
				assertEquals("new-secret", updating.password)

				cancelAndIgnoreRemainingEvents()
			}

			viewModel.effect.test {
				viewModel.signInAction("new-secret")
				val effect = assertIs<UpdatePassword.Effect.ShowSnackBar>(awaitItem())
				assertEquals(getString(Res.string.snack_password_updated), effect.message)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}
}
