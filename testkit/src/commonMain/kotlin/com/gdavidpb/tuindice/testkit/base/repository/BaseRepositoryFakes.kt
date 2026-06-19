package com.gdavidpb.tuindice.testkit.base.repository

import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.model.AppAvailabilityNotice
import com.gdavidpb.tuindice.base.domain.model.FlushPendingChangesResult
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.model.SyncPolicy
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.model.UpdateLaunchResult
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.FileRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.PendingChangesRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.ReviewRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.domain.repository.UpdateRepository
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update

class FakeAppEnvironmentRepository(
	private val appEnvironment: AppEnvironment = AppEnvironment(
		apiBaseUrl = "https://api.tuindice.app/",
		privacyPolicyUrl = "https://tuindice.app/privacy_policy_v6_0.html",
		termsAndConditionsUrl = "https://tuindice.app/terms_and_conditions_v6_0.html",
		supportUrl = "https://tuindice.app/support_v6_0.html",
		debug = false
	)
) : AppEnvironmentRepository {
	override fun getEnvironment(): AppEnvironment = appEnvironment
}

class RecordingBrowserRepository : BrowserRepository {
	var lastOpenedUrl: String? = null
	val openedUrls = MutableStateFlow<List<String>>(emptyList())

	override fun open(url: String) {
		lastOpenedUrl = url
		openedUrls.update { urls -> urls + url }
	}
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
	private var sessionId: String = "session-123",
	private var usbId: String = "20261234",
	private var accessToken: String = "access-token",
	private var refreshToken: String = "refresh-token"
) : SessionRepository {
	var cleared = false
		private set

	override suspend fun hasActiveSession(): Boolean {
		return sessionId.isNotBlank() &&
				usbId.isNotBlank() &&
				accessToken.isNotBlank() &&
				refreshToken.isNotBlank()
	}

	override suspend fun getActiveSessionSnapshot(): SessionSnapshot? {
		return if (hasActiveSession()) {
			SessionSnapshot(
				sessionId = sessionId,
				accessToken = accessToken,
				refreshToken = refreshToken,
				usbId = usbId
			)
		} else {
			null
		}
	}

	override suspend fun setSessionSnapshot(snapshot: SessionSnapshot) {
		sessionId = snapshot.sessionId
		accessToken = snapshot.accessToken
		refreshToken = snapshot.refreshToken
		usbId = snapshot.usbId
	}

	override suspend fun replaceSessionSnapshotIfCurrent(
		expectedSnapshot: SessionSnapshot,
		newSnapshot: SessionSnapshot
	): Boolean {
		if (getActiveSessionSnapshot() != expectedSnapshot) return false

		setSessionSnapshot(newSnapshot)
		return true
	}

	override suspend fun setUsbId(usbId: String) {
		this.usbId = usbId
	}

	override suspend fun setSessionId(sessionId: String) {
		this.sessionId = sessionId
	}

	override suspend fun setAccessToken(accessToken: String) {
		this.accessToken = accessToken
	}

	override suspend fun setRefreshToken(refreshToken: String) {
		this.refreshToken = refreshToken
	}

	override suspend fun getUsbId(): String = usbId

	override suspend fun getSessionId(): String = sessionId

	override suspend fun getAccessToken(): String = accessToken

	override suspend fun getRefreshToken(): String = refreshToken

	override suspend fun clear() {
		sessionId = ""
		usbId = ""
		accessToken = ""
		refreshToken = ""
		cleared = true
	}
}

class FakeSessionInvalidationRepository : SessionInvalidationRepository {
	private val invalidations = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
	var intentionalSignOutSessionId: String? = null
		private set
	val notifiedSessionIds = mutableListOf<String?>()
	var invalidationCalls = 0
		private set

	override fun observeSessionInvalidation(): Flow<Unit> = invalidations

	override fun markIntentionalSignOut(sessionId: String) {
		if (sessionId.isNotBlank()) {
			intentionalSignOutSessionId = sessionId
		}
	}

	override fun clearIntentionalSignOut(sessionId: String) {
		if (intentionalSignOutSessionId == sessionId) {
			intentionalSignOutSessionId = null
		}
	}

	override fun notifySessionInvalidated(sessionId: String?) {
		val shouldSuppressInvalidation =
			intentionalSignOutSessionId != null &&
					(sessionId.isNullOrBlank() || sessionId == intentionalSignOutSessionId)

		if (shouldSuppressInvalidation) {
			return
		}

		invalidationCalls++
		notifiedSessionIds += sessionId
		invalidations.tryEmit(Unit)
	}
}

class FakeSettingsRepository(
	private val reviewSuggested: Boolean = false,
	private var lastMainSection: MainSection = MainSection.SUMMARY,
	private var wizardCompleted: Boolean = true,
	private var outdatedAppState: OutdatedAppState? = null
) : SettingsRepository {
	var cleared = false
		private set

	override suspend fun isReviewSuggested(value: Int): Boolean = reviewSuggested

	override suspend fun getLastMainSection(): MainSection = lastMainSection

	override suspend fun setLastMainSection(section: MainSection) {
		lastMainSection = section
	}

	override suspend fun getOutdatedAppState(): OutdatedAppState? = outdatedAppState

	override suspend fun setOutdatedAppState(state: OutdatedAppState) {
		outdatedAppState = state
	}

	override suspend fun clearOutdatedAppState() {
		outdatedAppState = null
	}

	override suspend fun isWizardCompleted(): Boolean = wizardCompleted

	override suspend fun setWizardCompleted() {
		wizardCompleted = true
	}

	override suspend fun clear() {
		cleared = true
		outdatedAppState = null
	}
}

class FakeConfigRepository(
	private val email: String = "support@tuindice.app",
	private val subject: String = "Support TuIndice",
	private val appAvailabilityNotice: AppAvailabilityNotice = AppAvailabilityNotice(
		enabled = false,
		title = "",
		message = ""
	)
) : ConfigRepository {
	override suspend fun tryFetch() = Unit

	override fun getTimeout(): Long = 30_000L

	override fun getContactEmail(): String = email

	override fun getContactSubject(): String = subject

	override fun getLoadingMessages(): List<String> = listOf("Cargando")

	override fun getTimeUpdateStalenessDays(): Int = 7

	override fun getSyncsToSuggestReview(): Int = 3

	override fun getAttestationAndroidEnforcementEnabled(): Boolean = false

	override fun getAttestationIosEnforcementEnabled(): Boolean = true

	override fun getAppAvailabilityNotice(): AppAvailabilityNotice = appAvailabilityNotice
}

class RecordingReviewRepository : ReviewRepository {
	var launchCalls = 0
		private set

	override suspend fun launchReview() {
		launchCalls++
	}
}

class FakeUpdateRepository(
	private val updateAction: UpdateAction? = null,
	private val launchResult: UpdateLaunchResult = UpdateLaunchResult.Launched
) : UpdateRepository {
	var checkCalls = mutableListOf<Int>()
	val launchedActions = mutableListOf<UpdateAction>()

	override suspend fun checkForUpdate(stalenessDays: Int): UpdateAction? {
		checkCalls += stalenessDays
		return updateAction
	}

	override suspend fun launchUpdate(action: UpdateAction): UpdateLaunchResult {
		launchedActions += action
		return launchResult
	}
}

class FakeSyncRepository : SyncRepository {
	val scheduledSyncCalls = mutableListOf<String>()
	val scheduledSyncPolicies = mutableListOf<SyncPolicy>()

	override fun scheduleSync(password: String, policy: SyncPolicy) {
		scheduledSyncCalls += password
		scheduledSyncPolicies += policy
	}

	override fun observeSyncInProgress(): Flow<Boolean> = flowOf(false)
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

class FakeSyncStatusRepository(
	initialValue: SyncStatus = SyncStatus.Healthy,
	initialReport: SyncReport = SyncReport.success(),
	initialLastSuccessfulSyncAt: Long? = null
) : SyncStatusRepository {
	private val syncStatus = MutableStateFlow(initialValue)
	private val syncReport = MutableStateFlow(initialReport)
	private val lastSuccessfulSyncAt = MutableStateFlow(initialLastSuccessfulSyncAt)
	val setStatuses = mutableListOf<SyncStatus>()
	val setReports = mutableListOf<SyncReport>()
	val setLastSuccessfulSyncTimestamps = mutableListOf<Long>()
	var resetCalls = 0
		private set

	override fun observeSyncStatus(): Flow<SyncStatus> = syncStatus

	override fun observeSyncReport(): Flow<SyncReport> = syncReport

	override fun observeLastSuccessfulSyncAt(): Flow<Long?> = lastSuccessfulSyncAt

	override suspend fun getSyncStatus(): SyncStatus = syncStatus.value

	override suspend fun getSyncReport(): SyncReport = syncReport.value

	override suspend fun getLastSuccessfulSyncAt(): Long? = lastSuccessfulSyncAt.value

	override suspend fun setSyncStatus(status: SyncStatus) {
		syncStatus.value = status
		setStatuses += status
	}

	override suspend fun setSyncReport(report: SyncReport) {
		syncReport.value = report
		setReports += report
	}

	override suspend fun setLastSuccessfulSyncAt(timestamp: Long) {
		lastSuccessfulSyncAt.value = timestamp
		setLastSuccessfulSyncTimestamps += timestamp
	}

	override suspend fun reset() {
		syncStatus.value = SyncStatus.Healthy
		syncReport.value = SyncReport.success()
		lastSuccessfulSyncAt.value = null
		resetCalls++
	}
}

class FakePendingChangesRepository(
	var pendingChanges: PendingChanges = PendingChanges.Empty,
	var flushResult: FlushPendingChangesResult = FlushPendingChangesResult.Success,
	var getPendingChangesThrowable: Throwable? = null
) : PendingChangesRepository {
	var getPendingChangesCalls = 0
		private set
	var flushCalls = 0
		private set

	override suspend fun getPendingChanges(): PendingChanges {
		getPendingChangesCalls++
		getPendingChangesThrowable?.let { throw it }
		return pendingChanges
	}

	override suspend fun flushPendingChanges(): FlushPendingChangesResult {
		flushCalls++
		return flushResult
	}
}
