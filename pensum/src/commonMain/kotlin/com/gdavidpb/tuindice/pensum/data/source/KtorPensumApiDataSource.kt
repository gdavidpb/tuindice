package com.gdavidpb.tuindice.pensum.data.source

import com.gdavidpb.tuindice.pensum.data.model.GetPensumResponse
import com.gdavidpb.tuindice.pensum.data.repository.PensumRemoteDataRepository
import com.gdavidpb.tuindice.pensum.domain.model.PensumSelectionParams
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class KtorPensumApiDataSource(
	private val ktorClient: HttpClient
) : PensumRemoteDataRepository {
	override suspend fun getPensum(selection: PensumSelectionParams): GetPensumResponse {
		return ktorClient.get("pensums/v2") {
			selection.careerCode?.let { careerCode -> parameter("career_code", careerCode) }
			selection.year?.let { year -> parameter("year", year) }
			selection.modalityId?.let { modalityId -> parameter("modality_id", modalityId) }
		}.body()
	}
}
