package com.gdavidpb.tuindice.auth.presentation.route

import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.auth.domain.usecase.ConfirmSignOutUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.FlushPendingChangesUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.LoadPendingChangesUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.auth.presentation.action.ConfirmSignOutActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.FlushAndSignOutActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.ForceSignOutActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.LoadPendingChangesActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.OpenUpdatePasswordActionProcessor
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignOutViewModel
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakePendingChangesRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import io.github.vinceglb.filekit.PlatformFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SignOutRouteUiTest {
	@Test
	fun when_signOutActionTriggered_then_navigatesToSignIn() = runTuIndiceUiTest {
		val viewModel = createSignOutViewModel()
		var navigateCalls = 0
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SignOutRoute(
				onNavigateToSignIn = { navigateCalls++ },
				onNavigateToUpdatePassword = {},
				onDismissRequest = {},
				showSnackBar = { message -> snackBarMessages += message },
				viewModel = viewModel
			)
		}

		runOnIdle {
			viewModel.signOutAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			navigateCalls == 1
		}

		assertEquals(1, navigateCalls)
		assertEquals(0, snackBarMessages.size)
	}

	@Test
	fun when_signOutActionFails_then_showsSnackBarWithoutNavigation() = runTuIndiceUiTest {
		val viewModel = createSignOutViewModel(shouldThrowOnClearData = true)
		var navigateCalls = 0
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SignOutRoute(
				onNavigateToSignIn = { navigateCalls++ },
				onNavigateToUpdatePassword = {},
				onDismissRequest = {},
				showSnackBar = { message -> snackBarMessages += message },
				viewModel = viewModel
			)
		}

		runOnIdle {
			viewModel.signOutAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			snackBarMessages.isNotEmpty()
		}

		assertEquals(0, navigateCalls)
		assertEquals(1, snackBarMessages.size)
		assertTrue(snackBarMessages.first().message.isNotBlank())
	}

	private fun createSignOutViewModel(
		shouldThrowOnClearData: Boolean = false
	): SignOutViewModel {
		val applicationRepository = if (shouldThrowOnClearData) {
			object : ApplicationRepository {
				override suspend fun clearData() {
					error("forced sign out failure")
				}

				override suspend fun canOpen(file: PlatformFile): Boolean = true
			}
		} else {
			RecordingApplicationRepository()
		}

		val pendingChangesRepository = FakePendingChangesRepository()
		val reportingRepository = RecordingReportingRepository()
		val authRepository = RecordingAuthRepository()
		val attestationRepository = FakeAttestationRepository()
		val sessionRepository = FakeSessionRepository()
		val syncStatusRepository = FakeSyncStatusRepository()
		val signOutUseCase = SignOutUseCase(
			authRepository = authRepository,
			attestationRepository = attestationRepository,
			sessionRepository = sessionRepository,
			applicationRepository = applicationRepository,
			syncStatusRepository = syncStatusRepository,
			reportingRepository = reportingRepository
		)

		return SignOutViewModel(
			loadPendingChangesActionProcessor = LoadPendingChangesActionProcessor(
				LoadPendingChangesUseCase(
					pendingChangesRepository = pendingChangesRepository,
					reportingRepository = reportingRepository
				)
			),
			confirmSignOutActionProcessor = ConfirmSignOutActionProcessor(
				ConfirmSignOutUseCase(
					pendingChangesRepository = pendingChangesRepository,
					reportingRepository = reportingRepository
				),
				signOutUseCase = signOutUseCase
			),
			flushAndSignOutActionProcessor = FlushAndSignOutActionProcessor(
				FlushPendingChangesUseCase(
					pendingChangesRepository = pendingChangesRepository,
					reportingRepository = reportingRepository
				),
				signOutUseCase = signOutUseCase
			),
			forceSignOutActionProcessor = ForceSignOutActionProcessor(signOutUseCase),
			openUpdatePasswordActionProcessor = OpenUpdatePasswordActionProcessor()
		)
	}
}
