package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Quiz
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateGroup
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationHighlightTone
import com.gdavidpb.tuindice.evaluations.presentation.utils.toEvaluationEpochMillis
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_TERM
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvaluationsWeekGroupItemMappingTest {
	@Test
	fun toEvaluationsWeekGroupItemList_groupsContinuousEvaluationsOnlyUnderContinuousKey() = runTest {
		val date = LocalDate(2026, 5, 21)
		val continuousEvaluation = DEFAULT_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.CONTINUOUS,
			date = date.toEvaluationEpochMillis()
		)
		val datedEvaluation = DEFAULT_PENDING_EVALUATION.copy(
			id = "dated-evaluation",
			date = date.toEvaluationEpochMillis()
		)
		val weekItems = buildEvaluationsWeekItems(
			currentTerm = DEFAULT_EVALUATION_TERM,
			evaluations = listOf(continuousEvaluation, datedEvaluation),
			weekLabelPattern = "Semana %1${'$'}d",
			continuousLabel = "Continuas",
			currentDate = date
		)

		val groups = weekItems.toEvaluationsWeekGroupItemList(
			evaluations = listOf(continuousEvaluation, datedEvaluation),
			currentTerm = DEFAULT_EVALUATION_TERM,
			attempts = listOf(DEFAULT_EVALUATION_SUBJECT),
			mapping = testEvaluationItemMapping()
		)

		val continuousGroup = groups.first { group -> group.key == EvaluationsWeekKey.Continuous }
		val academicGroup = groups.first { group -> group.key == EvaluationsWeekKey.Academic(8) }

		assertEquals("Continuas", continuousGroup.title)
		assertEquals(
			listOf(continuousEvaluation.id),
			continuousGroup.groups.flatMap { group -> group.items }.map { item -> item.evaluationId }
		)
		assertTrue(
			academicGroup.groups
				.flatMap { group -> group.items }
				.none { item -> item.evaluationId == continuousEvaluation.id }
		)
	}
}

private fun testEvaluationItemMapping() = EvaluationItemMapping(
	evaluationName = { type, ordinal -> "${type.name} $ordinal" },
	typeLabel = { type -> type.name },
	gradesCompleted = { grade, maxGrade -> "${grade ?: 0.0} / $maxGrade" },
	gradesPending = { maxGrade -> "Pendiente / $maxGrade" },
	gradesOverdue = { maxGrade -> "Sin nota / $maxGrade" },
	scoreGrade = { grade, maxGrade -> "${grade ?: 0.0} / $maxGrade" },
	statusLabel = { state -> state.name },
	typeIcon = { Icons.Outlined.Quiz },
	dateIcon = { Icons.Outlined.CalendarToday },
	gradesIcon = { Icons.Outlined.AssignmentTurnedIn },
	dateGroupTitle = { group ->
		when (group) {
			EvaluationDateGroup.Continuous -> "Evaluacion continua"
			else -> "Fecha"
		}
	},
	dateHeaderText = { evaluation ->
		if (evaluation.scheduleMode == EvaluationScheduleMode.CONTINUOUS) "Evaluacion continua" else "Fecha"
	},
	dateText = { evaluation ->
		if (evaluation.scheduleMode == EvaluationScheduleMode.CONTINUOUS) "Evaluacion continua" else "Fecha"
	},
	highlightTone = { state ->
		when (state) {
			EvaluationState.OVERDUE -> EvaluationHighlightTone.Error
			EvaluationState.COMPLETED -> EvaluationHighlightTone.Success
			else -> EvaluationHighlightTone.Neutral
		}
	}
)
