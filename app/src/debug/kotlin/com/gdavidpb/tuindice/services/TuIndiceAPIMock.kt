package com.gdavidpb.tuindice.services

import com.gdavidpb.tuindice.data.source.functions.TuIndiceAPI
import com.gdavidpb.tuindice.data.source.functions.requests.AddQuarterRequest
import com.gdavidpb.tuindice.data.source.functions.requests.UpdateQuarterRequest
import com.gdavidpb.tuindice.data.source.functions.responses.*
import retrofit2.Response

class TuIndiceAPIMock : TuIndiceAPI {
	override suspend fun signIn(
		basicToken: String,
		refreshToken: Boolean,
		messagingToken: String?
	): Response<SignInResponse> {
		TODO("Not yet implemented")
	}

	override suspend fun getQuarters(): Response<List<QuarterResponse>> {
		TODO("Not yet implemented")
	}

	override suspend fun addQuarter(request: AddQuarterRequest): Response<AddQuarterResponse> {
		TODO("Not yet implemented")
	}

	override suspend fun updateQuarter(request: UpdateQuarterRequest): Response<UpdateQuarterResponse> {
		TODO("Not yet implemented")
	}

	override suspend fun deleteQuarter(quarterId: String): Response<Unit> {
		TODO("Not yet implemented")
	}

	override suspend fun enrollmentProof(): Response<EnrollmentProofResponse> {
		TODO("Not yet implemented")
	}
}