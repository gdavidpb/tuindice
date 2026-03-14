package com.gdavidpb.tuindice.testkit.base.repository

import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.FileRepository
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.OutdatedCredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.ReviewRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.UpdateRepository
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

data object FakeDestination : Destination()

class FakeAppEnvironmentRepository(
	private val appEnvironment: AppEnvironment = AppEnvironment(
		apiBaseUrl = "https://api.tuindice.app/",
		privacyPolicyUrl = "https://tuindice.app/privacy",
		termsAndConditionsUrl = "https://tuindice.app/terms",
		debug = false
	)
) : AppEnvironmentRepository {
	override fun getEnvironment(): AppEnvironment = appEnvironment
}

class RecordingBrowserRepository : BrowserRepository {
	var lastOpenedUrl: String? = null

	override fun open(url: String) {
		lastOpenedUrl = url
	}
}

class FakeIdentifierRepository(
	private val identifier: String = "identifier-1"
) : IdentifierRepository {
	override fun generateRandomIdentifier(): String = identifier
}

class FakeNetworkRepository(
	private val isAvailable: Boolean = true
) : NetworkRepository {
	override fun isAvailable(): Boolean = isAvailable
}

class RecordingReportingRepository : ReportingRepository {
	var lastIdentifier: String? = null
	val loggedExceptions = mutableListOf<Throwable>()
	val loggedMessages = mutableListOf<String>()
	val customKeys = mutableMapOf<String, Any>()

	override fun setIdentifier(identifier: String) {
		lastIdentifier = identifier
	}

	override fun logException(throwable: Throwable) {
		loggedExceptions += throwable
	}

	override fun logMessage(message: String) {
		loggedMessages += message
	}

	override fun <T : Any> setCustomKey(key: String, value: T) {
		customKeys[key] = value
	}
}

class FakeFileRepository(
	private val output: PlatformFile = PlatformFile("/tmp/test-file"),
	private val canOpenResult: Boolean = true
) : FileRepository {
	var lastCanOpenFile: PlatformFile? = null

	override suspend fun canOpen(file: PlatformFile): Boolean {
		lastCanOpenFile = file
		return canOpenResult
	}
}

class RecordingApplicationRepository(
	private val canOpenResult: Boolean = true
) : ApplicationRepository {
	var cleared = false
		private set
	var clearCalls = 0
		private set
	var lastCanOpenFile: PlatformFile? = null

	override suspend fun clearData() {
		cleared = true
		clearCalls++
	}

	override suspend fun canOpen(file: PlatformFile): Boolean {
		lastCanOpenFile = file
		return canOpenResult
	}
}

class FakeSessionRepository(
	private var usbId: String = "20261234",
	private var accessToken: String = "access-token",
	private var refreshToken: String = "refresh-token"
) : SessionRepository {
	var cleared = false
		private set

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

class FakeSettingsRepository(
	private val reviewSuggested: Boolean = false,
	private var lastDestination: Destination = FakeDestination
) : SettingsRepository {
	var cleared = false
		private set

	override suspend fun isReviewSuggested(value: Int): Boolean = reviewSuggested

	override suspend fun getLastDestination(): Destination = lastDestination

	override suspend fun setLastDestination(destination: Destination) {
		lastDestination = destination
	}

	override suspend fun clear() {
		cleared = true
	}
}

class FakeConfigRepository(
	private val email: String = "support@tuindice.app",
	private val subject: String = "Support TuIndice"
) : ConfigRepository {
	override suspend fun tryFetch() = Unit

	override fun getTimeout(): Long = 30_000L

	override fun getContactEmail(): String = email

	override fun getContactSubject(): String = subject

	override fun getLoadingMessages(): List<String> = listOf("Cargando")

	override fun getTimeUpdateStalenessDays(): Int = 7

	override fun getSyncsToSuggestReview(): Int = 3
}

class RecordingReviewRepository : ReviewRepository {
	var launchCalls = 0
		private set

	override suspend fun launchReview() {
		launchCalls++
	}
}

class FakeUpdateRepository(
	private val updateAction: UpdateAction? = null
) : UpdateRepository {
	var checkCalls = mutableListOf<Int>()
	val launchedActions = mutableListOf<UpdateAction>()

	override suspend fun checkForUpdate(stalenessDays: Int): UpdateAction? {
		checkCalls += stalenessDays
		return updateAction
	}

	override suspend fun launchUpdate(action: UpdateAction) {
		launchedActions += action
	}
}

class FakeSyncRepository : SyncRepository {
	val scheduledSyncCalls = mutableListOf<String>()

	override fun scheduleSync(password: String) {
		scheduledSyncCalls += password
	}
}

class FakeCredentialsRepository(
	password: String? = null
) : CredentialsRepository {
	var password: String? = password
	val storedPasswords = mutableListOf<String>()
	var clearCalls = 0
		private set

	override suspend fun hasPassword(): Boolean = password != null

	override suspend fun getPassword(): String = password ?: throw IllegalStateException()

	override suspend fun setPassword(password: String) {
		this.password = password
		storedPasswords += password
	}

	override suspend fun clearPassword() {
		password = null
		clearCalls++
	}
}

class FakeOutdatedCredentialsRepository(
	initialValue: Boolean = false
) : OutdatedCredentialsRepository {
	private val outdatedCredentials = MutableStateFlow(initialValue)
	var setCalls = 0
		private set
	var clearCalls = 0
		private set

	override fun observeOutdatedCredentials(): Flow<Boolean> = outdatedCredentials

	override suspend fun hasOutdatedCredentials(): Boolean = outdatedCredentials.value

	override suspend fun setOutdatedCredentials() {
		outdatedCredentials.value = true
		setCalls++
	}

	override suspend fun clearOutdatedCredentials() {
		outdatedCredentials.value = false
		clearCalls++
	}
}
