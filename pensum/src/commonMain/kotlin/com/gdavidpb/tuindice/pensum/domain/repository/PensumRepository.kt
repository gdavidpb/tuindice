package com.gdavidpb.tuindice.pensum.domain.repository

import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import kotlinx.coroutines.flow.Flow

interface PensumRepository {
	fun observePensumFlow(): Flow<PensumObservation>
	suspend fun hasSelectedPensumResponse(): Boolean
	suspend fun refreshPensum(forceRemote: Boolean)
	suspend fun selectSelection(year: Int, modalityId: String)
}
