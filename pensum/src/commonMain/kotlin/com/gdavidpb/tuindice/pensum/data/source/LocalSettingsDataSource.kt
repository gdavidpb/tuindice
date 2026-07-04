package com.gdavidpb.tuindice.pensum.data.source

import com.gdavidpb.tuindice.pensum.domain.repository.PensumSelectionRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class LocalSettingsDataSource(
	private val settings: Settings
) : PensumSelectionRepository {
	private val summaryCollapsed = MutableStateFlow(
		settings.getBooleanOrNull(PENSUM_SUMMARY_COLLAPSED_KEY) ?: false
	)

	override fun observeSummaryCollapsed(): Flow<Boolean> {
		return summaryCollapsed
	}

	override suspend fun setSummaryCollapsed(isCollapsed: Boolean) {
		settings.putBoolean(PENSUM_SUMMARY_COLLAPSED_KEY, isCollapsed)
		summaryCollapsed.value = isCollapsed
	}
}

private const val PENSUM_SUMMARY_COLLAPSED_KEY = "pensumSummaryCollapsed"
