package com.gdavidpb.tuindice.testkit.mvi

/** Outcome of [assertMachineRandomWalk], for extra assertions or debugging. */
class MachineWalkReport(
	val steps: Int,
	val accepted: Int,
	val rejected: Int,
	val visitedRows: Int,
	val totalRows: Int,
	val finalStateName: String?
) {
	val rowCoverage: Double
		get() = if (totalRows == 0) 1.0 else visitedRows.toDouble() / totalRows
}
