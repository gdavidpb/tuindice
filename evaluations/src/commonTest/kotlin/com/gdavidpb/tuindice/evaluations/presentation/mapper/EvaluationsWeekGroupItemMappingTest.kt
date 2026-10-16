package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Quiz
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.evaluations.domain.mapper.listedUnder
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationHighlightTone
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.presentation.utils.toEvaluationEpochMillis
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_TERM
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
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
	fun toEvaluationItemList_takesTheItemDateTextFromTheMapping() {
		val continuous = DEFAULT_PENDING_EVALUATION.copy(
			id = "continuous-evaluation",
			scheduleMode = EvaluationScheduleMode.CONTINUOUS,
			date = null
		)
		val dated = DEFAULT_PENDING_EVALUATION.copy(
			date = LocalDate(2026, 5, 21).toEvaluationEpochMillis()
		)

		val groups = listOf(continuous, dated).toEvaluationItemList(
			mapping = testEvaluationItemMapping(),
			attempts = listOf(DEFAULT_EVALUATION_SUBJECT)
		)

		// Dated first, then the ones without a date: the order comes from the instant, never
		// from the text of the date.
		assertEquals(
			listOf(DATED_DATE_TEXT, CONTINUOUS_DATE_TEXT),
			groups.flatMap { group -> group.items }.map { item -> item.dateText }
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

	// SEP_DEC 2026 with classes starting on Monday September 28th.
	private val termWithClassesStart = DEFAULT_EVALUATION_TERM.copy(
		periodCode = AcademicTermPeriod.SEP_DEC,
		classesStartDate = LocalDate(2026, 9, 28)
	)

	private fun groupsOf(vararg evaluations: Evaluation, currentDate: LocalDate = LocalDate(2026, 10, 14)) =
		buildEvaluationsWeekItems(
			currentTerm = termWithClassesStart,
			evaluations = evaluations.toList(),
			weekLabelPattern = "Semana %1${'$'}d",
			continuousLabel = "Continuas",
			currentDate = currentDate
		).toEvaluationsWeekGroupItemList(
			evaluations = evaluations.toList(),
			currentTerm = termWithClassesStart,
			attempts = listOf(DEFAULT_EVALUATION_SUBJECT),
			mapping = testEvaluationItemMapping()
		)

	private fun datedOn(id: String, date: LocalDate) = DEFAULT_PENDING_EVALUATION.copy(
		id = id,
		date = date.toEvaluationEpochMillis()
	)

	@Test
	fun toEvaluationsWeekGroupItemList_withARealStart_groupsEachEvaluationUnderTheWeekOfItsDate() {
		val groups = groupsOf(
			datedOn("first-day", LocalDate(2026, 9, 28)),
			datedOn("sunday-of-week-one", LocalDate(2026, 10, 4)),
			datedOn("monday-of-week-two", LocalDate(2026, 10, 5)),
			datedOn("thursday-of-week-three", LocalDate(2026, 10, 15))
		)

		assertEquals(
			mapOf<EvaluationsWeekKey, List<String>>(
				EvaluationsWeekKey.Academic(1) to listOf("first-day", "sunday-of-week-one"),
				EvaluationsWeekKey.Academic(2) to listOf("monday-of-week-two"),
				EvaluationsWeekKey.Academic(3) to listOf("thursday-of-week-three")
			),
			groups.associate { group ->
				group.key to group.groups.flatMap { it.items }.map { item -> item.evaluationId }.sorted()
			}
		)
	}

	// Fixed on purpose: the strip has no such day, and the group holds the evaluation in the nearest week.
	@Test
	fun toEvaluationsWeekGroupItemList_withARealStart_holdsEvaluationsOutsideTheTwelveWeeksInTheNearestOne() {
		val groups = groupsOf(
			datedOn("before", LocalDate(2026, 9, 20)),
			datedOn("after", LocalDate(2027, 2, 1))
		)

		assertEquals(
			mapOf<EvaluationsWeekKey, List<String>>(
				EvaluationsWeekKey.Academic(1) to listOf("before"),
				EvaluationsWeekKey.Academic(12) to listOf("after")
			),
			groups.associate { group ->
				group.key to group.groups.flatMap { it.items }.map { item -> item.evaluationId }
			}
		)
	}

	@Test
	fun theStrip_theGroupAndTheDefaultWeek_agreeOnTheWeekOfAnyDateOfTheTerm() {
		val firstDay = LocalDate(2026, 9, 28)

		(0 until 12 * 7).forEach { offset ->
			val date = firstDay.plus(DatePeriod(days = offset))
			val evaluation = datedOn("on-$date", date)
			val stripWeek = buildEvaluationsWeekItems(
				currentTerm = termWithClassesStart,
				evaluations = listOf(evaluation),
				weekLabelPattern = "Semana %1${'$'}d",
				continuousLabel = "Continuas",
				currentDate = date
			).single { item -> item.days.any { day -> day.isSelected } }.key
			val groupWeek = groupsOf(evaluation, currentDate = date).single().key
			val defaultWeek = defaultEvaluationsWeekKey(
				currentTerm = termWithClassesStart,
				evaluations = listOf(evaluation),
				currentDate = date
			)

			assertEquals(stripWeek, groupWeek, "group of $date")
			assertEquals(stripWeek, defaultWeek, "default of $date")
			assertEquals(EvaluationsWeekKey.Academic(offset / 7 + 1), stripWeek, "week of $date")
		}
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

internal fun testEvaluationItemMapping() = EvaluationItemMapping(
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
	dateText = { evaluation ->
		if (evaluation.scheduleMode == EvaluationScheduleMode.CONTINUOUS) CONTINUOUS_DATE_TEXT else DATED_DATE_TEXT
	},
	highlightTone = { state ->
		when (state) {
			EvaluationState.OVERDUE -> EvaluationHighlightTone.Error
			EvaluationState.COMPLETED -> EvaluationHighlightTone.Success
			else -> EvaluationHighlightTone.Neutral
		}
	}
)

private val CONTINUOUS_DATE_TEXT = UiText.Raw("Evaluacion continua")
private val DATED_DATE_TEXT = UiText.Raw("Fecha")
