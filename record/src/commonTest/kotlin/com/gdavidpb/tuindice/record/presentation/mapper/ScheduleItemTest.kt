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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScheduleItemTest {
	@Test
	fun when_noSubjectHasASchedule_then_thereIsNoGrid() {
		assertNull(listOf(attempt("MA2115"), attempt("FS2111", schedule = emptyList())).toScheduleItem()?.grid)
	}

	@Test
	fun when_onlyWeekdays_then_showsMondayToFridayAndSizesTheGridToTheLastBlock() {
		val grid = assertNotNull(
			listOf(
				attempt("MA2115", listOf(entry(day = 2, start = 1, end = 2), entry(day = 4, start = 1, end = 2))),
				attempt("FS2111", listOf(entry(day = 6, start = 5, end = 6)))
			).toScheduleItem()?.grid
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
			listOf(attempt("MA2115", listOf(entry(day = 7), entry(day = 1)))).toScheduleItem()?.grid
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
			).toScheduleItem()?.grid
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
			).toScheduleItem()?.grid
		)

		assertTrue(grid.days.flatMap { it.cells }.none { it.codeText != "MA2115" })
		assertEquals(emptyList(), grid.unscheduledCodes)
	}

	@Test
	fun when_aSubjectHasNoSchedule_then_itIsListedAsToBeAgreed() {
		val grid = assertNotNull(
			listOf(attempt("MA2115", listOf(entry())), attempt("EP1420", schedule = null)).toScheduleItem()?.grid
		)

		assertEquals(listOf("EP1420"), grid.unscheduledCodes)
	}

	@Test
	fun when_aMeetingIsMalformed_then_itIsDropped() {
		val grid = listOf(
			attempt("MA2115", listOf(entry(start = 3, end = 2), entry(day = 9), entry(start = 0, end = 1)))
		).toScheduleItem()?.grid

		assertNull(grid)
	}

	@Test
	fun when_aSubjectHasAnEnrollmentError_then_itsCellsAreFlagged() {
		val grid = assertNotNull(
			listOf(attempt("MA2115", listOf(entry()), errors = listOf("CHOQUE DE HORARIO"))).toScheduleItem()?.grid
		)

		val cell = grid.days.first().cells.single()

		assertTrue(cell.hasError)
		assertEquals("Choque de horario", cell.errorText)
	}

	@Test
	fun when_theOnlyErrorIsBlank_then_theCellIsNotFlagged() {
		val grid = assertNotNull(
			listOf(attempt("MA2115", listOf(entry()), errors = listOf("  "))).toScheduleItem()?.grid
		)

		assertFalse(grid.days.first().cells.single().hasError)
	}

	@Test
	fun when_everyMeetingOfASubjectIsMalformed_then_itIsListedAsToBeAgreed() {
		val grid = assertNotNull(
			listOf(
				attempt("MA2115", listOf(entry())),
				attempt("CI2691", listOf(entry(day = 9), entry(start = 4, end = 99)))
			).toScheduleItem()?.grid
		)

		assertEquals(listOf("CI2691"), grid.unscheduledCodes)
		assertEquals(2, grid.blockCount)
	}

	@Test
	fun when_aMeetingIsListedTwice_then_itIsOneCell() {
		val grid = assertNotNull(
			listOf(attempt("MA2115", listOf(entry(), entry()))).toScheduleItem()?.grid
		)
		val cell = grid.days.first().cells.single()

		assertEquals(1, cell.laneCount)
		assertFalse(cell.isClash)
	}

	@Test
	fun when_twoMeetingsOverlap_then_theirCellsAreNarrow() {
		val grid = assertNotNull(
			listOf(
				attempt("CI5311", listOf(entry(start = 1, end = 2))),
				attempt("CI5437", listOf(entry(start = 2, end = 3)))
			).toScheduleItem()?.grid
		)

		assertTrue(grid.days.first().cells.all { cell -> cell.isClash })
	}

	@Test
	fun when_readAsATable_then_hasOneRowPerSubjectThatIsNotWithdrawnWithTheSameDaysAsTheGrid() {
		val schedule = assertNotNull(
			listOf(
				attempt("MA2115", listOf(entry(day = 2), entry(day = 4)), section = 1),
				attempt("FS2111", listOf(entry(day = 7))),
				attempt("EP1420", schedule = null),
				attempt("EG1114", listOf(entry(day = 3)), withdrawn = true)
			).toScheduleItem()
		)

		assertEquals(listOf("MA2115", "FS2111", "EP1420"), schedule.table.rows.map { it.subjectCode })
		assertEquals(schedule.grid.days.map { it.day }, schedule.table.days)
		assertTrue(schedule.table.days.contains(ScheduleDay.Saturday))
		assertEquals(1, schedule.table.rows.first().section)
		assertEquals(
			listOf(ScheduleDay.Monday, ScheduleDay.Wednesday),
			schedule.table.rows.first().meetings.keys.toList()
		)
		assertTrue(schedule.table.rows.last().isUnscheduled)
		assertFalse(schedule.table.rows.first().isUnscheduled)
	}

	@Test
	fun when_twoSubjectsOverlapOneDay_then_onlyThatDaysCellsAreAClash() {
		val table = assertNotNull(
			listOf(
				attempt("CI5311", listOf(entry(day = 2, start = 1, end = 2), entry(day = 4, start = 1, end = 2))),
				attempt("CI5437", listOf(entry(day = 2, start = 2, end = 3), entry(day = 5, start = 3, end = 4)))
			).toScheduleItem()?.table
		)
		val clashing = table.rows.associate { row ->
			row.subjectCode to row.meetings.mapValues { (_, cells) -> cells.map { it.isClash } }
		}

		assertEquals(listOf(true), clashing.getValue("CI5311").getValue(ScheduleDay.Monday))
		assertEquals(listOf(false), clashing.getValue("CI5311").getValue(ScheduleDay.Wednesday))
		assertEquals(listOf(true), clashing.getValue("CI5437").getValue(ScheduleDay.Monday))
		assertEquals(listOf(false), clashing.getValue("CI5437").getValue(ScheduleDay.Thursday))
	}

	@Test
	fun when_aSubjectMeetsTwiceTheSameDay_then_bothMeetingsAreInItsCell() {
		val row = assertNotNull(
			listOf(attempt("MA2115", listOf(entry(day = 2, start = 1, end = 2), entry(day = 2, start = 5, end = 5))))
				.toScheduleItem()?.table
		).rows.single()

		assertEquals(listOf(1 to 2, 5 to 5), row.meetings.getValue(ScheduleDay.Monday).map { it.startBlock to it.endBlock })
	}

	@Test
	fun when_theRoomsDisagree_then_theRowHasNoClassroom_andWhenTheyAgreeItHasIt() {
		val rows = assertNotNull(
			listOf(
				attempt("MA2115", listOf(entry(classroom = "MYS-116"), entry(day = 4, classroom = " MYS-116 ")), section = 1),
				attempt("FS2111", listOf(entry(classroom = "MYS-116"), entry(day = 4, classroom = "MYS-210")), section = 2)
			).toScheduleItem()?.table
		).rows

		assertEquals("MYS-116", rows[0].classroom)
		assertNull(rows[1].classroom)
	}

	@Test
	fun when_aSubjectHasAnEnrollmentError_then_itsRowCarriesTheChipText() {
		val rows = assertNotNull(
			listOf(
				attempt("MA2115", listOf(entry()), errors = listOf("CHOQUE DE HORARIO")),
				attempt("FS2111", listOf(entry(day = 3)))
			).toScheduleItem()?.table
		).rows

		assertEquals("Choque de horario", rows[0].errorText)
		assertNull(rows[1].errorText)
	}

	private fun entry(day: Int = 2, start: Int = 1, end: Int = 2, classroom: String = "") =
		AcademicScheduleEntry(dayOfWeek = day, startBlock = start, endBlock = end, classroom = classroom)

	private fun attempt(
		code: String,
		schedule: List<AcademicScheduleEntry>? = null,
		withdrawn: Boolean = false,
		errors: List<String>? = null,
		section: Int? = null
	) = AttemptProjection(
		id = "attempt-$code",
		subjectCode = code,
		subjectName = code,
		credits = 3,
		gradingMode = AttemptGradingMode.NUMERIC,
		score = AttemptScore.empty(),
		outcome = AttemptOutcome.PENDING,
		badge = AttemptBadge.NONE,
		section = section,
		schedule = schedule,
		enrollmentErrors = errors,
		withdrawn = withdrawn
	)
}
