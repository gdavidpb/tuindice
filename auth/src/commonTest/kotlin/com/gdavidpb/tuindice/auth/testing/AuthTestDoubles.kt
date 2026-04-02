package com.gdavidpb.tuindice.auth.testing

import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationRequest
import com.gdavidpb.tuindice.auth.data.repository.AuthApiDataRepository
import com.gdavidpb.tuindice.auth.domain.model.IssueTokens
import com.gdavidpb.tuindice.auth.domain.model.AttestedTokenFlow
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import io.github.vinceglb.filekit.PlatformFile

val DEFAULT_AUTH_ATTESTATION = Attestation(
	token = "attestation-token"
)

val DEFAULT_ISSUE_TOKENS = IssueTokens(
	uid = "uid-123",
	usbId = "20261234",
	accessToken = "access-token",
	refreshToken = "refresh-token",
	expiresIn = 3600L
)

val DEFAULT_REFRESH_TOKENS = RefreshTokens(
	accessToken = "refreshed-access-token",
	refreshToken = "refreshed-refresh-token",
	expiresIn = 3600L
)

data class IssueTokensCall(
	val usbId: String,
	val password: String,
	val flow: AttestedTokenFlow,
	val attestation: Attestation
)

class RecordingAuthRepository(
	private val refreshTokens: RefreshTokens = DEFAULT_REFRESH_TOKENS,
	private val throwable: Throwable? = null
) : AuthRepository {
	var issueTokensCalls = mutableListOf<IssueTokensCall>()
	var refreshTokenCalls = mutableListOf<Triple<String, String, Attestation>>()
	var revokeTokensCalls = 0
	var revokedAccessTokens = mutableListOf<String>()

	override suspend fun issueTokens(
		usbId: String,
		password: String,
		attestedFlow: AttestedTokenFlow,
		attestation: Attestation
	) {
		issueTokensCalls += IssueTokensCall(
			usbId = usbId,
			password = password,
			flow = attestedFlow,
			attestation = attestation
		)
		throwable?.let { throw it }
	}

	override suspend fun refreshTokens(
		accessToken: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens {
		refreshTokenCalls += Triple(accessToken, refreshToken, attestation)
		throwable?.let { throw it }
		return this.refreshTokens
	}

	override suspend fun revokeTokens(accessToken: String) {
		revokeTokensCalls++
		revokedAccessTokens += accessToken
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

class FakeSessionRepository(
	private var usbId: String = DEFAULT_ISSUE_TOKENS.usbId,
	private var accessToken: String = DEFAULT_ISSUE_TOKENS.accessToken,
	private var refreshToken: String = DEFAULT_ISSUE_TOKENS.refreshToken
) : SessionRepository {
	var cleared = false

	override suspend fun hasActiveSession(): Boolean {
		return accessToken.isNotBlank() && refreshToken.isNotBlank()
	}

	override suspend fun setUsbId(usbId: String) {
		this.usbId = usbId
	}

	override suspend fun setAccessToken(accessToken: String) {
		this.accessToken = accessToken
	}

	override suspend fun setRefreshToken(refreshToken: String) {
		this.refreshToken = refreshToken
	}

	override suspend fun getUsbId(): String = usbId

	override suspend fun getAccessToken(): String = accessToken

	override suspend fun getRefreshToken(): String = refreshToken

	override suspend fun clear() {
		usbId = ""
		accessToken = ""
		refreshToken = ""
		cleared = true
	}
}

class FakeNetworkRepository(
	private val isAvailable: Boolean
) : NetworkRepository {
	override fun isAvailable(): Boolean = isAvailable
}

class RecordingReportingRepository : ReportingRepository {
	var identifier: String? = null
	val exceptions = mutableListOf<Throwable>()
	val messages = mutableListOf<String>()
	val customKeys = mutableMapOf<String, Any>()

	override fun setIdentifier(identifier: String) {
		this.identifier = identifier
	}

	override fun logException(throwable: Throwable) {
		exceptions += throwable
	}

	override fun logMessage(message: String) {
		messages += message
	}

	override fun <T : Any> setCustomKey(key: String, value: T) {
		customKeys[key] = value
	}
}

class RecordingApplicationRepository : ApplicationRepository {
	var cleared = false

	override suspend fun clearData() {
		cleared = true
	}

	override suspend fun canOpen(file: PlatformFile): Boolean = true
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
	private val issueTokens: IssueTokens = DEFAULT_ISSUE_TOKENS,
	private val refreshTokens: RefreshTokens = DEFAULT_REFRESH_TOKENS,
	private val throwable: Throwable? = null
) : AuthApiDataRepository {
	var issueCalls = mutableListOf<IssueTokensCall>()
	var refreshCalls = mutableListOf<Triple<String, String, Attestation>>()
	var revokeCalls = 0
	var revokedAccessTokens = mutableListOf<String>()

	override suspend fun issueTokens(
		usbId: String,
		password: String,
		attestedFlow: AttestedTokenFlow,
		attestation: Attestation
	): IssueTokens {
		issueCalls += IssueTokensCall(
			usbId = usbId,
			password = password,
			flow = attestedFlow,
			attestation = attestation
		)
		throwable?.let { throw it }
		return issueTokens
	}

	override suspend fun refreshTokens(
		accessToken: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens {
		refreshCalls += Triple(accessToken, refreshToken, attestation)
		throwable?.let { throw it }
		return refreshTokens
	}

	override suspend fun revokeTokens(accessToken: String) {
		revokeCalls++
		revokedAccessTokens += accessToken
		throwable?.let { throw it }
	}
}
