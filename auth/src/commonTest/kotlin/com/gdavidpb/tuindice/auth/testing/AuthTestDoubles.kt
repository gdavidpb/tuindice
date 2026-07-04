package com.gdavidpb.tuindice.auth.testing

import com.gdavidpb.tuindice.auth.data.repository.AuthApiDataRepository
import com.gdavidpb.tuindice.auth.domain.model.AttestedTokenFlow
import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.IssueTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.security.domain.model.Attestation
import com.gdavidpb.tuindice.security.domain.model.AttestationRequest
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository

val DEFAULT_AUTH_ATTESTATION = Attestation(
	token = "attestation-token"
)

val DEFAULT_BOOTSTRAP_TOKENS = BootstrapTokens(
	uid = "uid-123",
	usbId = "20261234",
	accessToken = "bootstrap-access-token",
	expiresIn = 300L
)

val DEFAULT_ISSUE_TOKENS = IssueTokens(
	uid = "uid-123",
	usbId = "20261234",
	sessionId = "session-123",
	accessToken = "access-token",
	refreshToken = "refresh-token",
	expiresIn = 3600L
)

val DEFAULT_REFRESH_TOKENS = RefreshTokens(
	sessionId = "session-123",
	accessToken = "refreshed-access-token",
	refreshToken = "refreshed-refresh-token",
	expiresIn = 3600L
)

data class BootstrapSignInCall(
	val usbId: String,
	val password: String
)

data class ExchangeSignInCall(
	val bootstrapAccessToken: String,
	val attestation: Attestation
)

data class ReissueTokensCall(
	val usbId: String,
	val password: String,
	val attestation: Attestation
)

class RecordingAuthRepository(
	private val bootstrapTokens: BootstrapTokens = DEFAULT_BOOTSTRAP_TOKENS,
	private val refreshTokens: RefreshTokens = DEFAULT_REFRESH_TOKENS,
	private val throwable: Throwable? = null
) : AuthRepository {
	var bootstrapSignInCalls = mutableListOf<BootstrapSignInCall>()
	var exchangeSignInCalls = mutableListOf<ExchangeSignInCall>()
	var reissueTokensCalls = mutableListOf<ReissueTokensCall>()
	var refreshTokenCalls = mutableListOf<Triple<String, String, Attestation>>()
	var revokeTokensCalls = 0
	var revokedSessionIds = mutableListOf<String>()
	var revokedRefreshTokens = mutableListOf<String>()

	override suspend fun bootstrapSignIn(
		usbId: String,
		password: String
	): BootstrapTokens {
		bootstrapSignInCalls += BootstrapSignInCall(
			usbId = usbId,
			password = password
		)
		throwable?.let { throw it }
		return bootstrapTokens
	}

	override suspend fun exchangeSignIn(
		bootstrapAccessToken: String,
		attestation: Attestation
	) {
		exchangeSignInCalls += ExchangeSignInCall(
			bootstrapAccessToken = bootstrapAccessToken,
			attestation = attestation
		)
		throwable?.let { throw it }
	}

	override suspend fun reissueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	) {
		reissueTokensCalls += ReissueTokensCall(
			usbId = usbId,
			password = password,
			attestation = attestation
		)
		throwable?.let { throw it }
	}

	override suspend fun refreshTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens {
		refreshTokenCalls += Triple(sessionId, refreshToken, attestation)
		throwable?.let { throw it }
		return this.refreshTokens
	}

	override suspend fun revokeTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	) {
		revokeTokensCalls++
		revokedSessionIds += sessionId
		revokedRefreshTokens += refreshToken
		throwable?.let { throw it }
	}
}

class FakeAttestationRepository(
	private val attestation: Attestation = DEFAULT_AUTH_ATTESTATION
) : AttestationRepository {
	var lastRequest: AttestationRequest? = null

	override suspend fun attest(request: AttestationRequest): Attestation {
		lastRequest = request
		return attestation
	}
}

class RecordingMessagingRepository : MessagingRepository {
	var subscribeCalls = 0
	var unsubscribeCalls = 0

	override suspend fun subscribe() {
		subscribeCalls++
	}

	override suspend fun unsubscribe() {
		unsubscribeCalls++
	}
}

class FakeAuthApiDataSource(
	private val bootstrapTokens: BootstrapTokens = DEFAULT_BOOTSTRAP_TOKENS,
	private val issueTokens: IssueTokens = DEFAULT_ISSUE_TOKENS,
	private val refreshTokens: RefreshTokens = DEFAULT_REFRESH_TOKENS,
	private val throwable: Throwable? = null,
	private val onExchangeTokens: suspend () -> Unit = {},
	private val onReissueTokens: suspend () -> Unit = {},
	private val onRefreshTokens: suspend () -> Unit = {}
) : AuthApiDataRepository {
	var bootstrapCalls = mutableListOf<BootstrapSignInCall>()
	var exchangeCalls = mutableListOf<ExchangeSignInCall>()
	var reissueCalls = mutableListOf<ReissueTokensCall>()
	var refreshCalls = mutableListOf<Triple<String, String, Attestation>>()
	var revokeCalls = 0
	var revokedSessionIds = mutableListOf<String>()
	var revokedRefreshTokens = mutableListOf<String>()

	override suspend fun bootstrapSignIn(
		usbId: String,
		password: String
	): BootstrapTokens {
		bootstrapCalls += BootstrapSignInCall(
			usbId = usbId,
			password = password
		)
		throwable?.let { throw it }
		return bootstrapTokens
	}

	override suspend fun exchangeSignIn(
		bootstrapAccessToken: String,
		attestation: Attestation
	): IssueTokens {
		exchangeCalls += ExchangeSignInCall(
			bootstrapAccessToken = bootstrapAccessToken,
			attestation = attestation
		)
		throwable?.let { throw it }
		onExchangeTokens()
		return issueTokens
	}

	override suspend fun reissueTokens(
		usbId: String,
		password: String,
		attestedFlow: AttestedTokenFlow,
		attestation: Attestation
	): IssueTokens {
		reissueCalls += ReissueTokensCall(
			usbId = usbId,
			password = password,
			attestation = attestation
		)
		throwable?.let { throw it }
		onReissueTokens()
		return issueTokens
	}

	override suspend fun refreshTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens {
		refreshCalls += Triple(sessionId, refreshToken, attestation)
		throwable?.let { throw it }
		onRefreshTokens()
		return refreshTokens
	}

	override suspend fun revokeTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	) {
		revokeCalls++
		revokedSessionIds += sessionId
		revokedRefreshTokens += refreshToken
		throwable?.let { throw it }
	}
}
