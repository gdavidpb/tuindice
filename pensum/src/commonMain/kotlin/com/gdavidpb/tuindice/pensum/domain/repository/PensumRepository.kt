package com.gdavidpb.tuindice.pensum.domain.repository

import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import kotlinx.coroutines.flow.Flow

interface PensumRepository {
	fun observePensumFlow(): Flow<PensumObservation>
	suspend fun refreshPensum()
	suspend fun selectPensum(careerCode: Int, year: Int)
	suspend fun selectModality(modalityId: String)
	suspend fun selectSelection(careerCode: Int, year: Int, modalityId: String)
}
