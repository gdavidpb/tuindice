package com.gdavidpb.tuindice.presentation.navigation

import com.gdavidpb.tuindice.auth.presentation.navigation.AuthDestination
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsDestination
import com.gdavidpb.tuindice.pensum.presentation.navigation.PensumDestination
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryForm
import com.gdavidpb.tuindice.testing.createTestNavigator
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

// Where the lines of the Summary hub lead, once the host connected them to its navigator.
class SummaryNavigationTest {
	private val navigator = createTestNavigator(startKey = SummaryDestination.Summary)

	@Test
	fun theRecordAndThePensumLinks_changeTheTab() {
		val navigation = navigator.toSummaryNavDependencies(
			onNavigateToEvaluationsWeek = { _, _ -> },
			onNavigateToRecordAttempt = {}
		)

		navigation.screens.onNavigateToSection(MainSection.RECORD)

		assertEquals(MainSection.RECORD, navigator.currentTab)

		navigation.screens.onNavigateToSection(MainSection.PENSUM)

		assertEquals(MainSection.PENSUM, navigator.currentTab)
		assertEquals(listOf(SummaryDestination.Summary, PensumDestination.Pensum), navigator.backStack)
	}

	@Test
	fun theStatisticsLink_pushesTheStatisticsScreenOverTheSummaryTab() {
		val navigation = navigator.toSummaryNavDependencies(
			onNavigateToEvaluationsWeek = { _, _ -> },
			onNavigateToRecordAttempt = {}
		)

		navigation.screens.onNavigateToStatistics()

		assertEquals(MainSection.SUMMARY, navigator.currentTab)
		assertEquals(listOf(SummaryDestination.Summary, SummaryDestination.Statistics), navigator.backStack)
	}

	@Test
	fun theShareTermLink_pushesTheShareSheetOnThatTerm_overTheSummaryTab() {
		val navigation = navigator.toSummaryNavDependencies(
			onNavigateToEvaluationsWeek = { _, _ -> },
			onNavigateToRecordAttempt = {}
		)

		navigation.screens.onNavigateToShareTerm("2026-SEP_DEC")

		assertEquals(MainSection.SUMMARY, navigator.currentTab)
		assertEquals(
			listOf(SummaryDestination.Summary, SummaryDestination.ShareTermDialog(termKey = "2026-SEP_DEC")),
			navigator.backStack
		)
	}

	@Test
	fun theStatisticsRecordLink_leavesTheStatisticsScreenAndGoesToRecord() {
		val navigation = navigator.toSummaryNavDependencies(
			onNavigateToEvaluationsWeek = { _, _ -> },
			onNavigateToRecordAttempt = {}
		)

		navigator.push(SummaryDestination.Statistics)
		navigation.screens.onNavigateToSection(MainSection.RECORD)

		assertEquals(MainSection.RECORD, navigator.currentTab)
		assertEquals(listOf(SummaryDestination.Summary, RecordDestination.Record), navigator.backStack)
	}

	@Test
	fun theStatisticsEvaluationsLink_leavesTheStatisticsScreenAndGoesToEvaluations() {
		val navigation = navigator.toSummaryNavDependencies(
			onNavigateToEvaluationsWeek = { _, _ -> },
			onNavigateToRecordAttempt = {}
		)

		navigator.push(SummaryDestination.Statistics)
		navigation.screens.onNavigateToSection(MainSection.EVALUATIONS)

		assertEquals(MainSection.EVALUATIONS, navigator.currentTab)
		assertEquals(listOf(SummaryDestination.Summary, EvaluationsDestination.Evaluations), navigator.backStack)
	}

	@Test
	fun theScheduleLink_opensTheSheetOverSummary() {
		val navigation = navigator.toSummaryNavDependencies(
			onNavigateToEvaluationsWeek = { _, _ -> },
			onNavigateToRecordAttempt = {}
		)

		navigation.onOpenSchedule()

		assertEquals(MainSection.SUMMARY, navigator.currentTab)
		assertEquals(RecordDestination.ScheduleDialog, navigator.currentKey)
	}

	@Test
	fun theEvaluationsWeek_isLeftToTheHost() {
		val asked = mutableListOf<Pair<LocalDate, String>>()
		val navigation = navigator.toSummaryNavDependencies(
			onNavigateToEvaluationsWeek = { weekStart, evaluationId -> asked += weekStart to evaluationId },
			onNavigateToRecordAttempt = {}
		)

		navigation.screens.onNavigateToEvaluationsWeek(LocalDate(2026, 10, 12), "evaluation-1")

		assertEquals(listOf(LocalDate(2026, 10, 12) to "evaluation-1"), asked)
		assertEquals(MainSection.SUMMARY, navigator.currentTab)
	}

	@Test
	fun theRecordAttemptLink_isLeftToTheHost() {
		val asked = mutableListOf<String>()
		val navigation = navigator.toSummaryNavDependencies(
			onNavigateToEvaluationsWeek = { _, _ -> },
			onNavigateToRecordAttempt = { attemptId -> asked += attemptId }
		)

		navigation.screens.onNavigateToRecordAttempt("attempt-1")

		assertEquals(listOf("attempt-1"), asked)
		assertEquals(MainSection.SUMMARY, navigator.currentTab)
	}

	@Test
	fun theUpdatePasswordLink_opensTheDialog() {
		val navigation = navigator.toSummaryNavDependencies(
			onNavigateToEvaluationsWeek = { _, _ -> },
			onNavigateToRecordAttempt = {}
		)

		navigation.onNavigateToUpdatePassword()

		assertEquals(AuthDestination.UpdatePasswordDialog, navigator.currentKey)
	}

	@Test
	fun theAddEvaluationLink_opensTheFormOverEvaluations_andBackReturnsToSummary() {
		val navigation = navigator.toSummaryNavDependencies(
			onNavigateToEvaluationsWeek = { _, _ -> },
			onNavigateToRecordAttempt = {}
		)

		navigation.onOpenForm(SummaryForm.AddEvaluation)

		assertEquals(MainSection.EVALUATIONS, navigator.currentTab)
		assertEquals(EvaluationsDestination.Evaluation(evaluationId = null), navigator.currentKey)

		navigator.pop()

		assertEquals(MainSection.EVALUATIONS, navigator.currentTab)

		navigator.pop()

		assertEquals(MainSection.SUMMARY, navigator.currentTab)
	}

	@Test
	fun theCreateTermLink_opensTheFormOverRecord_andAnAlreadyOpenFormIsNotPushedTwice() {
		val navigation = navigator.toSummaryNavDependencies(
			onNavigateToEvaluationsWeek = { _, _ -> },
			onNavigateToRecordAttempt = {}
		)

		navigation.onOpenForm(SummaryForm.CreateTerm)
		navigation.onOpenForm(SummaryForm.CreateTerm)

		assertEquals(MainSection.RECORD, navigator.currentTab)
		assertEquals(
			listOf(
				SummaryDestination.Summary,
				RecordDestination.Record,
				RecordDestination.CreateSyntheticTerm()
			),
			navigator.backStack
		)
	}
}
