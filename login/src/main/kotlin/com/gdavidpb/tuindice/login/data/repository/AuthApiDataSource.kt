package com.gdavidpb.tuindice.login.data.repository

import com.gdavidpb.tuindice.login.domain.model.IssueTokens

interface AuthApiDataSource {
	suspend fun issueTokens(usbId: String, password: String): IssueTokens
	suspend fun revokeTokens()
}