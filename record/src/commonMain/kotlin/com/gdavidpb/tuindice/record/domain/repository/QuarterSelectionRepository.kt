package com.gdavidpb.tuindice.record.domain.repository

interface QuarterSelectionRepository {
	suspend fun getSelectedQuarterId(): String?
	suspend fun setSelectedQuarterId(quarterId: String)
}
