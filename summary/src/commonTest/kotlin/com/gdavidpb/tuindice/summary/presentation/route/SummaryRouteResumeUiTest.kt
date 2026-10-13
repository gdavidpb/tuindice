package com.gdavidpb.tuindice.summary.presentation.route

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.gdavidpb.tuindice.summary.testing.ControllableVisibleAcademicRecordRepository
import com.gdavidpb.tuindice.summary.testing.HUB_THURSDAY
import com.gdavidpb.tuindice.summary.testing.SleepingScheduleClockRepository
import com.gdavidpb.tuindice.summary.testing.createObserveSummaryHubUseCase
import com.gdavidpb.tuindice.summary.testing.createSummaryViewModelWithHub
import com.gdavidpb.tuindice.summary.testing.hubAttempt
import com.gdavidpb.tuindice.summary.testing.hubMeeting
import com.gdavidpb.tuindice.summary.testing.hubRecord
import com.gdavidpb.tuindice.summary.testing.hubTerm
import com.gdavidpb.tuindice.summary.testing.scheduleNow
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

/** What the route does when the screen comes back in front: the hub reads the clock again. */
@OptIn(ExperimentalTestApi::class)
class SummaryRouteResumeUiTest {
	@Test
	fun when_theScreenIsResumedAfterTheClockMoved_then_theNextClassBecomesTheOneInProgress() = runTuIndiceUiTest {
		// A clock that, like the real one in a suspended process, says its moment once and then sleeps.
		val clock = SleepingScheduleClockRepository(reading = scheduleNow(minuteOfDay = 7 * 60))
		val lifecycleOwner = ResumeTestLifecycleOwner()
		val viewModel = createViewModel(clock)

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
				SummaryRoute(
					onNavigateToProfilePictureSettingsDialog = { _, _ -> },
					onNavigateToRemoveProfilePictureConfirmationDialog = {},
					showSnackBar = {},
					viewModel = viewModel
				)
			}
		}

		runOnIdle {
			lifecycleOwner.handleEvent(Lifecycle.Event.ON_CREATE)
			lifecycleOwner.handleEvent(Lifecycle.Event.ON_START)
			lifecycleOwner.handleEvent(Lifecycle.Event.ON_RESUME)
		}

		// At seven the class of the morning is the next one: its lead is the day, not "En curso".
		waitUntil(timeoutMillis = 5_000) { onAllNodesWithText("Hoy").fetchSemanticsNodes().isNotEmpty() }
		assertNoText("En curso")

		// The student goes to other apps for a while and comes back at ten: nothing ticked in between.
		clock.reading = scheduleNow(minuteOfDay = 10 * 60)
		runOnIdle {
			lifecycleOwner.handleEvent(Lifecycle.Event.ON_PAUSE)
			lifecycleOwner.handleEvent(Lifecycle.Event.ON_RESUME)
		}

		waitUntil(timeoutMillis = 5_000) { onAllNodesWithText("En curso").fetchSemanticsNodes().isNotEmpty() }
	}

	@Test
	fun when_theScreenIsOpened_then_itsOwnFirstResumeDoesNotReadTheClockAgain() = runTuIndiceUiTest {
		val clock = SleepingScheduleClockRepository(reading = scheduleNow(minuteOfDay = 7 * 60))
		val lifecycleOwner = ResumeTestLifecycleOwner()
		val viewModel = createViewModel(clock)

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
				SummaryRoute(
					onNavigateToProfilePictureSettingsDialog = { _, _ -> },
					onNavigateToRemoveProfilePictureConfirmationDialog = {},
					showSnackBar = {},
					viewModel = viewModel
				)
			}
		}

		runOnIdle {
			lifecycleOwner.handleEvent(Lifecycle.Event.ON_CREATE)
			lifecycleOwner.handleEvent(Lifecycle.Event.ON_START)
			lifecycleOwner.handleEvent(Lifecycle.Event.ON_RESUME)
		}

		waitUntil(timeoutMillis = 5_000) { onAllNodesWithText("Hoy").fetchSemanticsNodes().isNotEmpty() }
		waitForIdle()

		// The resume the screen is born in is not a return: the observation already read the clock.
		assertEquals(1, clock.observations)
	}

	// A Thursday with a class from half past nine to half past eleven, read at the clock's moment.
	private fun createViewModel(clock: SleepingScheduleClockRepository) = createSummaryViewModelWithHub(
		hub = createObserveSummaryHubUseCase(
			record = ControllableVisibleAcademicRecordRepository(
				hubRecord(
					hubTerm(
						attempts = listOf(
							hubAttempt(
								code = "CI5437",
								schedule = listOf(hubMeeting(day = HUB_THURSDAY, start = 3, end = 4))
							)
						)
					)
				)
			),
			clock = clock
		)
	)

	private fun ComposeUiTest.assertNoText(text: String) {
		check(onAllNodesWithText(text).fetchSemanticsNodes().isEmpty()) { "'$text' must not be on screen yet" }
	}
}

private class ResumeTestLifecycleOwner : LifecycleOwner {
	private val registry = LifecycleRegistry(this)

	override val lifecycle: Lifecycle
		get() = registry

	fun handleEvent(event: Lifecycle.Event) {
		registry.handleLifecycleEvent(event)
	}
}
