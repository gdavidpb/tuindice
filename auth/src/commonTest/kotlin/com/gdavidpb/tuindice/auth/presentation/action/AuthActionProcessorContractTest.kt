package com.gdavidpb.tuindice.auth.presentation.action

import app.cash.turbine.test
import com.gdavidpb.tuindice.auth.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.auth.domain.usecase.validator.UpdatePasswordParamsValidator
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.auth.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.auth.testing.FakeSessionRepository
import com.gdavidpb.tuindice.auth.testing.RecordingApplicationRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.testing.RecordingMessagingRepository
import com.gdavidpb.tuindice.auth.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.error_account_disabled
import tuindice.auth.generated.resources.snack_password_updated
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AuthActionProcessorContractTest {
	private companion object {
		const val VALID_USB_ID = "20-26123"
	}

	@Test
	fun signInActionProcessor_emitsLoggingMutation_thenNavigatesToSummary() = runTest {
		val processor = SignInActionProcessor(
			signInUseCase = SignInUseCase(
				authRepository = RecordingAuthRepository(),
				messagingRepository = RecordingMessagingRepository(),
				syncRepository = FakeSyncRepository(),
				credentialsRepository = FakeCredentialsRepository(),
				syncStatusRepository = FakeSyncStatusRepository(),
				attestationRepository = FakeAttestationRepository(),
				reportingRepository = RecordingReportingRepository(),
				paramsValidator = SignInParamsValidator(),
				exceptionHandler = SignInExceptionHandler(
					networkRepository = FakeNetworkRepository(isAvailable = true)
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
				authRepository = RecordingAuthRepository(),
				sessionRepository = FakeSessionRepository(),
				messagingRepository = RecordingMessagingRepository(),
				applicationRepository = RecordingApplicationRepository(),
				syncStatusRepository = FakeSyncStatusRepository(),
				reportingRepository = RecordingReportingRepository()
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

	@Test
	fun signInActionProcessor_showsNonRetrySnackBar_forDisabledAccount() = runTest {
		val processor = SignInActionProcessor(
			signInUseCase = SignInUseCase(
				authRepository = RecordingAuthRepository(
					throwable = clientRequestException(HttpStatusCode.Locked, path = "/auth/v1/token")
				),
				messagingRepository = RecordingMessagingRepository(),
				syncRepository = FakeSyncRepository(),
				credentialsRepository = FakeCredentialsRepository(),
				syncStatusRepository = FakeSyncStatusRepository(),
				attestationRepository = FakeAttestationRepository(),
				reportingRepository = RecordingReportingRepository(),
				paramsValidator = SignInParamsValidator(),
				exceptionHandler = SignInExceptionHandler(
					networkRepository = FakeNetworkRepository(isAvailable = true)
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
			val logging = assertIs<SignIn.State.LoggingIn>(awaitItem()(SignIn.State.Idle()))
			val idle = assertIs<SignIn.State.Idle>(awaitItem()(logging))
			assertEquals(VALID_USB_ID, idle.usbId)
			awaitComplete()
		}

		val effect = assertIs<SignIn.Effect.ShowSnackBar>(effects.single())
		assertEquals(getString(Res.string.error_account_disabled), effect.message)
	}
}
