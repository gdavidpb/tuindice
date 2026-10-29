package com.gdavidpb.tuindice.data.source.assistant

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.base.presentation.model.EvaluationTone
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDisplayContext
import com.gdavidpb.tuindice.evaluations.presentation.mapper.EvaluationItemMapping
import com.gdavidpb.tuindice.evaluations.presentation.mapper.asIcon
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationHighlightTone
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class EvaluationCardsAssistantDataSourceTest {
	@Test
	fun evaluationCardsOf_givesAnEvaluationTheDeviceHasTheCardOfTheEvaluationsTab() {
		// The chat said this quiz was scheduled; the device has it past its date with no grade, and the tab says so.
		val quiz = evaluationOf(id = "ev-quiz", type = EvaluationType.QUIZ, date = WEDNESDAY, state = EvaluationState.OVERDUE)

		val card = assertNotNull(cardsOf(ids = setOf("ev-quiz"), evaluations = listOf(quiz))["ev-quiz"])

		assertEquals("QUIZ 1", card.name)
		assertEquals("CI-2611", card.subjectCode)
		assertEquals(EvaluationType.QUIZ.asIcon(), card.typeIcon)
		assertEquals(UiText.Raw("dated"), card.dateText)
		assertEquals("-- / 20", card.gradeText)
		assertEquals("Sin nota", card.statusText)
		assertEquals(EvaluationTone.Error, card.statusTone)
		assertEquals(EvaluationTone.Error, card.highlightTone)
		// The quiz falls on the Wednesday its subject meets, so it takes the room of that class, as on the tab.
		assertEquals("MYS-116", card.place?.room)
	}

	@Test
	fun evaluationCardsOf_numbersTheNamesAsTheTabDoes_andGivesAGradedOneItsGradeAndState() {
		val first = evaluationOf(
			id = "ev-1",
			type = EvaluationType.TEST,
			date = MONDAY,
			state = EvaluationState.COMPLETED,
			grade = 14.0
		)
		val second = evaluationOf(id = "ev-2", type = EvaluationType.TEST, date = WEDNESDAY, state = EvaluationState.PENDING)

		val cards = cardsOf(ids = setOf("ev-1", "ev-2"), evaluations = listOf(second, first))

		assertEquals("TEST 1", cards.getValue("ev-1").name)
		assertEquals("14 / 20", cards.getValue("ev-1").gradeText)
		assertEquals("Completada", cards.getValue("ev-1").statusText)
		assertEquals(EvaluationTone.Success, cards.getValue("ev-1").statusTone)
		assertEquals("TEST 2", cards.getValue("ev-2").name)
		assertEquals("Programada", cards.getValue("ev-2").statusText)
		// A Monday with no class of its subject has no place line.
		assertNull(cards.getValue("ev-1").place)
	}

	@Test
	fun evaluationCardsOf_leavesOutAnEvaluationTheDeviceDoesNotHave_orOneTheTabDoesNotList() {
		val listed = evaluationOf(id = "ev-1", type = EvaluationType.QUIZ, date = WEDNESDAY, state = EvaluationState.PENDING)
		val withoutSubject = listed.copy(id = "ev-orphan", attemptId = "gone")

		val cards = cardsOf(ids = setOf("ev-1", "ev-orphan", "ev-unknown"), evaluations = listOf(listed, withoutSubject))

		assertEquals(setOf("ev-1"), cards.keys)
		assertFalse("ev-unknown" in cards)
	}

	private fun cardsOf(ids: Set<String>, evaluations: List<Evaluation>) = evaluationCardsOf(
		evaluationIds = ids,
		evaluations = evaluations,
		displayContext = EvaluationDisplayContext(attempts = listOf(ATTEMPT), currentTerm = null),
		mapping = MAPPING,
		nowMillis = millisOf(LocalDate(2026, 1, 16))
	)

	private fun evaluationOf(
		id: String,
		type: EvaluationType,
		date: LocalDate,
		state: EvaluationState,
		grade: Double? = null
	) = Evaluation(
		id = id,
		attemptId = ATTEMPT.id,
		subjectCode = ATTEMPT.code,
		termId = ATTEMPT.termId,
		scheduleMode = EvaluationScheduleMode.DATED,
		grade = grade,
		maxGrade = 20.0,
		date = millisOf(date),
		type = type,
		state = state
	)

	private companion object {
		val MONDAY = LocalDate(2026, 1, 12)
		val WEDNESDAY = LocalDate(2026, 1, 14)

		val ATTEMPT = EditableAttemptDescriptor(
			id = "attempt-1",
			termId = "term-1",
			code = "CI-2611",
			name = "Algoritmos I",
			credits = 4,
			grade = 0,
			// Wednesday is day 4 in the model's week, which starts on Sunday.
			meetings = listOf(
				ScheduleMeeting(
					attemptId = "attempt-1",
					subjectCode = "CI-2611",
					section = 1,
					dayOfWeek = 4,
					startBlock = 1,
					endBlock = 2,
					classroom = "MYS-116"
				)
			)
		)

		// The tab's mapping with its texts written out, since the resources are not read on the host.
		val MAPPING = EvaluationItemMapping(
			evaluationName = { type, ordinal -> "${type.name} $ordinal" },
			typeLabel = { type -> type.name },
			gradesCompleted = { grade, maxGrade -> "$grade / $maxGrade" },
			gradesPending = { maxGrade -> "Pendiente / $maxGrade" },
			gradesOverdue = { maxGrade -> "Sin nota / $maxGrade" },
			scoreGrade = { grade, _ -> grade?.let { value -> "${value.toInt()} / 20" } ?: "-- / 20" },
			statusLabel = { state ->
				when (state) {
					EvaluationState.PENDING -> "Programada"
					EvaluationState.OVERDUE -> "Sin nota"
					EvaluationState.COMPLETED -> "Completada"
					EvaluationState.CONTINUOUS -> "Continua"
				}
			},
			typeIcon = { type -> type.asIcon() },
			dateIcon = { Icons.Outlined.CalendarToday },
			gradesIcon = { Icons.Outlined.CalendarToday },
			dateText = { UiText.Raw("dated") },
			highlightTone = { state ->
				when (state) {
					EvaluationState.COMPLETED -> EvaluationHighlightTone.Success
					EvaluationState.OVERDUE -> EvaluationHighlightTone.Error
					else -> EvaluationHighlightTone.Neutral
				}
			}
		)

		fun millisOf(date: LocalDate): Long {
			return date.atTime(LocalTime(hour = 12, minute = 0))
				.toInstant(TimeZone.currentSystemDefault())
				.toEpochMilliseconds()
		}
	}
}
