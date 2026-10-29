package com.gdavidpb.tuindice.evaluations.domain.model

// A pick of a week of the Evaluations tab, as the storage value of that week. Its order is
// the place the pick took among the picks made in this process when it was made: a save
// keeps the pick made last.
data class SelectedWeekPick(
	val order: Long,
	val weekKey: String
)
