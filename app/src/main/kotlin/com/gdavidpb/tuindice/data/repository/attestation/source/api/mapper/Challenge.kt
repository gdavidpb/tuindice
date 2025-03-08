package com.gdavidpb.tuindice.data.repository.attestation.source.api.mapper

import com.gdavidpb.tuindice.data.repository.attestation.source.api.response.ChallengeResponse
import com.gdavidpb.tuindice.base.domain.model.Challenge

fun ChallengeResponse.toChallenge() = Challenge(
	id = id,
	challenge = challenge
)