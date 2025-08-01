package com.gdavidpb.tuindice.login.domain.repository

import com.gdavidpb.tuindice.login.domain.model.IssueTokens

interface AuthApiRepository {
	suspend fun issueTokens(usbId: String, password: String): IssueTokens
	suspend fun revokeTokens()
}