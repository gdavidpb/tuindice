package com.gdavidpb.tuindice.services

import android.content.res.Resources
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.source.functions.TuIndiceAPI
import com.gdavidpb.tuindice.data.source.functions.requests.AddQuarterRequest
import com.gdavidpb.tuindice.data.source.functions.requests.UpdateQuarterRequest
import com.gdavidpb.tuindice.data.source.functions.responses.*
import com.gdavidpb.tuindice.base.utils.extensions.encodeToBase64String
import retrofit2.Response
import retrofit2.mock.BehaviorDelegate
import java.util.*

class TuIndiceAPIMock(
	private val delegate: BehaviorDelegate<TuIndiceAPI>,
	private val resources: Resources
) : TuIndiceAPI {

	override suspend fun signIn(
		basicToken: String,
		refreshToken: Boolean,
		messagingToken: String?
	): Response<SignInResponse> {
		val response = SignInResponse(
			token = UUID.randomUUID().toString()
		)

		return delegate
			.returningResponse(response)
			.signIn(basicToken, refreshToken)
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
		val response = EnrollmentProofResponse(
			name = "Enero - Marzo 2021",
			content = resources.openRawResource(R.raw.enrollment_mock)
				.use { it.readBytes() }
				.encodeToBase64String()
		)

		return delegate
			.returningResponse(response)
			.enrollmentProof()
	}
}