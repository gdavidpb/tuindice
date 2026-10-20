package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardChipItem
import com.gdavidpb.tuindice.assistant.presentation.model.CardChipTone
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.EvaluationListComponent
import com.gdavidpb.tuindice.chatcontract.component.EvaluationRow
import com.gdavidpb.tuindice.chatcontract.component.RequiredGradeComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectPointsComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectPointsRow
import com.gdavidpb.tuindice.chatcontract.component.WeekLoad
import com.gdavidpb.tuindice.chatcontract.component.WeeklyLoadComponent
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_chip_heavy
import tuindice.assistant.generated.resources.assistant_card_b_count_evaluation_many
import tuindice.assistant.generated.resources.assistant_card_b_count_evaluation_one
import tuindice.assistant.generated.resources.assistant_card_b_evaluation_no_grade
import tuindice.assistant.generated.resources.assistant_card_b_fraction
import tuindice.assistant.generated.resources.assistant_card_b_joined
import tuindice.assistant.generated.resources.assistant_card_b_points_description
import tuindice.assistant.generated.resources.assistant_card_b_week
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EvaluacionesComponentItemMappingTest {
	@Test
	fun evaluationList_showsTheGradeOverTheMaximum_orNoGradeWhenThereIsNone() {
		val item = EvaluationListComponent(
			id = "e1",
			fallbackText = "Evaluaciones",
			items = listOf(
				EvaluationRow(
                    "ev-1",
                    "IS-101",
                    "Parcial 1",
                    dateMillis = null,
                    scheduleMode = "fixed",
                    grade = 14.0,
                    maxGrade = 20.0,
                    state = "graded"
                ),
				EvaluationRow(
                    "ev-2",
                    "IS-101",
                    "Parcial 2",
                    dateMillis = null,
                    scheduleMode = "fixed",
                    grade = null,
                    maxGrade = 20.0,
                    state = "pending"
                )
			)
		).toEvaluationListItem()

		assertEquals(
			UiText.Resource(Res.string.assistant_card_b_fraction, listOf("14", "20")),
			item.rows[0].gradeText
		)
		assertEquals(
			UiText.Resource(Res.string.assistant_card_b_evaluation_no_grade, listOf("20")),
			item.rows[1].gradeText
		)
		assertEquals(UiText.Raw("Parcial 1"), item.rows[0].nameText)
		assertNull(evaluationStateChip("unknown"))
	}

	@Test
	fun requiredGrade_isTheRequiredPointsWhenTheTargetAsksForThem_andTheRemainingOtherwise() {
		val withRequired = RequiredGradeComponent(
			id = "r1",
			fallbackText = "Meta",
			subjectCode = "IS-101",
			subjectName = "Programación I",
			targetGrade = 15,
			requiredPoints = 25.0,
			remainingPoints = 40.0,
			feasible = true
		).toRequiredGradeItem()
		val withoutRequired = RequiredGradeComponent(
			id = "r2",
			fallbackText = "Meta",
			subjectCode = "IS-101",
			subjectName = "Programación I",
			targetGrade = 15,
			requiredPoints = null,
			remainingPoints = 40.0,
			feasible = false
		).toRequiredGradeItem()

		assertEquals(UiText.Raw("25"), withRequired.primaryFigure.value)
		assertEquals(UiText.Raw("40"), withRequired.secondaryFigure?.value)
		assertEquals(UiText.Raw("40"), withoutRequired.primaryFigure.value)
		assertNull(withoutRequired.secondaryFigure)
	}

	@Test
	fun weeklyLoad_writesTheCountAndThePointsOfEachWeek() {
		val item = WeeklyLoadComponent(
			id = "w1",
			fallbackText = "Carga",
			weeks = listOf(WeekLoad(week = 3, count = 1, points = 20.0, heavy = false))
		).toWeeklyLoadItem()

		assertEquals(
			UiText.Resource(
				Res.string.assistant_card_b_joined,
				listOf(
					chatCardCountText(
                        1,
                        Res.string.assistant_card_b_count_evaluation_one,
                        Res.string.assistant_card_b_count_evaluation_many
                    ),
					chatCardPointsText(20.0)
				)
			),
			item.weeks.single().detailText
		)
		assertEquals(UiText.Resource(Res.string.assistant_card_b_week, listOf(3)), item.weeks.single().weekText)
		assertNull(item.weeks.single().chip)
	}

	@Test
	fun weeklyLoad_aHeavyWeekIsAWarningChip() {
		val item = WeeklyLoadComponent(
			id = "w2",
			fallbackText = "Carga",
			weeks = listOf(WeekLoad(week = 4, count = 3, points = 60.0, heavy = true))
		).toWeeklyLoadItem()

		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_b_chip_heavy), CardChipTone.Warning),
			item.weeks.single().chip
		)
	}

	@Test
	fun subjectPoints_barSplitsWhatIsEarnedAndWhatIsEvaluatedOverTheTotal_andReadsItsPoints() {
		val item = SubjectPointsComponent(
			id = "p1",
			fallbackText = "Puntos",
			rows = listOf(SubjectPointsRow("IS-101", "Programación I", earned = 30.0, evaluated = 50.0, total = 100.0))
		).toSubjectPointsItem()
		val row = item.rows.single()

		assertEquals(0.3f, row.earnedFraction)
		assertEquals(0.5f, row.evaluatedFraction)
		assertEquals(
			UiText.Resource(Res.string.assistant_card_b_fraction, listOf("30", "50")),
			row.pointsText
		)
		assertEquals(
			UiText.Resource(Res.string.assistant_card_b_points_description, listOf("30", "100", "50")),
			row.description
		)
	}
}
