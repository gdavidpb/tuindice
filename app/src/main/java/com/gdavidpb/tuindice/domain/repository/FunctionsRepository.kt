package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Credentials
import com.gdavidpb.tuindice.domain.model.functions.SignInResult

interface FunctionsRepository {
	suspend fun signIn(credentials: Credentials): SignInResult
	suspend fun checkCredentials(credentials: Credentials)
}