package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDayItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleGridItem

private const val FIRST_BLOCK = 1

/**
 * Lays the current term's meetings out on a grid, or returns null when no subject has a schedule
 * (so the term has nothing to switch to). Monday to Friday are always columns; Saturday and
 * Sunday only when something meets then. Withdrawn subjects stay out of the grid.
 */
internal fun List<AttemptProjection>.toScheduleGridItem(): ScheduleGridItem? {
	val active = filterNot { attempt -> attempt.withdrawn }
	val placed = active.flatMap { attempt ->
		attempt.schedule.orEmpty().mapNotNull { entry -> attempt.toPlacement(entry) }
	}

	if (placed.isEmpty()) return null

	val days = ScheduleDay.entries
		.filter { day -> !day.isWeekend || placed.any { placement -> placement.day == day } }
		.map { day ->
			ScheduleDayItem(
				day = day,
				cells = placed.filter { placement -> placement.day == day }.toCells()
			)
		}
	val unscheduledCodes = active
		.filter { attempt -> attempt.schedule.isNullOrEmpty() }
		.map(AttemptProjection::subjectCode)

	return ScheduleGridItem(
		blockCount = placed.maxOf { placement -> placement.endBlock },
		days = days,
		unscheduledCodes = unscheduledCodes
	)
}

private data class SchedulePlacement(
	val attemptId: String,
	val subjectCode: String,
	val classroom: String?,
	val day: ScheduleDay,
	val startBlock: Int,
	val endBlock: Int,
	val hasError: Boolean
)

// A malformed block range or day is dropped rather than drawn: a wrong cell is worse than none.
private fun AttemptProjection.toPlacement(entry: AcademicScheduleEntry): SchedulePlacement? {
	val day = ScheduleDay.fromCode(entry.dayOfWeek)
	val isValid = day != null && entry.startBlock >= FIRST_BLOCK && entry.endBlock >= entry.startBlock

	return day?.takeIf { isValid }?.let {
		SchedulePlacement(
			attemptId = id,
			subjectCode = subjectCode,
			classroom = entry.classroom.trim().takeIf { room -> room.isNotEmpty() },
			day = day,
			startBlock = entry.startBlock,
			endBlock = entry.endBlock,
			hasError = !enrollmentErrors.isNullOrEmpty()
		)
	}
}

// Meetings that overlap share the day's width: each cluster of overlapping meetings is split into
// as many lanes as it needs, first free lane first.
private fun List<SchedulePlacement>.toCells(): List<ScheduleCellItem> {
	val sorted = sortedWith(compareBy(SchedulePlacement::startBlock, SchedulePlacement::endBlock))
	val cells = mutableListOf<ScheduleCellItem>()
	var cluster = mutableListOf<Pair<SchedulePlacement, Int>>()
	var clusterEnd = 0

	fun flushCluster() {
		val laneCount = (cluster.maxOfOrNull { (_, lane) -> lane } ?: -1) + 1

		cluster.forEach { (placement, lane) ->
			cells += placement.toCell(lane = lane, laneCount = laneCount)
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

private fun SchedulePlacement.toCell(lane: Int, laneCount: Int) = ScheduleCellItem(
	attemptId = attemptId,
	codeText = subjectCode,
	classroomText = classroom,
	startBlock = startBlock,
	endBlock = endBlock,
	lane = lane,
	laneCount = laneCount,
	hasError = hasError
)
