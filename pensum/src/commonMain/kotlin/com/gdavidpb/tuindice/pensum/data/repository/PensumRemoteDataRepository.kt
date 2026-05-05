package com.gdavidpb.tuindice.pensum.data.repository

import com.gdavidpb.tuindice.pensum.data.model.GetPensumResponse
import com.gdavidpb.tuindice.pensum.domain.model.PensumSelectionParams

interface PensumRemoteDataRepository {
	suspend fun getPensum(selection: PensumSelectionParams): GetPensumResponse
}
