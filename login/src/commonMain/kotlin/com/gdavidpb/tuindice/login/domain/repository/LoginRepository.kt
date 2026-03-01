package com.gdavidpb.tuindice.login.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.login.domain.model.RefreshTokens

interface LoginRepository {
	suspend fun signIn(
		usbId: String,
		password: String,
		attestation: Attestation
	)

	suspend fun updatePassword(
		usbId: String,
		password: String,
		attestation: Attestation
	)

	suspend fun refreshTokens(
		accessToken: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens
}
