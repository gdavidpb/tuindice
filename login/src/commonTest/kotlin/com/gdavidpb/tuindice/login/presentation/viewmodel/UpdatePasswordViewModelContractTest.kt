package com.gdavidpb.tuindice.login.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.login.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.validator.UpdatePasswordParamsValidator
import com.gdavidpb.tuindice.login.presentation.action.SetUpdatePasswordActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.UpdatePasswordActionProcessor
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.login.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.login.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.login.testing.FakeSessionRepository
import com.gdavidpb.tuindice.login.testing.RecordingLoginRepository
import com.gdavidpb.tuindice.login.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import tuindice.login.generated.resources.Res
import tuindice.login.generated.resources.snack_password_updated
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
					loginRepository = RecordingLoginRepository(),
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
