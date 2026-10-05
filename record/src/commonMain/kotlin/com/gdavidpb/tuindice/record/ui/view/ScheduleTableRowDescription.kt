package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.record.presentation.model.ScheduleTableRowItem
import com.gdavidpb.tuindice.record.ui.model.label
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_table_description_classroom
import tuindice.record.generated.resources.schedule_table_description_meeting
import tuindice.record.generated.resources.schedule_table_description_section
import tuindice.record.generated.resources.schedule_table_description_unscheduled
import tuindice.record.generated.resources.schedule_table_meeting_block
import tuindice.record.generated.resources.schedule_table_meeting_blocks

/** What a screen reader says of a row: "CI5311, sección 1, aula MYS-116, lunes bloques 1 a 2, ...". */
@Composable
internal fun scheduleTableRowDescription(row: ScheduleTableRowItem): String {
	val withSection = row.section?.let { section ->
		stringResource(Res.string.schedule_table_description_section, row.subjectCode, section)
	} ?: row.subjectCode
	val withClassroom = row.classroom?.let { classroom ->
		stringResource(Res.string.schedule_table_description_classroom, withSection, classroom)
	} ?: withSection

	if (row.isUnscheduled) {
		return stringResource(Res.string.schedule_table_description_unscheduled, withClassroom)
	}

	var description = withClassroom

	row.meetings.forEach { (day, cells) ->
		val dayName = day.label(isShort = false)

		cells.forEach { cell ->
			val meeting = if (cell.blockSpan > 1) {
				stringResource(Res.string.schedule_table_meeting_blocks, dayName, cell.startBlock, cell.endBlock)
			} else {
				stringResource(Res.string.schedule_table_meeting_block, dayName, cell.startBlock)
			}

			description = stringResource(Res.string.schedule_table_description_meeting, description, meeting)
		}
	}

	return description
}
