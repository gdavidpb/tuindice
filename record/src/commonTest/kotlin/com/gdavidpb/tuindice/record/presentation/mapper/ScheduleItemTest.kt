package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.ScheduleNow
import com.gdavidpb.tuindice.record.domain.model.projectionFor
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.presentation.model.ScheduleTableRowItem
import com.gdavidpb.tuindice.record.testing.academicAttempt
import com.gdavidpb.tuindice.record.testing.academicTerm
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_cell_description
import tuindice.record.generated.resources.schedule_cell_description_clash
import tuindice.record.generated.resources.schedule_cell_description_classroom
import tuindice.record.generated.resources.schedule_cell_description_in_progress
import tuindice.record.generated.resources.schedule_cell_description_span
import tuindice.record.generated.resources.schedule_clash
import tuindice.record.generated.resources.schedule_day_monday
import tuindice.record.generated.resources.schedule_day_tuesday
import tuindice.record.generated.resources.schedule_day_wednesday
import tuindice.record.generated.resources.schedule_table_description_classroom
import tuindice.record.generated.resources.schedule_table_description_meeting
import tuindice.record.generated.resources.schedule_table_description_section
import tuindice.record.generated.resources.schedule_table_description_unscheduled
import tuindice.record.generated.resources.schedule_table_meeting_block
import tuindice.record.generated.resources.schedule_table_meeting_blocks
import tuindice.record.generated.resources.schedule_table_meeting_in_progress
import tuindice.record.generated.resources.schedule_table_section
import tuindice.record.generated.resources.schedule_table_section_classroom
import tuindice.record.generated.resources.schedule_unscheduled
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
		assertNull(grid.unscheduledText)
	}

	@Test
	fun when_aSubjectHasNoSchedule_then_itIsListedAsToBeAgreed() {
		val grid = assertNotNull(
			listOf(attempt("MA2115", listOf(entry())), attempt("EP1420", schedule = null)).toScheduleItem()?.grid
		)

		assertEquals(unscheduled("EP1420"), grid.unscheduledText)
	}

	@Test
	fun when_severalSubjectsHaveNoSchedule_then_theLineNamesThemAll() {
		val grid = assertNotNull(
			listOf(
				attempt("MA2115", listOf(entry())),
				attempt("EP1420", schedule = null),
				attempt("EG1114", schedule = emptyList())
			).toScheduleItem()?.grid
		)

		assertEquals(unscheduled("EP1420, EG1114"), grid.unscheduledText)
	}

	@Test
	fun when_aMeetingIsMalformed_then_itIsDropped() {
		val grid = listOf(
			attempt("MA2115", listOf(entry(start = 3, end = 2), entry(day = 9), entry(start = 0, end = 1)))
		).toScheduleItem()?.grid

		assertNull(grid)
	}

	@Test
	fun when_everyMeetingOfASubjectIsMalformed_then_itIsListedAsToBeAgreed() {
		val grid = assertNotNull(
			listOf(
				attempt("MA2115", listOf(entry())),
				attempt("CI2691", listOf(entry(day = 9), entry(start = 4, end = 99)))
			).toScheduleItem()?.grid
		)

		assertEquals(unscheduled("CI2691"), grid.unscheduledText)
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
		assertEquals(
			UiText.Resource(Res.string.schedule_table_section, listOf(1)),
			schedule.table.rows.first().detailText
		)
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

		assertEquals(
			UiText.Resource(Res.string.schedule_table_section_classroom, listOf(1, "MYS-116")),
			rows[0].detailText
		)
		assertEquals(UiText.Resource(Res.string.schedule_table_section, listOf(2)), rows[1].detailText)
	}

	@Test
	fun when_theSectionIsUnknown_then_theRowsDetailIsTheClassroomAlone_orNothing() {
		val rows = assertNotNull(
			listOf(
				attempt("MA2115", listOf(entry(classroom = "MYS-116"))),
				attempt("FS2111", listOf(entry(day = 3)))
			).toScheduleItem()?.table
		).rows

		assertEquals(UiText.Raw("MYS-116"), rows[0].detailText)
		assertNull(rows[1].detailText)
	}

	@Test
	fun when_aMeetingSpansBlocks_then_theTablePrintsTheRange_andASingleBlockItsNumber() {
		val cells = assertNotNull(
			listOf(attempt("MA2115", listOf(entry(start = 1, end = 2), entry(day = 3, start = 5, end = 5))))
				.toScheduleItem()?.grid
		).days.flatMap { it.cells }

		assertEquals(listOf("1-2", "5"), cells.map { it.blocksText })
	}

	@Test
	fun when_aMeetingIsOneBlockTall_then_itsCellLeavesTheRoomOut() {
		val cells = assertNotNull(
			listOf(
				attempt(
					"MA2115",
					listOf(entry(start = 1, end = 2, classroom = "MYS-116"), entry(day = 3, start = 5, end = 5, classroom = "MYS-116"))
				)
			).toScheduleItem()?.grid
		).days.flatMap { it.cells }

		assertEquals(listOf("MYS-116", null), cells.map { it.classroomText })
	}

	@Test
	fun when_aCellIsMapped_then_itCarriesTheColoursOfTheSubjectsChip() {
		val cell = assertNotNull(
			listOf(attempt("MA2115", listOf(entry()))).toScheduleItem()?.grid
		).days.first().cells.single()
		val chipColors = CourseCodeColorGenerator.fromCode("MA2115")

		assertEquals(chipColors.color, cell.codeColor)
		assertEquals(chipColors.containerColor, cell.codeContainerColor)
	}

	@Test
	fun when_aCellIsMapped_then_itsDescriptionNamesTheDayTheBlocksTheRoomAndTheClash() {
		val cells = assertNotNull(
			listOf(
				attempt(
					"MA2115",
					listOf(entry(start = 1, end = 2, classroom = "MYS-116"), entry(day = 3, start = 5, end = 5))
				),
				attempt("FS2111", listOf(entry(start = 2, end = 3)))
			).toScheduleItem()?.grid
		).days.flatMap { it.cells }.filter { it.codeText == "MA2115" }
		val span = UiText.Resource(
			Res.string.schedule_cell_description_span,
			listOf("MA2115", UiText.Resource(Res.string.schedule_day_monday), 1, 2)
		)
		val single = UiText.Resource(
			Res.string.schedule_cell_description,
			listOf("MA2115", UiText.Resource(Res.string.schedule_day_tuesday), 5)
		)

		// Monday overlaps FS2111, so the cell says the clash after the room; Tuesday is clear.
		assertEquals(
			UiText.Resource(
				Res.string.schedule_cell_description_clash,
				listOf(
					UiText.Resource(Res.string.schedule_cell_description_classroom, listOf(span, "MYS-116")),
					UiText.Resource(Res.string.schedule_clash)
				)
			),
			cells[0].description
		)
		assertEquals(single, cells[1].description)
	}

	@Test
	fun when_aRowIsMapped_then_itsDescriptionSaysSectionRoomAndEachMeetingInOrder() {
		val row = assertNotNull(
			listOf(
				attempt(
					"CI5311",
					listOf(entry(day = 2, start = 1, end = 2, classroom = "MYS-116"), entry(day = 4, start = 3, end = 3)),
					section = 1
				)
			).toScheduleItem()?.table
		).rows.single()
		val subject = UiText.Resource(
			Res.string.schedule_table_description_classroom,
			listOf(UiText.Resource(Res.string.schedule_table_description_section, listOf("CI5311", 1)), "MYS-116")
		)
		val monday = UiText.Resource(
			Res.string.schedule_table_meeting_blocks,
			listOf(UiText.Resource(Res.string.schedule_day_monday), 1, 2)
		)
		val wednesday = UiText.Resource(
			Res.string.schedule_table_meeting_block,
			listOf(UiText.Resource(Res.string.schedule_day_wednesday), 3)
		)

		assertEquals(
			UiText.Resource(
				Res.string.schedule_table_description_meeting,
				listOf(UiText.Resource(Res.string.schedule_table_description_meeting, listOf(subject, monday)), wednesday)
			),
			row.description
		)
	}

	@Test
	fun when_aRowHasNothingPlaced_then_itsDescriptionSaysSo() {
		val row = assertNotNull(
			listOf(attempt("MA2115", listOf(entry())), attempt("EP1420", schedule = null)).toScheduleItem()?.table
		).rows.last()

		assertEquals(
			UiText.Resource(Res.string.schedule_table_description_unscheduled, listOf(UiText.Raw("EP1420"))),
			row.description
		)
	}

	@Test
	fun when_twoSubjectsOverlap_then_bothRowsSayTheClash_andTheOthersDoNot() {
		val rows = assertNotNull(
			listOf(
				attempt("CI5311", listOf(entry(day = 2, start = 1, end = 2), entry(day = 4, start = 1, end = 2))),
				attempt("CI5437", listOf(entry(day = 2, start = 2, end = 3))),
				attempt("FS2111", listOf(entry(day = 3))),
				attempt("EG1114", schedule = null)
			).toScheduleItem()?.table
		).rows.associate { row -> row.subjectCode to row.hasClash() }

		assertEquals(
			mapOf("CI5311" to true, "CI5437" to true, "FS2111" to false, "EG1114" to false),
			rows
		)
	}

	@Test
	fun when_theUniversityFlagsAnEnrollment_then_theScheduleSaysNothingOfIt() {
		// The university names a clash on one subject only, against one that has no meeting placed:
		// the app works out no overlap, so the schedule marks nothing. The card in the record keeps it.
		val flagged = attempt(
			"MA2115",
			listOf(entry()),
			errors = listOf("CHOQUE DE HORARIO CON CI5311", "EXCEDE EL LIMITE DE CREDITOS")
		)
		val schedule = assertNotNull(listOf(flagged, attempt("CI5311", schedule = null)).toScheduleItem())
		val cell = schedule.grid.days.first().cells.single()

		assertFalse(schedule.table.rows.first().hasClash())
		assertFalse(cell.isClash)
		assertEquals(
			UiText.Resource(
				Res.string.schedule_cell_description_span,
				listOf("MA2115", UiText.Resource(Res.string.schedule_day_monday), 1, 2)
			),
			cell.description
		)
		assertEquals("Choque de horario con CI5311 · +1", flagged.toEnrollmentErrorText())
	}

	@Test
	fun when_theSelectedTermIsCurrentAndHasAMeeting_then_theRecordHasAScheduleOnIt() {
		val scheduled = academicAttempt(subjectCode = "CI5311").copy(schedule = listOf(entry()))
		val record = AcademicRecord(
			id = "record",
			terms = listOf(
				academicTerm(id = "now", kind = TermKind.CURRENT, attempts = listOf(scheduled)),
				academicTerm(id = "past", kind = TermKind.HISTORICAL, attempts = listOf(scheduled))
			)
		)

		assertTrue(record.hasScheduleOnTerm(termId = "now"))
		// A closed term keeps what it was taught at, but the bar offers the schedule of the term being lived.
		assertFalse(record.hasScheduleOnTerm(termId = "past"))
		assertFalse(record.hasScheduleOnTerm(termId = "missing"))
	}

	@Test
	fun when_nothingOfTheCurrentTermCanBeDrawn_then_theRecordHasNoScheduleOnIt() {
		val attempts = listOf(
			academicAttempt(id = "none", subjectCode = "EP1420"),
			academicAttempt(id = "malformed", subjectCode = "CI2691").copy(schedule = listOf(entry(day = 9))),
			academicAttempt(id = "withdrawn", subjectCode = "EG1114").copy(schedule = listOf(entry()), withdrawn = true)
		)
		val record = AcademicRecord(
			id = "record",
			terms = listOf(academicTerm(id = "now", kind = TermKind.CURRENT, attempts = attempts))
		)

		assertFalse(record.hasScheduleOnTerm(termId = "now"))
		// The cheap answer and the full layout agree.
		assertNull(
			record.projectionFor(RecordViewMode.Projection).terms.single().attempts.toScheduleItem()
		)
	}

	@Test
	fun when_todayIsOneOfTheDaysShown_then_itsColumnIsTodayInBothViews() {
		val schedule = assertNotNull(
			listOf(attempt("MA2115", listOf(entry(day = 2), entry(day = 4)))).toScheduleItem(now = at(day = 4, hour = 8))
		)

		assertEquals(listOf(ScheduleDay.Wednesday), schedule.grid.days.filter { it.isToday }.map { it.day })
		assertEquals(ScheduleDay.Wednesday, schedule.table.today)
	}

	@Test
	fun when_todayIsNotOneOfTheDaysShown_then_nothingIsHighlighted() {
		// A Sunday with nothing on Sunday: the week has no column for it.
		val schedule = assertNotNull(
			listOf(attempt("MA2115", listOf(entry(day = 2)))).toScheduleItem(now = at(day = 1, hour = 8))
		)

		assertTrue(schedule.grid.days.none { it.isToday || it.nowBlockOffset != null })
		assertNull(schedule.table.today)
		assertTrue(schedule.grid.days.flatMap { it.cells }.none { it.isInProgress })
	}

	@Test
	fun when_theHourFallsInsideABlock_then_theLineSitsThere_andThatClassIsInProgress() {
		// Block 1 runs 7:30-8:30 and block 2 8:30-9:30: 9:00 is halfway through block 2.
		val schedule = assertNotNull(
			listOf(
				attempt("MA2115", listOf(entry(day = 2, start = 1, end = 2))),
				attempt("FS2111", listOf(entry(day = 2, start = 3, end = 4))),
				attempt("EP1420", listOf(entry(day = 3, start = 1, end = 2)))
			).toScheduleItem(now = at(day = 2, hour = 9))
		)
		val inProgress = schedule.grid.days.flatMap { it.cells }.filter { it.isInProgress }.map { it.codeText }

		assertEquals(1.5f, schedule.grid.days.first().nowBlockOffset)
		// Only today's class: Tuesday's meeting at the same blocks is not being taught.
		assertEquals(listOf("MA2115"), inProgress)
		assertEquals(
			listOf(true),
			schedule.table.rows.first().meetings.getValue(ScheduleDay.Monday).map { it.isInProgress }
		)
	}

	@Test
	fun when_theHourIsABlocksEdge_then_theBlockThatStartsIsTheOneInProgress() {
		// 8:30 sharp: block 1 has ended and block 2 starts.
		val schedule = assertNotNull(
			listOf(
				attempt("MA2115", listOf(entry(day = 2, start = 1, end = 1))),
				attempt("FS2111", listOf(entry(day = 2, start = 2, end = 2)))
			).toScheduleItem(now = at(day = 2, hour = 8, minute = 30))
		)
		val inProgress = schedule.grid.days.flatMap { it.cells }.filter { it.isInProgress }.map { it.codeText }

		assertEquals(1f, schedule.grid.days.first().nowBlockOffset)
		assertEquals(listOf("FS2111"), inProgress)
	}

	@Test
	fun when_itIsBeforeTheFirstBlock_then_todayIsMarkedButThereIsNoLineNorClassInProgress() {
		val schedule = assertNotNull(
			listOf(attempt("MA2115", listOf(entry(day = 2, start = 1, end = 2))))
				.toScheduleItem(now = at(day = 2, hour = 7, minute = 29))
		)

		assertTrue(schedule.grid.days.first().isToday)
		assertNull(schedule.grid.days.first().nowBlockOffset)
		assertTrue(schedule.grid.days.flatMap { it.cells }.none { it.isInProgress })
	}

	@Test
	fun when_itIsAfterTheLastBlockOfTheGrid_then_thereIsNoLineNorClassInProgress() {
		// The grid ends with block 2, at 9:30; the first minute of block 1 still draws the line.
		val attempts = listOf(attempt("MA2115", listOf(entry(day = 2, start = 1, end = 2))))
		val after = assertNotNull(attempts.toScheduleItem(now = at(day = 2, hour = 9, minute = 30)))
		val atStart = assertNotNull(attempts.toScheduleItem(now = at(day = 2, hour = 7, minute = 30)))

		assertNull(after.grid.days.first().nowBlockOffset)
		assertTrue(after.grid.days.flatMap { it.cells }.none { it.isInProgress })
		assertEquals(0f, atStart.grid.days.first().nowBlockOffset)
		assertTrue(atStart.grid.days.first().cells.single().isInProgress)
	}

	@Test
	fun when_aClassIsInProgress_then_bothViewsSayItAloud() {
		val schedule = assertNotNull(
			listOf(attempt("MA2115", listOf(entry(day = 2, start = 1, end = 2)))).toScheduleItem(now = at(day = 2, hour = 8))
		)
		val monday = UiText.Resource(Res.string.schedule_day_monday)

		assertEquals(
			UiText.Resource(
				Res.string.schedule_cell_description_in_progress,
				listOf(UiText.Resource(Res.string.schedule_cell_description_span, listOf("MA2115", monday, 1, 2)))
			),
			schedule.grid.days.first().cells.single().description
		)
		assertEquals(
			UiText.Resource(
				Res.string.schedule_table_description_meeting,
				listOf(
					UiText.Raw("MA2115"),
					UiText.Resource(
						Res.string.schedule_table_meeting_in_progress,
						listOf(UiText.Resource(Res.string.schedule_table_meeting_blocks, listOf(monday, 1, 2)))
					)
				)
			),
			schedule.table.rows.single().description
		)
	}

	private fun at(day: Int, hour: Int, minute: Int = 0) =
		ScheduleNow(dayOfWeek = day, minuteOfDay = hour * 60 + minute)

	private fun unscheduled(codes: String) = UiText.Resource(Res.string.schedule_unscheduled, listOf(codes))

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

// Some meeting of the row overlaps another subject's.
private fun ScheduleTableRowItem.hasClash(): Boolean {
	return meetings.values.any { cells -> cells.any(ScheduleCellItem::isClash) }
}
