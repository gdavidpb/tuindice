package com.gdavidpb.tuindice.base.data.source.event

import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReportingBreadcrumbEventSubscriberTest {
	@Test
	fun screenView_logsBreadcrumbAndUpdatesCurrentScreen() {
		val reportingRepository = RecordingReportingRepository()
		val subscriber = ReportingBreadcrumbEventSubscriber(reportingRepository)

		subscriber.onEvent(AppEvent.ScreenView(source = "summary"))

		assertEquals(
			listOf("mvi screen_view source=summary screen_name=summary"),
			reportingRepository.loggedMessages
		)
		assertEquals("summary", reportingRepository.customKeys["mvi.current_screen"])
	}

	@Test
	fun action_logsBreadcrumbAndUpdatesLastAction() {
		val reportingRepository = RecordingReportingRepository()
		val subscriber = ReportingBreadcrumbEventSubscriber(reportingRepository)

		subscriber.onEvent(AppEvent.Action(source = "summary", action = "RefreshSummary"))

		assertEquals(
			listOf("mvi app_action source=summary action=RefreshSummary"),
			reportingRepository.loggedMessages
		)
		assertEquals("summary", reportingRepository.customKeys["mvi.current_screen"])
		assertEquals("RefreshSummary", reportingRepository.customKeys["mvi.last_action"])
	}

	@Test
	fun state_logsBreadcrumbAndUpdatesCurrentState() {
		val reportingRepository = RecordingReportingRepository()
		val subscriber = ReportingBreadcrumbEventSubscriber(reportingRepository)

		subscriber.onEvent(AppEvent.State(source = "summary", state = "Content"))

		assertEquals(
			listOf("mvi app_state source=summary state=Content"),
			reportingRepository.loggedMessages
		)
		assertEquals("summary", reportingRepository.customKeys["mvi.current_screen"])
		assertEquals("Content", reportingRepository.customKeys["mvi.current_state"])
	}

	@Test
	fun effect_logsBreadcrumbAndUpdatesLastEffect() {
		val reportingRepository = RecordingReportingRepository()
		val subscriber = ReportingBreadcrumbEventSubscriber(reportingRepository)

		subscriber.onEvent(AppEvent.Effect(source = "summary", effect = "ShowSnackBar"))

		assertEquals(
			listOf("mvi app_effect source=summary effect=ShowSnackBar"),
			reportingRepository.loggedMessages
		)
		assertEquals("summary", reportingRepository.customKeys["mvi.current_screen"])
		assertEquals("ShowSnackBar", reportingRepository.customKeys["mvi.last_effect"])
	}

	@Test
	fun stateChangingTransition_logsBreadcrumbAndUpdatesTransitionContext() {
		val reportingRepository = RecordingReportingRepository()
		val subscriber = ReportingBreadcrumbEventSubscriber(reportingRepository)

		subscriber.onEvent(
			AppEvent.Transition(
				source = "summary",
				from = "Loading",
				event = "RefreshSucceeded",
				to = "Content"
			)
		)

		assertEquals(
			listOf(
				"mvi app_transition source=summary from=Loading event=RefreshSucceeded to=Content"
			),
			reportingRepository.loggedMessages
		)
		assertEquals("summary", reportingRepository.customKeys["mvi.current_screen"])
		assertEquals("Content", reportingRepository.customKeys["mvi.current_state"])
		assertEquals(
			"from=Loading event=RefreshSucceeded to=Content",
			reportingRepository.customKeys["mvi.last_transition"]
		)
	}

	@Test
	fun selfLoopTransition_isIgnoredLikeAnalytics() {
		val reportingRepository = RecordingReportingRepository()
		val subscriber = ReportingBreadcrumbEventSubscriber(reportingRepository)

		subscriber.onEvent(
			AppEvent.Transition(
				source = "sign_in",
				from = "Idle",
				event = "SetPassword",
				to = "Idle"
			)
		)

		assertTrue(reportingRepository.loggedMessages.isEmpty())
		assertTrue(reportingRepository.customKeys.isEmpty())
	}

	@Test
	fun invalidTransition_logsBreadcrumbAndUpdatesLastInvalidTransition() {
		val reportingRepository = RecordingReportingRepository()
		val subscriber = ReportingBreadcrumbEventSubscriber(reportingRepository)

		subscriber.onEvent(
			AppEvent.InvalidTransition(
				source = "summary",
				from = "Loading",
				event = "ObserveSummary"
			)
		)

		assertEquals(
			listOf(
				"mvi app_invalid_transition source=summary from=Loading event=ObserveSummary"
			),
			reportingRepository.loggedMessages
		)
		assertEquals("summary", reportingRepository.customKeys["mvi.current_screen"])
		assertEquals(
			"from=Loading event=ObserveSummary",
			reportingRepository.customKeys["mvi.last_invalid_transition"]
		)
	}

	@Test
	fun breadcrumbsDoNotIncludeSensitivePayloadKeys() {
		val reportingRepository = RecordingReportingRepository()
		val subscriber = ReportingBreadcrumbEventSubscriber(reportingRepository)

		subscriber.onEvent(AppEvent.Action(source = "sign_in", action = "SubmitCredentials"))

		val breadcrumb = reportingRepository.loggedMessages.single()
		assertFalse(breadcrumb.contains("password="))
		assertFalse(breadcrumb.contains("usbId="))
	}
}
