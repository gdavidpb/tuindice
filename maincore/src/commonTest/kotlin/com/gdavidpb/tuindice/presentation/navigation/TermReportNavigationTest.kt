package com.gdavidpb.tuindice.presentation.navigation

import com.gdavidpb.tuindice.assistant.presentation.navigation.AssistantDestination
import com.gdavidpb.tuindice.auth.presentation.navigation.AuthDestination
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantNavTarget
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.pensum.presentation.navigation.PensumDestination
import com.gdavidpb.tuindice.presentation.mapper.toChatNavTarget
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.testing.createTestNavigator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

// A notification of a term report opens the report over the summary tab, once the student is signed in.
class TermReportNavigationTest {

	@Test
	fun aTappedReport_opensOverTheSummaryTab_andItsBackReturnsToTheSummary() {
		val navigator = createTestNavigator(startKey = PensumDestination.Pensum)

		navigator.showTermReport(insightId = "term-1:2")

		assertEquals(MainSection.SUMMARY, navigator.currentTab)
		assertEquals(
			listOf(PensumDestination.Pensum, SummaryDestination.Summary, SummaryDestination.TermReport(insightId = "term-1:2")),
			navigator.backStack
		)
	}

	@Test
	fun aTappedReport_waitsForTheSignIn() {
		val navigator = createTestNavigator(startKey = AuthDestination.SignIn)

		navigator.showTermReport(insightId = "term-1:2")

		assertFalse(navigator.backStack.contains(SummaryDestination.TermReport(insightId = "term-1:2")))
	}

	@Test
	fun theLinkOfTheSummary_pushesTheReportOverTheSummaryTab() {
		val navigator = createTestNavigator(startKey = SummaryDestination.Summary)
		val navigation = navigator.toSummaryNavDependencies(
			onNavigateToEvaluationsWeek = { _, _ -> },
			onNavigateToRecordAttempt = {}
		)

		navigation.screens.onNavigateToTermReport("term-1:2")

		assertEquals(MainSection.SUMMARY, navigator.currentTab)
		assertEquals(
			listOf(SummaryDestination.Summary, SummaryDestination.TermReport(insightId = "term-1:2")),
			navigator.backStack
		)
	}

	@Test
	fun aTappedReport_closesTheChatOfTheTabTheStudentIsOn_andNoSheetIsLeftBehind() {
		val navigator = createTestNavigator(startKey = PensumDestination.Pensum)
		navigator.switchTab(MainSection.RECORD)
		navigator.push(AssistantDestination.Chat(context = null))

		navigator.showTermReport(insightId = "term-1:2")

		assertEquals(MainSection.SUMMARY, navigator.currentTab)
		assertEquals(
			listOf(PensumDestination.Pensum, SummaryDestination.Summary, SummaryDestination.TermReport(insightId = "term-1:2")),
			navigator.backStack
		)

		navigator.switchTab(MainSection.RECORD)

		assertFalse(navigator.backStack.any { key -> key is AssistantDestination.Chat })
	}

	@Test
	fun everyTargetOfTheReportOpensItsChatScreen() {
		assertEquals(
			NavTarget.entries.toSet(),
			AssistantNavTarget.entries.map { target -> target.toChatNavTarget() }.toSet()
		)
	}
}
