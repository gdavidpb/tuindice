package com.gdavidpb.tuindice.data.repository.attestation

import com.gdavidpb.tuindice.base.domain.model.Challenge

interface RemoteDataSource {
	suspend fun getChallenge(): Challenge
}