package com.gdavidpb.tuindice.services

import android.content.res.Resources
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.source.functions.TuIndiceAPI
import com.gdavidpb.tuindice.data.source.functions.requests.AddQuarterRequest
import com.gdavidpb.tuindice.data.source.functions.requests.UpdateQuarterRequest
import com.gdavidpb.tuindice.data.source.functions.responses.*
import com.gdavidpb.tuindice.base.utils.extensions.base64
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.Response
import retrofit2.mock.BehaviorDelegate
import java.util.*

class TuIndiceAPIMock(
	private val delegate: BehaviorDelegate<TuIndiceAPI>
) : TuIndiceAPI, KoinComponent {
	private val resources by inject<Resources>()

	override suspend fun signIn(
		basicToken: String,
		refreshToken: Boolean
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
				.base64()
		)

		return delegate
			.returningResponse(response)
			.enrollmentProof()
	}
}