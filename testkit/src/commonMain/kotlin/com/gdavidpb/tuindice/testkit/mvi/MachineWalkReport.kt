package com.gdavidpb.tuindice.testkit.mvi

/**
 * Outcome of [assertMachineRandomWalk], for extra assertions or debugging. [visitedUnreachableRows] names, as
 * `from/on`, the rows declared unreachable that the walk visited anyway: their exclusion is stale.
 */
data class MachineWalkReport(
	val steps: Int,
	val accepted: Int,
	val rejected: Int,
	val visitedRows: Int,
	val totalRows: Int,
	val finalStateName: String?,
	val visitedUnreachableRows: List<String> = emptyList()
) {
	val rowCoverage: Double
		get() = if (totalRows == 0) 1.0 else visitedRows.toDouble() / totalRows
}
