package com.gdavidpb.tuindice.evaluations.domain.repository

import kotlinx.coroutines.flow.Flow

interface EvaluationsSelectionRepository {
	fun observeSelectedWeekKey(): Flow<String?>
	suspend fun setSelectedWeekKey(weekKey: String)
}
