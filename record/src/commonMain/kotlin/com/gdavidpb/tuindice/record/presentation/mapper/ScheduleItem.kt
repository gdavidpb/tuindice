package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.isCurrent
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.record.domain.model.ScheduleBlockClock
import com.gdavidpb.tuindice.record.domain.model.ScheduleNow
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDayItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleGridItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleTableItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleTableRowItem
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_table_section
import tuindice.record.generated.resources.schedule_table_section_classroom
import tuindice.record.generated.resources.schedule_unscheduled

private const val FIRST_BLOCK = 1

// Far above the 11 blocks the university has used so far: anything beyond it is a malformed entry,
// and drawing it would stretch the grid into dozens of empty rows.
private const val LAST_BLOCK = 24

/**
 * Whether the term with [termId] is the current one and has something scheduled: what puts the
 * schedule icon on the record's bar. Asked of the record as stored, without projecting it or laying
 * the week out — the projection hands a current term's meetings over untouched, so this agrees with
 * [toScheduleItem] returning a schedule for that term.
 */
internal fun AcademicRecord.hasScheduleOnTerm(termId: String): Boolean {
	return terms.any { term ->
		term.id == termId && term.kind.isCurrent && term.attempts.any { attempt ->
			!attempt.withdrawn && attempt.schedule.orEmpty().any { entry -> entry.isPlaceable() }
		}
	}
}

/**
 * Lays the current term's meetings out once and reads that layout as a grid and as a table, or
 * returns null when no subject has a schedule (so the term has none to show). Monday to Friday are
 * always columns; Saturday and Sunday only when something meets then. Withdrawn subjects stay out.
 * Every text the two views show or say is resolved here, so they only draw. What the university
 * flagged on an enrollment is not among them: the schedule marks the clashes it works out itself
 * from the meetings, on every subject involved, and the university's own wording stays on the
 * subject's card in the record.
 *
 * [now] is the moment being looked at. It marks today's column, the "now" line of the grid and the
 * class in progress, and leaves all three out when today is not one of the days shown or the hour
 * falls outside the blocks of the grid.
 */
internal fun List<AttemptProjection>.toScheduleItem(now: ScheduleNow? = null): ScheduleItem? {
	val active = filterNot { attempt -> attempt.withdrawn }
	// distinct: the same meeting listed twice is still one cell.
	val placed = active.flatMap { attempt ->
		attempt.schedule.orEmpty().mapNotNull { entry -> attempt.toPlacement(entry) }
	}.distinct()

	if (placed.isEmpty()) return null

	val blockCount = placed.maxOf { placement -> placement.endBlock }
	val shownDays = ScheduleDay.entries
		.filter { day -> !day.isWeekend || placed.any { placement -> placement.day == day } }
	val today = now?.let { moment -> ScheduleDay.fromCode(moment.dayOfWeek) }?.takeIf { day -> day in shownDays }
	// Before block 1 or past the last block of the grid there is no line to draw nor class to mark.
	val nowBlockOffset = now
		?.takeIf { today != null }
		?.let { moment -> ScheduleBlockClock.blocksSinceFirstBlock(moment.minuteOfDay) }
		?.takeIf { offset -> offset >= 0f && offset < blockCount }
	val currentBlock = nowBlockOffset?.let { offset -> offset.toInt() + FIRST_BLOCK }
	val days = shownDays.map { day ->
		val isToday = day == today

		ScheduleDayItem(
			day = day,
			cells = placed
				.filter { placement -> placement.day == day }
				.toCells(currentBlock = currentBlock.takeIf { isToday }),
			isToday = isToday,
			nowBlockOffset = nowBlockOffset.takeIf { isToday }
		)
	}
	// Whatever ended up without a cell is still to be agreed, including a subject whose entries
	// were all malformed: it must not vanish from the screen.
	val placedAttemptIds = placed.map(SchedulePlacement::attemptId).toSet()
	val unscheduledCodes = active
		.filter { attempt -> attempt.id !in placedAttemptIds }
		.map(AttemptProjection::subjectCode)

	val grid = ScheduleGridItem(
		blockCount = blockCount,
		days = days,
		unscheduledText = unscheduledCodes.takeIf { codes -> codes.isNotEmpty() }?.let { codes ->
			UiText.Resource(Res.string.schedule_unscheduled, listOf(codes.joinToString(separator = ", ")))
		}
	)

	return ScheduleItem(
		grid = grid,
		table = ScheduleTableItem(
			days = days.map(ScheduleDayItem::day),
			rows = active.map { attempt -> attempt.toTableRow(days = days) },
			today = today
		)
	)
}

// The row takes the grid's cells, so tinting a clash in the table cannot disagree with the grid.
private fun AttemptProjection.toTableRow(days: List<ScheduleDayItem>): ScheduleTableRowItem {
	val classroom = sharedClassroom()
	val meetings = days
		.associate { dayItem ->
			dayItem.day to dayItem.cells.filter { cell -> cell.attemptId == id }
		}
		.filterValues { cells -> cells.isNotEmpty() }

	return ScheduleTableRowItem(
		attemptId = id,
		subjectCode = subjectCode,
		detailText = toTableDetailText(classroom = classroom),
		description = scheduleTableRowDescription(
			subjectCode = subjectCode,
			section = section,
			classroom = classroom,
			meetings = meetings
		),
		meetings = meetings
	)
}

// "Sec. 1 · MYS-116". The classroom stands alone when the section is unknown, and a subject with
// neither is only its code.
private fun AttemptProjection.toTableDetailText(classroom: String?): UiText? {
	val section = section ?: return classroom?.let { room -> UiText.Raw(room) }

	return if (classroom != null) {
		UiText.Resource(Res.string.schedule_table_section_classroom, listOf(section, classroom))
	} else {
		UiText.Resource(Res.string.schedule_table_section, listOf(section))
	}
}

private data class SchedulePlacement(
	val attemptId: String,
	val subjectCode: String,
	val classroom: String?,
	val day: ScheduleDay,
	val startBlock: Int,
	val endBlock: Int
)

// A day the week has and a block range that makes sense: what a meeting needs to be drawn.
private fun AcademicScheduleEntry.isPlaceable(): Boolean {
	return ScheduleDay.fromCode(dayOfWeek) != null &&
		startBlock >= FIRST_BLOCK &&
		endBlock >= startBlock &&
		endBlock <= LAST_BLOCK
}

// A malformed block range or day is dropped rather than drawn: a wrong cell is worse than none.
private fun AttemptProjection.toPlacement(entry: AcademicScheduleEntry): SchedulePlacement? {
	val day = ScheduleDay.fromCode(entry.dayOfWeek)

	return day?.takeIf { entry.isPlaceable() }?.let {
		SchedulePlacement(
			attemptId = id,
			subjectCode = subjectCode,
			classroom = entry.classroom.trim().takeIf { room -> room.isNotEmpty() },
			day = day,
			startBlock = entry.startBlock,
			endBlock = entry.endBlock
		)
	}
}

// Meetings that overlap share the day's width: each cluster of overlapping meetings is split into
// as many lanes as it needs, first free lane first. currentBlock is the block being taught when
// these are today's meetings, and null otherwise.
private fun List<SchedulePlacement>.toCells(currentBlock: Int?): List<ScheduleCellItem> {
	val sorted = sortedWith(compareBy(SchedulePlacement::startBlock, SchedulePlacement::endBlock))
	val cells = mutableListOf<ScheduleCellItem>()
	var cluster = mutableListOf<Pair<SchedulePlacement, Int>>()
	var clusterEnd = 0

	fun flushCluster() {
		val laneCount = (cluster.maxOfOrNull { (_, lane) -> lane } ?: -1) + 1

		cluster.forEach { (placement, lane) ->
			cells += placement.toCell(
				lane = lane,
				laneCount = laneCount,
				isInProgress = currentBlock != null && currentBlock in placement.startBlock..placement.endBlock
			)
		}
		cluster = mutableListOf()
	}

	sorted.forEach { placement ->
		if (cluster.isNotEmpty() && placement.startBlock > clusterEnd) flushCluster()

		val busyLanes = cluster
			.filter { (other, _) -> other.endBlock >= placement.startBlock }
			.map { (_, lane) -> lane }
			.toSet()
		val lane = generateSequence(0) { it + 1 }.first { candidate -> candidate !in busyLanes }

		cluster += placement to lane
		clusterEnd = maxOf(clusterEnd, placement.endBlock)
	}

	flushCluster()

	return cells
}

private fun SchedulePlacement.toCell(lane: Int, laneCount: Int, isInProgress: Boolean): ScheduleCellItem {
	val isSingleBlock = startBlock == endBlock
	val subjectColors = CourseCodeColorGenerator.fromCode(subjectCode)

	return ScheduleCellItem(
		attemptId = attemptId,
		codeText = subjectCode,
		// A single block is one line tall: the room would not fit under the code.
		classroomText = classroom.takeUnless { isSingleBlock },
		blocksText = if (isSingleBlock) "$startBlock" else "$startBlock-$endBlock",
		description = scheduleCellDescription(
			subjectCode = subjectCode,
			day = day,
			blocks = startBlock..endBlock,
			classroom = classroom
		).withCellMarks(isClash = laneCount > 1, isInProgress = isInProgress),
		codeColor = subjectColors.color,
		codeContainerColor = subjectColors.containerColor,
		startBlock = startBlock,
		endBlock = endBlock,
		lane = lane,
		laneCount = laneCount,
		isInProgress = isInProgress
	)
}
