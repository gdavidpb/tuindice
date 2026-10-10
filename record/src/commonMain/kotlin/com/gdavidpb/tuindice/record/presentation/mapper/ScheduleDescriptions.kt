package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_cell_description
import tuindice.record.generated.resources.schedule_cell_description_clash
import tuindice.record.generated.resources.schedule_cell_description_classroom
import tuindice.record.generated.resources.schedule_cell_description_in_progress
import tuindice.record.generated.resources.schedule_cell_description_span
import tuindice.record.generated.resources.schedule_clash
import tuindice.record.generated.resources.schedule_table_description_classroom
import tuindice.record.generated.resources.schedule_table_description_meeting
import tuindice.record.generated.resources.schedule_table_description_section
import tuindice.record.generated.resources.schedule_table_description_unscheduled
import tuindice.record.generated.resources.schedule_table_meeting_block
import tuindice.record.generated.resources.schedule_table_meeting_blocks
import tuindice.record.generated.resources.schedule_table_meeting_in_progress

/**
 * What a screen reader says of a meeting on the week grid: "CI5311, lunes, bloques 1 a 2, aula
 * MYS-116". The room is said only when the meeting names one. [withCellMarks] adds what is true of
 * the meeting right now.
 */
internal fun scheduleCellDescription(
	subjectCode: String,
	day: ScheduleDay,
	blocks: IntRange,
	classroom: String?
): UiText {
	val timing = if (blocks.first == blocks.last) {
		UiText.Resource(
			Res.string.schedule_cell_description,
			listOf(subjectCode, day.toNameText(), blocks.first)
		)
	} else {
		UiText.Resource(
			Res.string.schedule_cell_description_span,
			listOf(subjectCode, day.toNameText(), blocks.first, blocks.last)
		)
	}

	return classroom?.let { room ->
		UiText.Resource(Res.string.schedule_cell_description_classroom, listOf(timing, room))
	} ?: timing
}

/**
 * Ends a cell's description with its marks: ". Choque de horario" when the meeting overlaps another
 * subject's that day, and ". En curso" for the class being taught right now.
 */
internal fun UiText.withCellMarks(isClash: Boolean, isInProgress: Boolean): UiText {
	val withClash = if (isClash) {
		UiText.Resource(
			Res.string.schedule_cell_description_clash,
			listOf(this, UiText.Resource(Res.string.schedule_clash))
		)
	} else {
		this
	}

	return if (isInProgress) {
		UiText.Resource(Res.string.schedule_cell_description_in_progress, listOf(withClash))
	} else {
		withClash
	}
}

/**
 * What a screen reader says of a row of the table: "CI5311, sección 1, aula MYS-116, lunes bloques
 * 1 a 2, miércoles bloques 1 a 2". Each part wraps what was said so far, in the order the row is
 * read; a subject with nothing placed ends in "sin horario", and the class being taught right now
 * is followed by "en curso".
 */
internal fun scheduleTableRowDescription(
	subjectCode: String,
	section: Int?,
	classroom: String?,
	meetings: Map<ScheduleDay, List<ScheduleCellItem>>
): UiText {
	val withSection = section?.let { number ->
		UiText.Resource(Res.string.schedule_table_description_section, listOf(subjectCode, number))
	} ?: UiText.Raw(subjectCode)
	val withClassroom = classroom?.let { room ->
		UiText.Resource(Res.string.schedule_table_description_classroom, listOf(withSection, room))
	} ?: withSection

	if (meetings.isEmpty()) {
		return UiText.Resource(Res.string.schedule_table_description_unscheduled, listOf(withClassroom))
	}

	return meetings
		.flatMap { (day, cells) -> cells.map { cell -> cell.toMeetingText(day = day) } }
		.fold(withClassroom) { description, meeting ->
			UiText.Resource(Res.string.schedule_table_description_meeting, listOf(description, meeting))
		}
}

// "lunes bloques 1 a 2", or "lunes bloque 3" for a single block; ", en curso" after the one being
// taught right now.
private fun ScheduleCellItem.toMeetingText(day: ScheduleDay): UiText {
	val meeting = if (blockSpan > 1) {
		UiText.Resource(
			Res.string.schedule_table_meeting_blocks,
			listOf(day.toNameText(), startBlock, endBlock)
		)
	} else {
		UiText.Resource(Res.string.schedule_table_meeting_block, listOf(day.toNameText(), startBlock))
	}

	return if (isInProgress) {
		UiText.Resource(Res.string.schedule_table_meeting_in_progress, listOf(meeting))
	} else {
		meeting
	}
}
