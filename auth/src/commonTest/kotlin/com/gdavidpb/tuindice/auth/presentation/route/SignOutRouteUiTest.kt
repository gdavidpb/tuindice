package com.gdavidpb.tuindice.auth.presentation.route

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.auth.domain.usecase.ConfirmSignOutUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.FlushPendingChangesUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.auth.presentation.machine.SignOutMachine
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignOutViewModel
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.base.repository.RunningSessionEndRepository
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.testkit.base.repository.FakePendingChangesRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
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
				initialPendingChanges = PendingChanges.Empty,
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
		val viewModel = createSignOutViewModel(shouldFailRevoke = true)
		var navigateCalls = 0
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SignOutRoute(
				initialPendingChanges = PendingChanges.Empty,
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

	@Test
	fun when_routeStartsWithPendingChanges_then_rendersPendingMessageImmediately() = runTuIndiceUiTest {
		val pendingChanges = PendingChanges(
			totalCount = 4,
			recordCount = 4,
			evaluationsCount = 0,
			hasFailedMutations = true
		)
		val viewModel = createSignOutViewModel()

		setTuIndiceTestContent {
			SignOutRoute(
				initialPendingChanges = pendingChanges,
				onNavigateToSignIn = {},
				onNavigateToUpdatePassword = {},
				onDismissRequest = {},
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		onNodeWithText("Tienes 4 cambios pendientes", substring = true)
			.assertExists()
	}

	private fun createSignOutViewModel(
		shouldFailRevoke: Boolean = false
	): SignOutViewModel {
		val applicationRepository = RecordingApplicationRepository()
		val sessionInvalidationRepository = FakeSessionInvalidationRepository()
		val pendingChangesRepository = FakePendingChangesRepository()
		val reportingRepository = RecordingReportingRepository()
		val authRepository = if (shouldFailRevoke) {
			RecordingAuthRepository(throwable = IllegalStateException("forced sign out failure"))
		} else {
			RecordingAuthRepository()
		}
		val attestationRepository = FakeAttestationRepository()
		val sessionRepository = FakeSessionRepository()
		val syncStatusRepository = FakeSyncStatusRepository()
		val signOutUseCase = SignOutUseCase(
			authRepository = authRepository,
			attestationRepository = attestationRepository,
			sessionRepository = sessionRepository,
			sessionInvalidationRepository = sessionInvalidationRepository,
			sessionEndRepository = RunningSessionEndRepository(
				applicationRepository = applicationRepository,
				sessionRepository = sessionRepository,
				syncStatusRepository = syncStatusRepository,
				sessionCoroutineScope = testSessionCoroutineScope(),
				sessionInvalidationRepository = sessionInvalidationRepository
			),
			reportingRepository = reportingRepository
		)

		return SignOutViewModel(
			screenMachine = SignOutMachine(
				confirmSignOutUseCase = ConfirmSignOutUseCase(
					pendingChangesRepository = pendingChangesRepository,
					reportingRepository = reportingRepository
				),
				signOutUseCase = signOutUseCase,
				flushPendingChangesUseCase = FlushPendingChangesUseCase(
					pendingChangesRepository = pendingChangesRepository,
					reportingRepository = reportingRepository
				)
			),
			eventPublisher = NoOpEventPublisher
		)
	}
}
