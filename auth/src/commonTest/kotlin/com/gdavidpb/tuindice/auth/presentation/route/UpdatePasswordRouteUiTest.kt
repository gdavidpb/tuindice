package com.gdavidpb.tuindice.auth.presentation.route

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.auth.domain.model.AttestedTokenFlow
import com.gdavidpb.tuindice.auth.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.validator.UpdatePasswordParamsValidator
import com.gdavidpb.tuindice.auth.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.auth.presentation.machine.UpdatePasswordMachine
import com.gdavidpb.tuindice.auth.presentation.viewmodel.UpdatePasswordViewModel
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class UpdatePasswordRouteUiTest {
	private data class UpdatePasswordRouteFixture(
		val viewModel: UpdatePasswordViewModel,
		val authRepository: RecordingAuthRepository
	)

	@Test
	fun when_updatePasswordActionSucceeds_then_showsSnackBarAndReportsPasswordUpdated() = runTuIndiceUiTest {
		val fixture = createUpdatePasswordViewModel()
		var dismissCalls = 0
		var passwordUpdatedCalls = 0
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			UpdatePasswordRoute(
				onDismissRequest = { dismissCalls++ },
				onPasswordUpdated = { passwordUpdatedCalls++ },
				showSnackBar = { message -> snackBarMessages += message },
				viewModel = fixture.viewModel
			)
		}

		runOnIdle {
			fixture.viewModel.setPasswordAction(password = "nueva-clave-segura")
			fixture.viewModel.signInAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			snackBarMessages.isNotEmpty() && passwordUpdatedCalls > 0
		}

		assertEquals(1, snackBarMessages.size)
		assertEquals(0, dismissCalls)
		assertEquals(1, passwordUpdatedCalls)
	}

	@Test
	fun when_passwordTypedAndConfirmTapped_then_updatesPasswordAndReportsPasswordUpdated() = runTuIndiceUiTest {
		val fixture = createUpdatePasswordViewModel()
		var dismissCalls = 0
		var passwordUpdatedCalls = 0
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			UpdatePasswordRoute(
				onDismissRequest = { dismissCalls++ },
				onPasswordUpdated = { passwordUpdatedCalls++ },
				showSnackBar = { message -> snackBarMessages += message },
				viewModel = fixture.viewModel
			)
		}

		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("nueva-clave-segura")
		onNodeWithTag(AuthUiTags.UpdatePasswordConfirmButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			snackBarMessages.isNotEmpty() &&
				passwordUpdatedCalls > 0 &&
				fixture.authRepository.reissueTokensCalls.isNotEmpty()
		}

		val call = fixture.authRepository.reissueTokensCalls.first()
		assertEquals("12-34567", call.usbId)
		assertEquals("nueva-clave-segura", call.password)
		assertEquals(0, dismissCalls)
		assertEquals(1, passwordUpdatedCalls)
		assertEquals(1, snackBarMessages.size)
	}

	@Test
	fun when_passwordToggleTapped_then_updatesPasswordVisibility() = runTuIndiceUiTest {
		val fixture = createUpdatePasswordViewModel()

		setTuIndiceTestContent {
			UpdatePasswordRoute(
				onDismissRequest = {},
				onPasswordUpdated = {},
				showSnackBar = {},
				viewModel = fixture.viewModel
			)
		}

		onNodeWithContentDescription("Mostrar contraseña").performClick()

		onNodeWithContentDescription("Ocultar contraseña").assertExists()
	}

	@Test
	fun when_updatePasswordActionFails_then_showsInlineErrorAndKeepsDialogOpen() = runTuIndiceUiTest {
		val fixture = createUpdatePasswordViewModel(
			throwable = clientRequestException(HttpStatusCode.Unauthorized, path = "/auth/v1/token")
		)
		var dismissCalls = 0
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			UpdatePasswordRoute(
				onDismissRequest = { dismissCalls++ },
				onPasswordUpdated = {},
				showSnackBar = { message -> snackBarMessages += message },
				viewModel = fixture.viewModel
			)
		}

		runOnIdle {
			fixture.viewModel.setPasswordAction(password = "password-invalida")
			fixture.viewModel.signInAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			val state = fixture.viewModel.state.value
			state is UpdatePassword.State.Idle && !state.error.isNullOrBlank()
		}

		val state = fixture.viewModel.state.value
		assertIs<UpdatePassword.State.Idle>(state)
		assertEquals("password-invalida", state.password)
		assertTrue(!state.error.isNullOrBlank())
		assertEquals(0, dismissCalls)
		assertTrue(snackBarMessages.isEmpty())
	}

	private fun createUpdatePasswordViewModel(): UpdatePasswordRouteFixture {
		return createUpdatePasswordViewModel(throwable = null)
	}

	private fun createUpdatePasswordViewModel(
		throwable: Throwable?
	): UpdatePasswordRouteFixture {
		val authRepository = RecordingAuthRepository(throwable = throwable)
		val updatePasswordUseCase = UpdatePasswordUseCase(
			authRepository = authRepository,
			sessionRepository = FakeSessionRepository(usbId = "12-34567"),
			syncRepository = FakeSyncRepository(),
			credentialsRepository = FakeCredentialsRepository(),
			syncStatusRepository = FakeSyncStatusRepository(),
			attestationRepository = FakeAttestationRepository(),
			reportingRepository = RecordingReportingRepository(),
			paramsValidator = UpdatePasswordParamsValidator(),
			exceptionHandler = UpdatePasswordExceptionHandler(
				networkRepository = FakeNetworkRepository()
			)
		)

		return UpdatePasswordRouteFixture(
			viewModel = UpdatePasswordViewModel(
				screenMachine = UpdatePasswordMachine(
					updatePasswordUseCase = updatePasswordUseCase,
					configRepository = FakeConfigRepository()
				),
				eventPublisher = NoOpEventPublisher
			),
			authRepository = authRepository
		)
	}
}
