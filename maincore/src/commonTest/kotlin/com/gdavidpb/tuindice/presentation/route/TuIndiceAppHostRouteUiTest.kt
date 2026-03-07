package com.gdavidpb.tuindice.presentation.route

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.base.repository.FakeUpdateRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingBrowserRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReviewRepository
import com.gdavidpb.tuindice.testing.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testing.createMainViewModel
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class TuIndiceAppHostRouteUiTest {
	@Test
	fun when_hostRouteStarts_then_rendersNavHostAndTriggersReviewRequest() = runTuIndiceUiTest {
		val reviewRepository = RecordingReviewRepository()

		setTuIndiceTestContent {
			TuIndiceAppHostRoute(
				onConfirmExitClick = {},
				isSwipeBackNavigationEnabled = false,
				browserRepository = RecordingBrowserRepository(),
				deviceInfoRepository = FakeDeviceInfoRepository(hasCamera = false),
				reviewRepository = reviewRepository,
				updateRepository = FakeUpdateRepository(),
				viewModel = createMainViewModel()
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			reviewRepository.launchCalls > 0
		}

		assertTrue(reviewRepository.launchCalls > 0)
	}

	@Test
	fun when_updateIsAvailable_then_hostRouteLaunchesUpdateFlow() = runTuIndiceUiTest {
		val updateRepository = FakeUpdateRepository(updateAction = UpdateAction.Immediate)
		val viewModel = createMainViewModel(updateRepository = updateRepository)

		setTuIndiceTestContent {
			TuIndiceAppHostRoute(
				onConfirmExitClick = {},
				isSwipeBackNavigationEnabled = false,
				browserRepository = RecordingBrowserRepository(),
				deviceInfoRepository = FakeDeviceInfoRepository(hasCamera = false),
				reviewRepository = RecordingReviewRepository(),
				updateRepository = updateRepository,
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			updateRepository.launchedActions.isNotEmpty()
		}

		assertTrue(updateRepository.checkCalls.isNotEmpty())
		assertTrue(updateRepository.launchedActions.contains(UpdateAction.Immediate))
	}

	@Test
	fun when_googlePlayServicesDialogConfirmed_then_hostRouteInvokesExitCallback() = runTuIndiceUiTest {
		var confirmExitCalls = 0

		setTuIndiceTestContent {
			TuIndiceAppHostRoute(
				onConfirmExitClick = { confirmExitCalls++ },
				isSwipeBackNavigationEnabled = false,
				browserRepository = RecordingBrowserRepository(),
				deviceInfoRepository = FakeDeviceInfoRepository(hasCamera = false),
				reviewRepository = RecordingReviewRepository(),
				updateRepository = FakeUpdateRepository(),
				viewModel = createMainViewModel()
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(BaseUiTags.ConfirmationDialogPositiveButton)
				.fetchSemanticsNodes().isNotEmpty()
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogPositiveButton)
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			confirmExitCalls > 0
		}

		assertTrue(confirmExitCalls > 0)
	}

}
