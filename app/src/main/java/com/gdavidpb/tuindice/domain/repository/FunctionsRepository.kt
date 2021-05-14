package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Credentials

interface FunctionsRepository {
	suspend fun checkCredentials(credentials: Credentials)
}