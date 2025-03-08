package com.gdavidpb.tuindice.data.repository.attestation.source

import com.gdavidpb.tuindice.data.repository.attestation.RemoteDataSource
import com.gdavidpb.tuindice.data.repository.attestation.source.api.mapper.toChallenge
import com.gdavidpb.tuindice.data.repository.attestation.source.api.response.ChallengeResponse
import com.gdavidpb.tuindice.base.domain.model.Challenge
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class ChallengeApiDataSource(
	private val ktorClient: HttpClient
) : RemoteDataSource {
	override suspend fun getChallenge(): Challenge {
		return ktorClient.get("auth/challenge")
			.body<ChallengeResponse>()
			.toChallenge()
	}
}