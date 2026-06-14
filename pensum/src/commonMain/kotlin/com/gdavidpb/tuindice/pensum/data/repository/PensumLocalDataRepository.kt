package com.gdavidpb.tuindice.pensum.data.repository

import com.gdavidpb.tuindice.pensum.data.model.GetPensumResponse
import com.gdavidpb.tuindice.pensum.domain.model.AcademicPensumSnapshot
import com.gdavidpb.tuindice.pensum.domain.model.PensumSelectionParams
import kotlinx.coroutines.flow.Flow

interface PensumLocalDataRepository {
	fun observePensumResponseFlow(): Flow<GetPensumResponse?>
	fun observeAcademicSnapshotFlow(): Flow<AcademicPensumSnapshot>
	suspend fun hasSelectedPensumResponse(): Boolean
	suspend fun getSelectionParams(): PensumSelectionParams
	suspend fun savePensumResponse(response: GetPensumResponse)
	suspend fun selectPensum(year: Int)
	suspend fun selectModality(modalityId: String)
	suspend fun selectSelection(year: Int, modalityId: String)
}
