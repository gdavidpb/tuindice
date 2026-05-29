package com.gdavidpb.tuindice.auth.presentation.action

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.auth.domain.usecase.ConfirmSignOutUseCase
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
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakePendingChangesRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
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
import kotlin.test.assertTrue

class AuthActionProcessorContractTest {
	private companion object {
		const val VALID_USB_ID = "20-26123"
	}

	@Test
	fun setUsageDataCollectionEnabledActionProcessor_persistsConsentAndUpdatesIdleState() = runTest {
		val usageDataConsentRepository = InMemoryUsageDataConsentRepository(initialValue = false)
		val processor = SetUsageDataCollectionEnabledActionProcessor(
			usageDataConsentRepository = usageDataConsentRepository
		)

		processor.process(
			action = SignIn.Action.SetUsageDataCollectionEnabled(enabled = true),
			sideEffect = {}
		).test {
			val state = assertIs<SignIn.State.Idle>(awaitItem()(SignIn.State.Idle()))
			assertTrue(state.usageDataCollectionEnabled)
			awaitComplete()
		}

		assertTrue(usageDataConsentRepository.isUsageDataCollectionEnabled())
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
	fun updatePasswordActionProcessor_emitsUpdatingMutation_thenReportsPasswordUpdated() = runTest {
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
		val initialState = UpdatePassword.State.Idle(
			password = "new-secret",
			isPasswordVisible = true
		)

		processor.process(
			action = UpdatePassword.Action.ClickSignIn(password = "new-secret"),
			sideEffect = effects::add
		).test {
			val updating = assertIs<UpdatePassword.State.Updating>(awaitItem()(initialState))
			assertEquals("new-secret", updating.password)
			assertEquals(true, updating.isPasswordVisible)
			assertEquals(updating, awaitItem()(updating))
			awaitComplete()
		}

		val effect = assertIs<UpdatePassword.Effect.PasswordUpdated>(effects.single())
		assertEquals(getString(Res.string.snack_password_updated), effect.message)
	}

	@Test
	fun confirmSignOutActionProcessor_emitsLoggingOutMutation_thenNavigatesToSignIn() = runTest {
		val authRepository = RecordingAuthRepository()
		val attestationRepository = FakeAttestationRepository()
		val sessionRepository = FakeSessionRepository()
		val applicationRepository = RecordingApplicationRepository()
		val syncStatusRepository = FakeSyncStatusRepository()
		val reportingRepository = RecordingReportingRepository()
		val processor = ConfirmSignOutActionProcessor(
			confirmSignOutUseCase = ConfirmSignOutUseCase(
				pendingChangesRepository = FakePendingChangesRepository(),
				reportingRepository = reportingRepository
			),
			signOutUseCase = SignOutUseCase(
				authRepository = authRepository,
				attestationRepository = attestationRepository,
				sessionRepository = sessionRepository,
				sessionInvalidationRepository = FakeSessionInvalidationRepository(),
				applicationRepository = applicationRepository,
				syncStatusRepository = syncStatusRepository,
				reportingRepository = reportingRepository
			)
		)
		val effects = mutableListOf<SignOut.Effect>()

		processor.process(
			action = SignOut.Action.ConfirmSignOut,
			sideEffect = effects::add
		).test {
			val loggingOut = awaitItem()(SignOut.State.Plain)
			assertEquals(SignOut.State.LoggingOut(), loggingOut)
			assertEquals(loggingOut, awaitItem()(loggingOut))
			assertEquals(loggingOut, awaitItem()(loggingOut))
			awaitComplete()
		}

		assertIs<SignOut.Effect.NavigateToSignIn>(effects.single())
	}

	@Test
	fun confirmSignOutActionProcessor_promptsPendingState_beforeLogout_whenLocalChangesExist() = runTest {
		val pendingChanges = PendingChanges(
			totalCount = 2,
			recordCount = 1,
			evaluationsCount = 1,
			hasFailedMutations = false
		)
		val processor = ConfirmSignOutActionProcessor(
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
			)
		)
		val effects = mutableListOf<SignOut.Effect>()

		processor.process(
			action = SignOut.Action.ConfirmSignOut,
			sideEffect = effects::add
		).test {
			val loggingOut = awaitItem()(SignOut.State.Plain)
			assertEquals(SignOut.State.LoggingOut(), loggingOut)
			val pending = awaitItem()(loggingOut)
			assertEquals(SignOut.State.Pending(pendingChanges), pending)
			awaitComplete()
		}

		assertEquals(emptyList(), effects)
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
