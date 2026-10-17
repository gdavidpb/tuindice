package com.gdavidpb.tuindice.presentation.navigation

import com.gdavidpb.tuindice.assistant.presentation.navigation.AssistantDestination
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.EnrollmentProofDestination
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsDestination
import com.gdavidpb.tuindice.pensum.presentation.navigation.PensumDestination
import com.gdavidpb.tuindice.presentation.mapper.toTabRootDestination
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination
import com.gdavidpb.tuindice.subjects.presentation.navigation.SubjectsDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.testing.createTestNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AssistantNavigationTest {

	@Test
	fun when_recordScheduleIsRequested_then_theScheduleOpensOverRecord_andTheChatIsClosed() {
		val navigator = navigatorWithChatOver(PensumDestination.Pensum)

		dependenciesOf(navigator).onNavigateTo(NavTarget.RECORD_SCHEDULE, emptyList())

		assertEquals(MainSection.RECORD, navigator.currentTab)
		assertEquals(RecordDestination.ScheduleDialog, navigator.currentKey)
		assertNoChatOnTab(navigator, MainSection.PENSUM)
	}

	@Test
	fun when_statisticsIsRequested_then_theStatisticsOpensOnTheSummaryTab() {
		val navigator = navigatorWithChatOver(PensumDestination.Pensum)

		dependenciesOf(navigator).onNavigateTo(NavTarget.STATISTICS, emptyList())

		assertEquals(MainSection.SUMMARY, navigator.currentTab)
		assertEquals(SummaryDestination.Statistics, navigator.currentKey)
	}

	@Test
	fun when_shareTermIsRequested_then_theShareSheetOpensOverStatistics() = runTest {
		val navigator = navigatorWithChatOver(PensumDestination.Pensum)

		dependenciesOf(navigator, scope = this).onNavigateTo(NavTarget.SHARE_TERM, emptyList())
		advanceUntilIdle()

		assertEquals(MainSection.SUMMARY, navigator.currentTab)
		assertEquals(
			listOf<Destination>(SummaryDestination.Statistics, SummaryDestination.ShareTermDialog(termKey = null)),
			navigator.backStack.takeLast(2)
		)
	}

	@Test
	fun when_shareTermNamesATerm_then_theSheetOpensOnThatTermsKey() = runTest {
		val navigator = navigatorWithChatOver(PensumDestination.Pensum)
		val term = EntityRef(kind = EntityKind.TERM, id = "term-7")

		dependenciesOf(
			navigator = navigator,
			scope = this,
			termKeyOf = { termId -> if (termId == "term-7") "2024-SEP_DEC" else null }
		).onNavigateTo(NavTarget.SHARE_TERM, listOf(term))
		advanceUntilIdle()

		assertEquals(SummaryDestination.ShareTermDialog(termKey = "2024-SEP_DEC"), navigator.currentKey)
		assertNoChatOnTab(navigator, MainSection.PENSUM)
	}

	@Test
	fun when_theRecordDoesNotKnowTheTerm_then_theSheetOpensOnTheNewestTerm() = runTest {
		val navigator = navigatorWithChatOver(PensumDestination.Pensum)
		val term = EntityRef(kind = EntityKind.TERM, id = "term-unknown")

		dependenciesOf(navigator = navigator, scope = this, termKeyOf = { null })
			.onNavigateTo(NavTarget.SHARE_TERM, listOf(term))
		advanceUntilIdle()

		assertEquals(SummaryDestination.ShareTermDialog(termKey = null), navigator.currentKey)
	}

	@Test
	fun when_evaluationsDetailIsRequestedWithAnEvaluation_then_thatEvaluationOpensOnEvaluations() {
		val navigator = navigatorWithChatOver(SummaryDestination.Summary)
		val evaluation = EntityRef(kind = EntityKind.EVALUATION, id = "evaluation-1")

		dependenciesOf(navigator).onNavigateTo(NavTarget.EVALUATIONS_DETAIL, listOf(evaluation))

		assertEquals(MainSection.EVALUATIONS, navigator.currentTab)
		assertEquals(
			EvaluationsDestination.Evaluation(evaluationId = "evaluation-1"),
			navigator.currentKey
		)
	}

	@Test
	fun when_evaluationsDetailIsRequestedWithoutAnEvaluation_then_theEvaluationsTabOpensAlone() {
		val navigator = navigatorWithChatOver(SummaryDestination.Summary)

		dependenciesOf(navigator).onNavigateTo(NavTarget.EVALUATIONS_DETAIL, emptyList())

		assertEquals(MainSection.EVALUATIONS, navigator.currentTab)
		assertEquals(MainSection.EVALUATIONS.toTabRootDestination(), navigator.currentKey)
	}

	@Test
	fun when_subjectDetailIsRequested_then_theSubjectOpensOverPensum() {
		val navigator = navigatorWithChatOver(SummaryDestination.Summary)
		val subject = EntityRef(kind = EntityKind.SUBJECT, id = "MA1111")

		dependenciesOf(navigator).onNavigateTo(NavTarget.SUBJECTS_DETAIL, listOf(subject))

		assertEquals(MainSection.PENSUM, navigator.currentTab)
		assertEquals(SubjectsDestination.SubjectDetail(subjectCode = "MA1111"), navigator.currentKey)
	}

	@Test
	fun when_aboutIsRequested_then_theAboutTabOpens() {
		val navigator = navigatorWithChatOver(SummaryDestination.Summary)

		dependenciesOf(navigator).onNavigateTo(NavTarget.ABOUT, emptyList())

		assertEquals(MainSection.ABOUT, navigator.currentTab)
		assertEquals(MainSection.ABOUT.toTabRootDestination(), navigator.currentKey)
	}

	@Test
	fun when_signInOrPasswordIsRequested_then_theChatStaysAndNothingOpens() {
		val navigator = navigatorWithChatOver(PensumDestination.Pensum)
		val chat = navigator.currentKey

		dependenciesOf(navigator).onNavigateTo(NavTarget.SIGN_IN, emptyList())
		dependenciesOf(navigator).onNavigateTo(NavTarget.UPDATE_PASSWORD, emptyList())

		assertEquals(chat, navigator.currentKey)
		assertEquals(MainSection.PENSUM, navigator.currentTab)
	}

	@Test
	fun when_recordProjectionIsRequested_then_theProjectionIsSaved_andRecordOpens() = runTest {
		val navigator = navigatorWithChatOver(PensumDestination.Pensum)
		var savedMode: RecordViewMode? = null

		dependenciesOf(
			navigator = navigator,
			scope = this,
			writeRecordViewMode = { mode -> savedMode = mode }
		).onNavigateTo(NavTarget.RECORD_PROJECTION, emptyList())
		advanceUntilIdle()

		assertEquals(RecordViewMode.Projection, savedMode)
		assertEquals(MainSection.RECORD, navigator.currentTab)
		assertNoChatOnTab(navigator, MainSection.PENSUM)
	}

	@Test
	fun when_theProjectionIsNotSaved_then_recordStillOpens() = runTest {
		val navigator = navigatorWithChatOver(PensumDestination.Pensum)

		dependenciesOf(
			navigator = navigator,
			scope = this,
			writeRecordViewMode = { _ -> }
		).onNavigateTo(NavTarget.RECORD_PROJECTION, emptyList())
		advanceUntilIdle()

		assertEquals(MainSection.RECORD, navigator.currentTab)
	}

	@Test
	fun when_enrollmentProofIsOpenedFromTheChat_then_theProofOpensOverTheChat() {
		val navigator = navigatorWithChatOver(PensumDestination.Pensum)
		val chat = navigator.currentKey

		dependenciesOf(navigator).onOpenEnrollmentProof()

		assertEquals(EnrollmentProofDestination.EnrollmentProofDialog, navigator.currentKey)
		assertEquals(chat, navigator.backStack[navigator.backStack.lastIndex - 1])
	}

	@Test
	fun when_textIsCopiedFromTheChat_then_itReachesTheClipboard() {
		val navigator = navigatorWithChatOver(PensumDestination.Pensum)
		val copied = mutableListOf<String>()

		dependenciesOf(navigator, copyText = { text -> copied += text }).onCopyText("Tu promedio")

		assertEquals(listOf("Tu promedio"), copied)
	}

	private fun navigatorWithChatOver(startKey: Destination): TuIndiceNavigator {
		return createTestNavigator(startKey = startKey).also { navigator ->
			navigator.openAssistant(AssistantScreenContext(screen = AssistantScreen.PENSUM))
		}
	}

	private fun dependenciesOf(
		navigator: TuIndiceNavigator,
		scope: CoroutineScope = TestScope(),
		writeRecordViewMode: suspend (RecordViewMode) -> Unit = {},
		termKeyOf: suspend (String) -> String? = { null },
		copyText: (String) -> Unit = {}
	) = navigator.toAssistantNavDependencies(
		scope = scope,
		writeRecordViewMode = writeRecordViewMode,
		termKeyOf = termKeyOf,
		copyText = copyText
	)

	// The chat is gone from the tab: back in that tab the sheet is not parked on it.
	private fun assertNoChatOnTab(navigator: TuIndiceNavigator, section: MainSection) {
		val current = navigator.currentTab
		navigator.switchTab(section)
		assertFalse(navigator.backStack.any { key -> key is AssistantDestination.Chat })
		navigator.switchTab(current)
	}
}
