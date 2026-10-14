package com.gdavidpb.tuindice.summary.presentation.route

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryNavDependencies
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryScreenLinks
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

// Each effect that leads to a screen asks for its own connection of the host, and the effects the route answers itself
// ask for none.
class SummaryEffectNavigationTest {
	@Test
	fun when_theScheduleEffectIsLed_then_theScheduleSheetIsOpened() {
		assertEquals(listOf("schedule"), leadingTo(Summary.Effect.NavigateToSchedule))
	}

	@Test
	fun when_theRecordEffectIsLed_then_theRecordSectionIsOpened() {
		assertEquals(listOf("section:${MainSection.RECORD.name}"), leadingTo(Summary.Effect.NavigateToRecord))
	}

	@Test
	fun when_thePensumEffectIsLed_then_thePensumSectionIsOpened() {
		assertEquals(listOf("section:${MainSection.PENSUM.name}"), leadingTo(Summary.Effect.NavigateToPensum))
	}

	@Test
	fun when_theEvaluationsEffectIsLed_then_theEvaluationsSectionIsOpenedOnNoWeek() {
		assertEquals(
			listOf("section:${MainSection.EVALUATIONS.name}"),
			leadingTo(Summary.Effect.NavigateToEvaluations)
		)
	}

	@Test
	fun when_theStatisticsEffectIsLed_then_theStatisticsScreenIsPushed() {
		assertEquals(listOf("statistics"), leadingTo(Summary.Effect.NavigateToStatistics))
	}

	@Test
	fun when_anEvaluationsWeekEffectIsLed_then_theWeekAndItsEvaluationAreAsked() {
		val effect = Summary.Effect.NavigateToEvaluationsWeek(
			weekStart = LocalDate(2026, 10, 5),
			evaluationId = "evaluation-1"
		)

		assertEquals(listOf("week:2026-10-05:evaluation-1"), leadingTo(effect))
	}

	@Test
	fun when_aRecordAttemptEffectIsLed_then_theAttemptIsAsked() {
		val effect = Summary.Effect.NavigateToRecordAttempt(attemptId = "attempt-1")

		assertEquals(listOf("attempt:attempt-1"), leadingTo(effect))
	}

	@Test
	fun when_theInvitationEffectsAreLed_then_theirFormsAreOpened() {
		assertEquals(listOf("form:AddEvaluation"), leadingTo(Summary.Effect.NavigateToAddEvaluation))
		assertEquals(listOf("form:CreateTerm"), leadingTo(Summary.Effect.NavigateToCreateTerm))
	}

	@Test
	fun when_anEffectTheRouteAnswersItself_then_theHostIsAskedForNothing() {
		val effects = listOf(
			Summary.Effect.OpenCamera,
			Summary.Effect.OpenPicker,
			Summary.Effect.ShowSnackBar(message = "message"),
			Summary.Effect.ShowRemoveProfilePictureConfirmationDialog,
			Summary.Effect.ShowProfilePictureSettingsDialog(showRemove = false, isCameraAvailable = true)
		)

		effects.forEach { effect -> assertEquals(emptyList<String>(), leadingTo(effect)) }
	}

	private fun leadingTo(effect: Summary.Effect): List<String> {
		val calls = mutableListOf<String>()

		recordingNavigation(calls).leadTo(effect)

		return calls
	}

	private fun recordingNavigation(calls: MutableList<String>): SummaryNavDependencies {
		return SummaryNavDependencies(
			onNavigateToUpdatePassword = { calls += "password" },
			screens = SummaryScreenLinks(
				onNavigateToSection = { section -> calls += "section:${section.name}" },
				onNavigateToEvaluationsWeek = { weekStart, evaluationId -> calls += "week:$weekStart:$evaluationId" },
				onNavigateToRecordAttempt = { attemptId -> calls += "attempt:$attemptId" },
				onNavigateToStatistics = { calls += "statistics" }
			),
			onOpenSchedule = { calls += "schedule" },
			onOpenForm = { form -> calls += "form:${form.name}" }
		)
	}
}
