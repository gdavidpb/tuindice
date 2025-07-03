package com.gdavidpb.tuindice.login.data.repository

import com.gdavidpb.tuindice.login.domain.repository.LoginRepository

class LoginDataRepository(
	private val authApiDataSource: AuthApiDataSource,
	private val messagingApiDataSource: MessagingApiDataSource,
	private val reportingDataSource: ReportingDataSource,
	private val messagingDataSource: MessagingDataSource
) : LoginRepository {
	override suspend fun signIn(usbId: String, password: String) {
		val tokens = authApiDataSource.issueTokens(
			usbId = usbId,
			password = password
		)

		reportingDataSource.setIdentifier(
			id = tokens.uid
		)

		val messagingToken = messagingDataSource.getToken()

		messagingApiDataSource.subscribe(
			token = messagingToken
		)
	}

	override suspend fun signOut() {
		authApiDataSource.revokeTokens()
	}
}