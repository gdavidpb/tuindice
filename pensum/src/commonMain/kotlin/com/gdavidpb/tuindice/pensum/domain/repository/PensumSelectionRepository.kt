package com.gdavidpb.tuindice.pensum.domain.repository

import kotlinx.coroutines.flow.Flow

interface PensumSelectionRepository {
	fun observeSummaryCollapsed(): Flow<Boolean>
	suspend fun setSummaryCollapsed(isCollapsed: Boolean)
}
