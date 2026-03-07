package com.gdavidpb.tuindice.login.presentation.action

import app.cash.turbine.test
import com.gdavidpb.tuindice.login.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.login.domain.usecase.validator.UpdatePasswordParamsValidator
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.presentation.contract.SignOut
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.login.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.login.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.login.testing.FakeSessionRepository
import com.gdavidpb.tuindice.login.testing.RecordingApplicationRepository
import com.gdavidpb.tuindice.login.testing.RecordingLoginRepository
import com.gdavidpb.tuindice.login.testing.RecordingMessagingRepository
import com.gdavidpb.tuindice.login.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import tuindice.login.generated.resources.Res
import tuindice.login.generated.resources.snack_password_updated
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LoginActionProcessorContractTest {
	private companion object {
		const val VALID_USB_ID = "20-26123"
	}

	@Test
	fun signInActionProcessor_emitsLoggingMutation_thenNavigatesToSummary() = runTest {
		val processor = SignInActionProcessor(
			signInUseCase = SignInUseCase(
				loginRepository = RecordingLoginRepository(),
				messagingRepository = RecordingMessagingRepository(),
				riskAttestationRepository = FakeAttestationRepository(),
				paramsValidator = SignInParamsValidator(),
				exceptionHandler = SignInExceptionHandler(
					networkRepository = FakeNetworkRepository(isAvailable = true),
					reportingRepository = RecordingReportingRepository()
				)
			),
			configRepository = FakeConfigRepository()
		)
		val effects = mutableListOf<SignIn.Effect>()

		processor.process(
			action = SignIn.Action.ClickSignIn(
				usbId = VALID_USB_ID,
				password = "secret123"
			),
			sideEffect = effects::add
		).test {
			val loading = awaitItem()(SignIn.State.Idle())
			val logging = assertIs<SignIn.State.LoggingIn>(loading)
			assertEquals(VALID_USB_ID, logging.usbId)

			val next = awaitItem()(logging)
			assertEquals(logging, next)
			awaitComplete()
		}

		assertIs<SignIn.Effect.NavigateToSummary>(effects.single())
	}

	@Test
	fun updatePasswordActionProcessor_emitsUpdatingMutation_thenShowsSuccessSnackBar() = runTest {
		val processor = UpdatePasswordActionProcessor(
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
		val effects = mutableListOf<UpdatePassword.Effect>()
		val initialState = UpdatePassword.State.Idle(password = "new-secret")

		processor.process(
			action = UpdatePassword.Action.ClickSignIn(password = "new-secret"),
			sideEffect = effects::add
		).test {
			val updating = assertIs<UpdatePassword.State.Updating>(awaitItem()(initialState))
			assertEquals("new-secret", updating.password)
			assertEquals(updating, awaitItem()(updating))
			awaitComplete()
		}

		val effect = assertIs<UpdatePassword.Effect.ShowSnackBar>(effects.single())
		assertEquals(getString(Res.string.snack_password_updated), effect.message)
	}

	@Test
	fun signOutActionProcessor_emitsLoggingOutMutation_thenNavigatesToSignIn() = runTest {
		val processor = SignOutActionProcessor(
			signOutUseCase = SignOutUseCase(
				sessionRepository = FakeSessionRepository(),
				messagingRepository = RecordingMessagingRepository(),
				applicationRepository = RecordingApplicationRepository()
			)
		)
		val effects = mutableListOf<SignOut.Effect>()

		processor.process(
			action = SignOut.Action.ConfirmSignOut,
			sideEffect = effects::add
		).test {
			val loggingOut = awaitItem()(SignOut.State.Idle)
			assertEquals(SignOut.State.LoggingOut, loggingOut)
			assertEquals(loggingOut, awaitItem()(loggingOut))
			awaitComplete()
		}

		assertIs<SignOut.Effect.NavigateToSignIn>(effects.single())
	}
}
