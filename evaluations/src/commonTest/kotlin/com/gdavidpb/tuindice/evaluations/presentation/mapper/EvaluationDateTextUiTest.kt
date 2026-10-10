package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.presentation.utils.formatMonthYear
import com.gdavidpb.tuindice.evaluations.presentation.utils.toEvaluationEpochMillis
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_TERM
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDate
import kotlin.test.Test

// The Spanish every date of Evaluations reads as, resolved the way the screens do it: asString()
// inside a composable. It reads resources, so it runs on the iOS host only.
@OptIn(ExperimentalTestApi::class)
class EvaluationDateTextUiTest {
	@Test
	fun when_evaluationIsDated_then_itemDateReadsTheCapitalizedWeekdayAndTheNumericDate() = runTuIndiceUiTest {
		val mapping = getEvaluationItemMapping()

		assertResolvedTexts(
			"Jueves — 15/01/26" to mapping.dateText(evaluationOn(LocalDate(2026, 1, 15))),
			"Miércoles — 04/02/26" to mapping.dateText(evaluationOn(LocalDate(2026, 2, 4))),
			"Sábado — 17/01/26" to mapping.dateText(evaluationOn(LocalDate(2026, 1, 17)))
		)
	}

	@Test
	fun when_evaluationIsContinuousOrUndated_then_itsDateTextReadsTheNoDateLabel() = runTuIndiceUiTest {
		val mapping = getEvaluationItemMapping()
		val continuous = DEFAULT_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.CONTINUOUS,
			date = LocalDate(2026, 1, 15).toEvaluationEpochMillis()
		)
		val undated = DEFAULT_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.DATED,
			date = null
		)

		assertResolvedTexts(
			"Evaluación continua" to mapping.dateText(continuous),
			"Evaluación continua" to mapping.dateText(undated)
		)
	}

	@Test
	fun when_dateIsPicked_then_readsTheCapitalizedShortWeekdayAndTheNumericDate() = runTuIndiceUiTest {
		assertResolvedTexts(
			"Jue — 15/01/26" to LocalDate(2026, 1, 15).toEvaluationEpochMillis().formatAsShortDayOfWeekAndDate(),
			"Sáb — 17/01/26" to LocalDate(2026, 1, 17).toEvaluationEpochMillis().formatAsShortDayOfWeekAndDate()
		)
	}

	@Test
	fun when_calendarMonthIsDisplayed_then_readsTheMonthInLowerCaseAndTheYear() = runTuIndiceUiTest {
		assertResolvedTexts(
			"abril 2026" to LocalDate(2026, 4, 1).formatMonthYear(),
			"diciembre 2026" to LocalDate(2026, 12, 31).formatMonthYear()
		)
	}

	@Test
	fun when_weekStripDaysAreResolved_then_readTheShortWeekdayInUpperCaseFromMondayToSunday() = runTuIndiceUiTest {
		val week = buildEvaluationsWeekItems(
			currentTerm = DEFAULT_EVALUATION_TERM,
			evaluations = emptyList(),
			weekLabelPattern = "Semana %1${'$'}d",
			continuousLabel = "Continuas",
			currentDate = LocalDate(2026, 5, 21)
		).first { item -> item.key == EvaluationsWeekKey.Academic(8) }

		assertResolvedTexts(
			*listOf("LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM")
				.zip(week.days.map { day -> day.weekdayText })
				.toTypedArray()
		)
	}

	private fun evaluationOn(date: LocalDate): Evaluation {
		return DEFAULT_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.DATED,
			date = date.toEvaluationEpochMillis()
		)
	}

	private fun ComposeUiTest.assertResolvedTexts(vararg cases: Pair<String, UiText>) {
		setTuIndiceTestContent {
			cases.forEachIndexed { index, (_, text) ->
				Text(
					modifier = Modifier.testTag(resolvedTextTag(index)),
					text = text.asString()
				)
			}
		}

		cases.forEachIndexed { index, (expected, _) ->
			// Resources resolve asynchronously off Android: wait for the text, then read the node.
			waitUntil(timeoutMillis = RESOURCE_TIMEOUT_MILLIS) {
				onAllNodesWithText(expected).fetchSemanticsNodes().isNotEmpty()
			}

			onNodeWithTag(resolvedTextTag(index)).assertTextEquals(expected)
		}
	}

	private companion object {
		const val RESOURCE_TIMEOUT_MILLIS = 5_000L

		fun resolvedTextTag(index: Int) = "evaluation_date_text_resolved_$index"
	}
}
