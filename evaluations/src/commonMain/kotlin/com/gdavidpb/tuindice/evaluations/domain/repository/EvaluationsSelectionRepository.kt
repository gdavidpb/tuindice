package com.gdavidpb.tuindice.evaluations.domain.repository

import com.gdavidpb.tuindice.evaluations.domain.model.SelectedWeekPick
import kotlinx.coroutines.flow.Flow

interface EvaluationsSelectionRepository {
	fun observeSelectedWeekKey(): Flow<String?>

	// Saves a pick of the student. The week left saved is the pick made last, whichever machine made it.
	suspend fun saveSelectedWeekPick(pick: SelectedWeekPick)
}
