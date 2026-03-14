package com.gdavidpb.tuindice.auth.presentation.route

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.auth.domain.model.IssueTokensFlow
import com.gdavidpb.tuindice.auth.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.validator.UpdatePasswordParamsValidator
import com.gdavidpb.tuindice.auth.presentation.action.SetUpdatePasswordActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.UpdatePasswordActionProcessor
import com.gdavidpb.tuindice.auth.presentation.viewmodel.UpdatePasswordViewModel
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeOutdatedCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class UpdatePasswordRouteUiTest {
	private data class UpdatePasswordRouteFixture(
		val viewModel: UpdatePasswordViewModel,
		val authRepository: RecordingAuthRepository
	)

	@Test
	fun when_updatePasswordActionSucceeds_then_showsSnackBarAndDismisses() = runTuIndiceUiTest {
		val fixture = createUpdatePasswordViewModel()
		var dismissCalls = 0
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			UpdatePasswordRoute(
				onDismissRequest = { dismissCalls++ },
				showSnackBar = { message -> snackBarMessages += message },
				viewModel = fixture.viewModel
			)
		}

		runOnIdle {
			fixture.viewModel.signInAction(password = "nueva-clave-segura")
		}

		waitUntil(timeoutMillis = 2_000) {
			snackBarMessages.isNotEmpty() && dismissCalls > 0
		}

		assertEquals(1, snackBarMessages.size)
		assertEquals(1, dismissCalls)
	}

	@Test
	fun when_passwordTypedAndConfirmTapped_then_updatesPasswordAndDismisses() = runTuIndiceUiTest {
		val fixture = createUpdatePasswordViewModel()
		var dismissCalls = 0
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			UpdatePasswordRoute(
				onDismissRequest = { dismissCalls++ },
				showSnackBar = { message -> snackBarMessages += message },
				viewModel = fixture.viewModel
			)
		}

		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("nueva-clave-segura")
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			snackBarMessages.isNotEmpty() && dismissCalls > 0 && fixture.authRepository.issueTokensCalls.isNotEmpty()
		}

		val call = fixture.authRepository.issueTokensCalls.first()
		assertEquals("12-34567", call.usbId)
		assertEquals("nueva-clave-segura", call.password)
		assertEquals(IssueTokensFlow.ReissueTokens, call.flow)
		assertEquals(1, dismissCalls)
		assertEquals(1, snackBarMessages.size)
	}

	@Test
	fun when_updatePasswordActionFails_then_showsSnackBarAndDismisses() = runTuIndiceUiTest {
		val fixture = createUpdatePasswordViewModel(
			throwable = clientRequestException(HttpStatusCode.Unauthorized, path = "/auth/v1/token")
		)
		var dismissCalls = 0
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			UpdatePasswordRoute(
				onDismissRequest = { dismissCalls++ },
				showSnackBar = { message -> snackBarMessages += message },
				viewModel = fixture.viewModel
			)
		}

		runOnIdle {
			fixture.viewModel.signInAction(password = "password-invalida")
		}

		waitUntil(timeoutMillis = 2_000) {
			snackBarMessages.isNotEmpty() && dismissCalls > 0
		}

		assertEquals(1, snackBarMessages.size)
		assertEquals(1, dismissCalls)
		assertTrue(snackBarMessages.first().message.isNotBlank())
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
			outdatedCredentialsRepository = FakeOutdatedCredentialsRepository(),
			riskAttestationRepository = FakeAttestationRepository(),
			paramsValidator = UpdatePasswordParamsValidator(),
			exceptionHandler = UpdatePasswordExceptionHandler(
				networkRepository = FakeNetworkRepository(),
				reportingRepository = RecordingReportingRepository()
			)
		)

		return UpdatePasswordRouteFixture(
			viewModel = UpdatePasswordViewModel(
				setUpdatePasswordActionProcessor = SetUpdatePasswordActionProcessor(),
				updatePasswordActionProcessor = UpdatePasswordActionProcessor(updatePasswordUseCase)
			),
			authRepository = authRepository
		)
	}
}
