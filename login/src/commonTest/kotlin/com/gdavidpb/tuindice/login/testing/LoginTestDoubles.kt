package com.gdavidpb.tuindice.login.testing

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationPayload
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.login.data.repository.LoginAuthApiDataSource
import com.gdavidpb.tuindice.login.domain.model.IssueTokens
import com.gdavidpb.tuindice.login.domain.model.RefreshTokens
import com.gdavidpb.tuindice.login.domain.repository.LoginRepository
import io.github.vinceglb.filekit.PlatformFile

val DEFAULT_LOGIN_ATTESTATION = Attestation(
	id = "attestation-id",
	token = "attestation-token",
	provider = AttestationProvider.PLAY_INTEGRITY
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

class RecordingLoginRepository(
	private val issueTokens: IssueTokens = DEFAULT_ISSUE_TOKENS,
	private val refreshTokens: RefreshTokens = DEFAULT_REFRESH_TOKENS,
	private val throwable: Throwable? = null
) : LoginRepository {
	var signInCalls = mutableListOf<Triple<String, String, Attestation>>()
	var updatePasswordCalls = mutableListOf<Triple<String, String, Attestation>>()
	var refreshTokenCalls = mutableListOf<Triple<String, String, Attestation>>()

	override suspend fun signIn(usbId: String, password: String, attestation: Attestation) {
		signInCalls += Triple(usbId, password, attestation)
		throwable?.let { throw it }
	}

	override suspend fun updatePassword(usbId: String, password: String, attestation: Attestation) {
		updatePasswordCalls += Triple(usbId, password, attestation)
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
}

class FakeAttestationRepository(
	private val attestation: Attestation = DEFAULT_LOGIN_ATTESTATION
) : AttestationRepository {
	var lastPayload: AttestationPayload? = null

	override suspend fun getAttestation(payload: AttestationPayload): Attestation {
		lastPayload = payload
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

class FakeLoginAuthApiDataSource(
	private val issueTokens: IssueTokens = DEFAULT_ISSUE_TOKENS,
	private val refreshTokens: RefreshTokens = DEFAULT_REFRESH_TOKENS,
	private val throwable: Throwable? = null
) : LoginAuthApiDataSource {
	var issueCalls = mutableListOf<Triple<String, String, Attestation>>()
	var refreshCalls = mutableListOf<Triple<String, String, Attestation>>()

	override suspend fun issueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	): IssueTokens {
		issueCalls += Triple(usbId, password, attestation)
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
}
