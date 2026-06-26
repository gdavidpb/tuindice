package com.gdavidpb.tuindice.pensum.domain.repository

import kotlinx.coroutines.flow.Flow

interface PensumSettingsRepository {
	fun observeSummaryCollapsed(): Flow<Boolean>
	fun isSummaryCollapsed(): Boolean
	fun setSummaryCollapsed(isCollapsed: Boolean)
}
