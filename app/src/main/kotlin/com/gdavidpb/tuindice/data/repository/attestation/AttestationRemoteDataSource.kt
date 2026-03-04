package com.gdavidpb.tuindice.data.repository.attestation

import com.gdavidpb.tuindice.base.domain.model.Challenge

interface AttestationRemoteDataSource {
	suspend fun getChallenge(): Challenge
}
