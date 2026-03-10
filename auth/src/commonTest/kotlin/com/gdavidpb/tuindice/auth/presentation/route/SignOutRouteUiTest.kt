package com.gdavidpb.tuindice.auth.presentation.route

import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.auth.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.auth.presentation.action.SignOutActionProcessor
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignOutViewModel
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.testing.RecordingMessagingRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
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

		val signOutUseCase = SignOutUseCase(
			authRepository = RecordingAuthRepository(),
			sessionRepository = FakeSessionRepository(),
			messagingRepository = RecordingMessagingRepository(),
			applicationRepository = applicationRepository
		)

		return SignOutViewModel(
			signOutActionProcessor = SignOutActionProcessor(signOutUseCase)
		)
	}
}
