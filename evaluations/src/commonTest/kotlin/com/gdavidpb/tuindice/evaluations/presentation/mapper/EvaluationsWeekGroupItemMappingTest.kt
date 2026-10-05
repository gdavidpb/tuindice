package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Quiz
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateGroup
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationHighlightTone
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
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

	// The crash this fixes: an evaluation pointing at a subject that is not among the current
	// term's used to throw from the mapper, inside the machine's job, and take the process down.
	@Test
	fun toEvaluationItemList_leavesOutAnEvaluationWhoseSubjectIsGone() {
		val orphan = DEFAULT_PENDING_EVALUATION.copy(
			id = "orphan-evaluation",
			attemptId = "attempt-that-is-gone"
		)

		val groups = listOf(orphan, DEFAULT_PENDING_EVALUATION).toEvaluationItemList(
			mapping = testEvaluationItemMapping(),
			attempts = listOf(DEFAULT_EVALUATION_SUBJECT)
		)

		assertEquals(
			listOf(DEFAULT_PENDING_EVALUATION.id),
			groups.flatMap { group -> group.items }.map { item -> item.evaluationId }
		)
	}

	@Test
	fun toEvaluationItemList_whenEverySubjectIsGone_isEmptyInsteadOfThrowing() {
		val date = LocalDate(2026, 5, 21)
		val orphans = listOf(
			DEFAULT_PENDING_EVALUATION.copy(id = "orphan-1", attemptId = "gone-1"),
			DEFAULT_PENDING_EVALUATION.copy(
				id = "orphan-2",
				attemptId = "gone-2",
				date = date.toEvaluationEpochMillis()
			)
		)

		// No date header is left behind with nothing under it.
		assertEquals(
			emptyList(),
			orphans.toEvaluationItemList(
				mapping = testEvaluationItemMapping(),
				attempts = listOf(DEFAULT_EVALUATION_SUBJECT)
			)
		)
		assertEquals(
			emptyList(),
			orphans.toEvaluationItemList(mapping = testEvaluationItemMapping(), attempts = emptyList())
		)
	}

	@Test
	fun toEvaluationItemList_doesNotCountAGoneSubjectInTheOrdinals() {
		val date = LocalDate(2026, 5, 21)
		val orphan = DEFAULT_PENDING_EVALUATION.copy(
			id = "orphan-evaluation",
			attemptId = "attempt-that-is-gone",
			date = date.toEvaluationEpochMillis()
		)
		val listed = DEFAULT_PENDING_EVALUATION.copy(
			date = LocalDate(2026, 5, 22).toEvaluationEpochMillis()
		)

		val item = listOf(orphan, listed)
			.toEvaluationItemList(
				mapping = testEvaluationItemMapping(),
				attempts = listOf(DEFAULT_EVALUATION_SUBJECT)
			)
			.flatMap { group -> group.items }
			.single()

		assertEquals("${listed.type.name} 1", item.nameText)
	}

	@Test
	fun toEvaluationsWeekGroupItemList_leavesOutTheWeekOfAnEvaluationWhoseSubjectIsGone() {
		val date = LocalDate(2026, 5, 21)
		val orphan = DEFAULT_PENDING_EVALUATION.copy(
			id = "orphan-evaluation",
			attemptId = "attempt-that-is-gone",
			date = date.toEvaluationEpochMillis()
		)
		val weekItems = buildEvaluationsWeekItems(
			currentTerm = DEFAULT_EVALUATION_TERM,
			evaluations = listOf(orphan),
			weekLabelPattern = "Semana %1${'$'}d",
			continuousLabel = "Continuas",
			currentDate = date
		)

		val groups = weekItems.toEvaluationsWeekGroupItemList(
			evaluations = listOf(orphan),
			currentTerm = DEFAULT_EVALUATION_TERM,
			attempts = listOf(DEFAULT_EVALUATION_SUBJECT),
			mapping = testEvaluationItemMapping()
		)

		assertEquals(emptyList(), groups)
	}

	@Test
	fun listedUnder_keepsOnlyTheEvaluationsOfTheGivenSubjects() {
		val orphan = DEFAULT_PENDING_EVALUATION.copy(
			id = "orphan-evaluation",
			attemptId = "attempt-that-is-gone"
		)

		assertEquals(
			listOf(DEFAULT_PENDING_EVALUATION),
			listOf(orphan, DEFAULT_PENDING_EVALUATION).listedUnder(listOf(DEFAULT_EVALUATION_SUBJECT))
		)
		assertEquals(
			emptyList(),
			listOf(orphan, DEFAULT_PENDING_EVALUATION).listedUnder(emptyList())
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
