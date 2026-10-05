package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScheduleGridItemTest {
	@Test
	fun when_noSubjectHasASchedule_then_thereIsNoGrid() {
		assertNull(listOf(attempt("MA2115"), attempt("FS2111", schedule = emptyList())).toScheduleGridItem())
	}

	@Test
	fun when_onlyWeekdays_then_showsMondayToFridayAndSizesTheGridToTheLastBlock() {
		val grid = assertNotNull(
			listOf(
				attempt("MA2115", listOf(entry(day = 2, start = 1, end = 2), entry(day = 4, start = 1, end = 2))),
				attempt("FS2111", listOf(entry(day = 6, start = 5, end = 6)))
			).toScheduleGridItem()
		)

		assertEquals(
			listOf(
				ScheduleDay.Monday,
				ScheduleDay.Tuesday,
				ScheduleDay.Wednesday,
				ScheduleDay.Thursday,
				ScheduleDay.Friday
			),
			grid.days.map { it.day }
		)
		assertEquals(6, grid.blockCount)
		// The backend counts days from Sunday: 2 is Monday, 4 is Wednesday.
		assertEquals(listOf("MA2115"), grid.days[0].cells.map { it.codeText })
		assertEquals(listOf("MA2115"), grid.days[2].cells.map { it.codeText })
		assertEquals(listOf("FS2111"), grid.days[4].cells.map { it.codeText })
	}

	@Test
	fun when_somethingMeetsOnSaturdayOrSunday_then_thoseColumnsAppear() {
		val grid = assertNotNull(
			listOf(attempt("MA2115", listOf(entry(day = 7), entry(day = 1)))).toScheduleGridItem()
		)

		assertEquals(listOf(ScheduleDay.Saturday, ScheduleDay.Sunday), grid.days.takeLast(2).map { it.day })
	}

	@Test
	fun when_meetingsOverlapTheSameDay_then_theyShareTheWidth() {
		val grid = assertNotNull(
			listOf(
				attempt("MA2115", listOf(entry(day = 2, start = 1, end = 3))),
				attempt("FS2111", listOf(entry(day = 2, start = 2, end = 4))),
				attempt("EP1420", listOf(entry(day = 2, start = 6, end = 6)))
			).toScheduleGridItem()
		)
		val monday = grid.days.first().cells.associateBy { it.codeText }

		assertEquals(0 to 2, monday.getValue("MA2115").lane to monday.getValue("MA2115").laneCount)
		assertEquals(1 to 2, monday.getValue("FS2111").lane to monday.getValue("FS2111").laneCount)
		assertEquals(0 to 1, monday.getValue("EP1420").lane to monday.getValue("EP1420").laneCount)
	}

	@Test
	fun when_aSubjectIsWithdrawn_then_itIsOutOfTheGridAndNotListedAsPending() {
		val grid = assertNotNull(
			listOf(
				attempt("MA2115", listOf(entry())),
				attempt("FS2111", listOf(entry(day = 3)), withdrawn = true),
				attempt("EP1420", schedule = null, withdrawn = true)
			).toScheduleGridItem()
		)

		assertTrue(grid.days.flatMap { it.cells }.none { it.codeText != "MA2115" })
		assertEquals(emptyList(), grid.unscheduledCodes)
	}

	@Test
	fun when_aSubjectHasNoSchedule_then_itIsListedAsToBeAgreed() {
		val grid = assertNotNull(
			listOf(attempt("MA2115", listOf(entry())), attempt("EP1420", schedule = null)).toScheduleGridItem()
		)

		assertEquals(listOf("EP1420"), grid.unscheduledCodes)
	}

	@Test
	fun when_aMeetingIsMalformed_then_itIsDropped() {
		val grid = listOf(
			attempt("MA2115", listOf(entry(start = 3, end = 2), entry(day = 9), entry(start = 0, end = 1)))
		).toScheduleGridItem()

		assertNull(grid)
	}

	@Test
	fun when_aSubjectHasAnEnrollmentError_then_itsCellsAreFlagged() {
		val grid = assertNotNull(
			listOf(attempt("MA2115", listOf(entry()), errors = listOf("CHOQUE DE HORARIO"))).toScheduleGridItem()
		)

		assertTrue(grid.days.first().cells.single().hasError)
	}

	private fun entry(day: Int = 2, start: Int = 1, end: Int = 2, classroom: String = "") =
		AcademicScheduleEntry(dayOfWeek = day, startBlock = start, endBlock = end, classroom = classroom)

	private fun attempt(
		code: String,
		schedule: List<AcademicScheduleEntry>? = null,
		withdrawn: Boolean = false,
		errors: List<String>? = null
	) = AttemptProjection(
		id = "attempt-$code",
		subjectCode = code,
		subjectName = code,
		credits = 3,
		gradingMode = AttemptGradingMode.NUMERIC,
		score = AttemptScore.empty(),
		outcome = AttemptOutcome.PENDING,
		badge = AttemptBadge.NONE,
		schedule = schedule,
		enrollmentErrors = errors,
		withdrawn = withdrawn
	)
}
