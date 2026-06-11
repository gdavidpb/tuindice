package com.gdavidpb.tuindice.data.repository.sync

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicProfile
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.domain.model.SyncPolicy
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.data.model.SyncResult
import com.gdavidpb.tuindice.data.source.sync.SyncDataSource
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import com.gdavidpb.tuindice.testkit.ktor.serverResponseException
import io.ktor.http.HttpStatusCode
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SyncRepositoryContractTest {
	@Test
	fun scheduleSync_callsApi_persistsSnapshot_marksCooldowns_andMarksHealthy() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false)
		val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.Failed)
		val remoteDataSource = FakeSyncRemoteDataSource()
		val recordLocalDataSource = FakeAcademicRecordLocalDataRepository()
		val userLocalDataSource = FakeUserLocalDataRepository()
		val repository = createRepository(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = remoteDataSource,
			recordLocalDataSource = recordLocalDataSource,
			userLocalDataSource = userLocalDataSource,
			dispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "secret123", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(listOf("secret123"), remoteDataSource.syncPasswords)
		assertEquals(listOf(DEFAULT_RECORD), recordLocalDataSource.savedRecords)
		assertEquals(listOf(DEFAULT_USER), userLocalDataSource.updatedUsers)
		assertEquals(true, settingsDataSource.cooldownMarked)
		assertEquals(true, settingsDataSource.syncedFeatureCooldownsMarked)
		assertEquals(true, settingsDataSource.staleFeatureCooldownsCleared)
		assertEquals(SyncStatus.Healthy, syncStatusRepository.getSyncStatus())
		assertEquals(listOf(SyncStatus.Healthy), syncStatusRepository.setStatuses)
	}

	@Test
	fun scheduleSync_skipsApiAndInFlightStateWhenOnCooldown() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = true)
		val syncStatusRepository = FakeSyncStatusRepository()
		val remoteDataSource = FakeSyncRemoteDataSource()
		val repository = createRepository(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = remoteDataSource,
			dispatcher = StandardTestDispatcher(testScheduler)
		)
		val emissions = mutableListOf<Boolean>()

		backgroundScope.launch {
			repository.observeSyncInProgress()
				.take(1)
				.toList(emissions)
		}
		repository.scheduleSync(password = "new-secret", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(listOf(false), emissions)
		assertEquals(emptyList(), remoteDataSource.syncPasswords)
		assertEquals(false, settingsDataSource.cooldownMarked)
	}

	@Test
	fun observeSyncInProgress_emitsTrueOnlyWhileRemoteSyncIsRunning() = runTest {
		val remoteDataSource = FakeSyncRemoteDataSource(blockUntilCompleted = true)
		val repository = createRepository(
			settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false),
			syncStatusRepository = FakeSyncStatusRepository(),
			remoteDataSource = remoteDataSource,
			dispatcher = StandardTestDispatcher(testScheduler)
		)
		var observedDuringRemoteCall = false

		remoteDataSource.onSyncStarted = {
			observedDuringRemoteCall = repository.observeSyncInProgress().first()
		}
		assertEquals(false, repository.observeSyncInProgress().first())

		repository.scheduleSync(password = "stored-secret", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(true, observedDuringRemoteCall)

		remoteDataSource.complete()
		advanceUntilIdle()

		assertEquals(false, repository.observeSyncInProgress().first())
	}

	@Test
	fun scheduleSync_doesNotMarkSyncCooldownWhenSyncFails() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false)
		val syncStatusRepository = FakeSyncStatusRepository()
		val remoteDataSource = FakeSyncRemoteDataSource(
			throwable = serverResponseException(
				statusCode = HttpStatusCode.BadRequest,
				path = "/record/v5/sync"
			)
		)
		val repository = createRepository(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = remoteDataSource,
			dispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "secret123", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(listOf("secret123"), remoteDataSource.syncPasswords)
		assertEquals(false, settingsDataSource.cooldownMarked)
		assertEquals(false, settingsDataSource.syncRetryBackoffMarked)
		assertEquals(SyncStatus.Failed, syncStatusRepository.getSyncStatus())
	}

	@Test
	fun scheduleSync_retriableFailure_marksSyncRetryBackoffAndBlocksNextRespectCooldownAttempt() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false)
		val syncStatusRepository = FakeSyncStatusRepository()
		val remoteDataSource = FakeSyncRemoteDataSource(
			throwable = serverResponseException(
				statusCode = HttpStatusCode.ServiceUnavailable,
				path = "/record/v5/sync"
			)
		)
		val repository = createRepository(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = remoteDataSource,
			dispatcher = StandardTestDispatcher(testScheduler)
		)
		val successRemoteDataSource = FakeSyncRemoteDataSource()

		repository.scheduleSync(password = "secret123", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(listOf("secret123"), remoteDataSource.syncPasswords)
		assertEquals(true, settingsDataSource.syncRetryBackoffMarked)
		assertEquals(1, settingsDataSource.syncRetryBackoffState.retryCount)
		assertEquals(SyncStatus.Unavailable, syncStatusRepository.getSyncStatus())

		createRepository(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = FakeSyncStatusRepository(),
			remoteDataSource = successRemoteDataSource,
			dispatcher = StandardTestDispatcher(testScheduler)
		).scheduleSync(password = "secret123", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(emptyList(), successRemoteDataSource.syncPasswords)
		assertEquals(true, settingsDataSource.syncRetryBackoffActive)
	}

	@Test
	fun scheduleSync_forceRefresh_clearsSyncRetryBackoff() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = true)
		settingsDataSource.markSyncRetryBackoff(IllegalStateException("backoff-active"))
		val remoteDataSource = FakeSyncRemoteDataSource()
		val repository = createRepository(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = FakeSyncStatusRepository(),
			remoteDataSource = remoteDataSource,
			dispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "recovery-secret", policy = SyncPolicy.ForceRefresh)
		advanceUntilIdle()

		assertEquals(true, settingsDataSource.recoveryCooldownsCleared)
		assertEquals(true, settingsDataSource.syncRetryBackoffCleared)
		assertEquals(false, settingsDataSource.syncRetryBackoffActive)
		assertEquals(listOf("recovery-secret"), remoteDataSource.syncPasswords)
	}

	@Test
	fun scheduleSync_success_clearsSyncRetryBackoffAndAllowsNextAttempt() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = true)
		settingsDataSource.syncRetryBackoffActive = true
		settingsDataSource.syncRetryBackoffState = SyncRetryBackoffState(
			retryCount = 1,
			lastRetryAt = 1L,
			retryBackoffUntil = Long.MAX_VALUE
		)
		settingsDataSource.syncRetryBackoffMarked = true
		val syncStatusRepository = FakeSyncStatusRepository()
		val forceRemoteDataSource = FakeSyncRemoteDataSource()
		createRepository(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = forceRemoteDataSource,
			dispatcher = StandardTestDispatcher(testScheduler)
		).scheduleSync(password = "recovery-secret", policy = SyncPolicy.ForceRefresh)
		advanceUntilIdle()

		assertEquals(true, settingsDataSource.syncRetryBackoffCleared)
		assertEquals(false, settingsDataSource.syncRetryBackoffActive)
		assertEquals(listOf("recovery-secret"), forceRemoteDataSource.syncPasswords)
		assertEquals(true, settingsDataSource.cooldownMarked)

		val nextRemoteDataSource = FakeSyncRemoteDataSource()
		settingsDataSource.onCooldown = false
		createRepository(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = nextRemoteDataSource,
			dispatcher = StandardTestDispatcher(testScheduler)
		).scheduleSync(password = "secret123", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(true, settingsDataSource.cooldownMarked)
		assertEquals(listOf("secret123"), nextRemoteDataSource.syncPasswords)
	}

	@Test
	fun scheduleSync_nonRetriableError_doesNotMarkSyncRetryBackoff() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false)
		val syncStatusRepository = FakeSyncStatusRepository()
		val remoteDataSource = FakeSyncRemoteDataSource(
			throwable = serverResponseException(
				statusCode = HttpStatusCode.BadRequest,
				path = "/record/v5/sync"
			)
		)
		val repository = createRepository(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = remoteDataSource,
			dispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "secret123", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(listOf("secret123"), remoteDataSource.syncPasswords)
		assertEquals(false, settingsDataSource.syncRetryBackoffMarked)
		assertEquals(false, settingsDataSource.cooldownMarked)
		assertEquals(SyncStatus.Failed, syncStatusRepository.getSyncStatus())
	}

	@Test
	fun scheduleSync_ignoresConflictAndKeepsCooldownUntouched() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false)
		val syncStatusRepository = FakeSyncStatusRepository()
		val remoteDataSource = FakeSyncRemoteDataSource(
			throwable = clientRequestException(
				statusCode = HttpStatusCode.Conflict,
				path = "/record/v5/sync"
			)
		)
		val repository = createRepository(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = remoteDataSource,
			dispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "stored-secret", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(listOf("stored-secret"), remoteDataSource.syncPasswords)
		assertEquals(false, settingsDataSource.cooldownMarked)
		assertEquals(false, settingsDataSource.syncedFeatureCooldownsMarked)
		assertEquals(false, settingsDataSource.staleFeatureCooldownsCleared)
		assertEquals(SyncStatus.OutdatedCredentials, syncStatusRepository.getSyncStatus())
		assertEquals(listOf(SyncStatus.OutdatedCredentials), syncStatusRepository.setStatuses)
	}

	@Test
	fun scheduleSync_marksUnavailable_whenSyncFailsWithServiceUnavailable() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false)
		val syncStatusRepository = FakeSyncStatusRepository()
		val remoteDataSource = FakeSyncRemoteDataSource(
			throwable = serverResponseException(
				statusCode = HttpStatusCode.ServiceUnavailable,
				path = "/record/v5/sync"
			)
		)
		val repository = createRepository(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = remoteDataSource,
			dispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "new-secret", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(listOf("new-secret"), remoteDataSource.syncPasswords)
		assertEquals(SyncStatus.Unavailable, syncStatusRepository.getSyncStatus())
		assertEquals(listOf(SyncStatus.Unavailable), syncStatusRepository.setStatuses)
	}

	@Test
	fun scheduleSync_marksUnavailable_whenSyncFailsWithFailedDependency() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false)
		val syncStatusRepository = FakeSyncStatusRepository()
		val remoteDataSource = FakeSyncRemoteDataSource(
			throwable = clientRequestException(
				statusCode = HttpStatusCode.FailedDependency,
				path = "/record/v5/sync"
			)
		)
		val repository = createRepository(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = remoteDataSource,
			dispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "new-secret", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(listOf("new-secret"), remoteDataSource.syncPasswords)
		assertEquals(SyncStatus.Unavailable, syncStatusRepository.getSyncStatus())
		assertEquals(listOf(SyncStatus.Unavailable), syncStatusRepository.setStatuses)
	}

	@Test
	fun scheduleSync_marksFailed_whenSyncFailsWithUnhandledError() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false)
		val syncStatusRepository = FakeSyncStatusRepository()
		val remoteDataSource = FakeSyncRemoteDataSource(
			throwable = serverResponseException(
				statusCode = HttpStatusCode.InternalServerError,
				path = "/record/v5/sync"
			)
		)
		val repository = createRepository(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = remoteDataSource,
			dispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "new-secret", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(listOf("new-secret"), remoteDataSource.syncPasswords)
		assertEquals(SyncStatus.Failed, syncStatusRepository.getSyncStatus())
		assertEquals(listOf(SyncStatus.Failed), syncStatusRepository.setStatuses)
	}

	@Test
	fun scheduleSync_skipsApiWhenSyncStatusIsOutdatedCredentials() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false)
		val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.OutdatedCredentials)
		val remoteDataSource = FakeSyncRemoteDataSource()
		val repository = createRepository(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = remoteDataSource,
			dispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "new-secret", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(emptyList(), remoteDataSource.syncPasswords)
		assertEquals(false, settingsDataSource.cooldownMarked)
	}

	@Test
	fun scheduleSync_forceRefresh_callsApiAndClearsRecoveryCooldowns_whenOnCooldown() = runTest {
		val events = mutableListOf<String>()
		val settingsDataSource = FakeSyncSettingsLocalDataSource(
			onCooldown = true,
			events = events
		)
		val remoteDataSource = FakeSyncRemoteDataSource(events = events)
		val repository = createRepository(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = FakeSyncStatusRepository(),
			remoteDataSource = remoteDataSource,
			dispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "recovery-secret", policy = SyncPolicy.ForceRefresh)
		advanceUntilIdle()

		assertEquals(listOf("recovery-secret"), remoteDataSource.syncPasswords)
		assertEquals(true, settingsDataSource.recoveryCooldownsCleared)
		assertEquals(true, settingsDataSource.cooldownMarked)
		assertEquals(true, settingsDataSource.syncedFeatureCooldownsMarked)
		assertEquals(true, settingsDataSource.staleFeatureCooldownsCleared)
		assertEquals(listOf("clear_recovery", "sync"), events)
	}

	@Test
	fun scheduleSync_forceRefresh_skipsApiWhenSyncStatusIsOutdatedCredentials() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = true)
		val remoteDataSource = FakeSyncRemoteDataSource()
		val repository = createRepository(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.OutdatedCredentials),
			remoteDataSource = remoteDataSource,
			dispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "recovery-secret", policy = SyncPolicy.ForceRefresh)
		advanceUntilIdle()

		assertEquals(emptyList(), remoteDataSource.syncPasswords)
		assertEquals(false, settingsDataSource.recoveryCooldownsCleared)
		assertEquals(false, settingsDataSource.cooldownMarked)
	}

	private fun createRepository(
		settingsDataSource: SyncSettingsLocalDataRepository,
		syncStatusRepository: SyncStatusRepository,
		remoteDataSource: SyncRemoteDataRepository,
		recordLocalDataSource: FakeAcademicRecordLocalDataRepository = FakeAcademicRecordLocalDataRepository(),
		userLocalDataSource: FakeUserLocalDataRepository = FakeUserLocalDataRepository(),
		dispatcher: CoroutineDispatcher
	): SyncDataSource {
		return SyncDataSource(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = remoteDataSource,
			recordLocalDataSource = recordLocalDataSource,
			userLocalDataSource = userLocalDataSource,
			syncDispatcher = dispatcher
		)
	}
}

private class FakeSyncSettingsLocalDataSource(
	var onCooldown: Boolean,
	var syncRetryBackoffActive: Boolean = false,
	var syncRetryBackoffMarked: Boolean = false,
	var syncRetryBackoffCleared: Boolean = false,
	var syncRetryBackoffState: SyncRetryBackoffState = SyncRetryBackoffState(
		retryCount = 0,
		lastRetryAt = 0L,
		retryBackoffUntil = 0L
	),
	private val events: MutableList<String> = mutableListOf()
) : SyncSettingsLocalDataRepository {
	var cooldownMarked = false
	var syncedFeatureCooldownsMarked = false
	var staleFeatureCooldownsCleared = false
	var recoveryCooldownsCleared = false

	override suspend fun isSyncOnCooldown(): Boolean = onCooldown

	override suspend fun setSyncOnCooldown() {
		cooldownMarked = true
	}

	override suspend fun setSyncedFeatureCooldowns() {
		syncedFeatureCooldownsMarked = true
	}

	override suspend fun clearStaleFeatureCooldowns() {
		staleFeatureCooldownsCleared = true
	}

	override suspend fun clearRecoveryCooldowns() {
		recoveryCooldownsCleared = true
		events += "clear_recovery"
	}

	override suspend fun isSyncRetryBackoffActive(): Boolean = syncRetryBackoffActive

	override suspend fun markSyncRetryBackoff(_throwable: Throwable) {
		syncRetryBackoffMarked = true
		syncRetryBackoffActive = true
		syncRetryBackoffState = syncRetryBackoffState.copy(
			retryCount = syncRetryBackoffState.retryCount + 1,
			lastRetryAt = currentTimeMillis(),
			retryBackoffUntil = syncRetryBackoffState.retryBackoffUntil + 1L
		)
	}

	override suspend fun clearSyncRetryBackoff() {
		syncRetryBackoffCleared = true
		syncRetryBackoffActive = false
		syncRetryBackoffState = SyncRetryBackoffState(
			retryCount = 0,
			lastRetryAt = 0L,
			retryBackoffUntil = 0L
		)
	}

	override suspend fun getSyncRetryBackoffState(): SyncRetryBackoffState = syncRetryBackoffState
}

private class FakeSyncStatusRepository(
	initialValue: SyncStatus = SyncStatus.Healthy
) : SyncStatusRepository {
	private val syncStatus = MutableStateFlow(initialValue)
	val setStatuses = mutableListOf<SyncStatus>()

	override fun observeSyncStatus(): Flow<SyncStatus> = syncStatus

	override suspend fun getSyncStatus(): SyncStatus = syncStatus.value

	override suspend fun setSyncStatus(status: SyncStatus) {
		syncStatus.value = status
		setStatuses += status
	}

	override suspend fun reset() {
		syncStatus.value = SyncStatus.Healthy
	}
}

private class FakeSyncRemoteDataSource(
	private val result: SyncResult = SyncResult(
		record = DEFAULT_RECORD,
		user = DEFAULT_USER
	),
	private val throwable: Throwable? = null,
	private val blockUntilCompleted: Boolean = false,
	private val events: MutableList<String> = mutableListOf()
) : SyncRemoteDataRepository {
	private val releaseSync = CompletableDeferred<Unit>()
	var onSyncStarted: suspend () -> Unit = {}
	val syncPasswords = mutableListOf<String>()

	override suspend fun sync(password: String): SyncResult {
		syncPasswords += password
		events += "sync"
		onSyncStarted()

		if (blockUntilCompleted) {
			releaseSync.await()
		}

		throwable?.let { throw it }
		return result
	}

	fun complete() {
		releaseSync.complete(Unit)
	}
}

private class FakeAcademicRecordLocalDataRepository : AcademicRecordLocalDataRepository {
	val savedRecords = mutableListOf<VersionedAcademicRecord>()

	override fun observeAcademicRecordFlow(): Flow<AcademicRecord?> = flowOf(savedRecords.lastOrNull()?.record)

	override fun observeHasSyncedRecordFlow(): Flow<Boolean> = flowOf(savedRecords.isNotEmpty())

	override suspend fun hasAcademicRecord(): Boolean = savedRecords.isNotEmpty()

	override suspend fun getAcademicRecord(): AcademicRecord? = savedRecords.lastOrNull()?.record

	override suspend fun getRecordRevision(): Long? = savedRecords.lastOrNull()?.revision

	override suspend fun saveAcademicRecord(record: VersionedAcademicRecord) {
		savedRecords += record
	}

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		committed: Boolean
	): AcademicRecord? = null

	override suspend fun deleteAttemptOverride(attemptId: String): AcademicRecord? = null

	override suspend fun addSyntheticTerm(command: AcademicRecordMutation.AddSyntheticTerm): AcademicRecord? = null

	override suspend fun updateSyntheticTerm(command: AcademicRecordMutation.UpdateSyntheticTerm): AcademicRecord? = null

	override suspend fun deleteSyntheticTerm(termId: String): AcademicRecord? = null
}

private class FakeUserLocalDataRepository : LocalDataRepository {
	val updatedUsers = mutableListOf<User>()

	override fun getUserFlow(): Flow<User?> = flowOf(updatedUsers.lastOrNull())

	override suspend fun updateUser(user: User) {
		updatedUsers += user
	}
}

private val DEFAULT_RECORD = VersionedAcademicRecord(
	revision = 7L,
	record = AcademicRecord(
		id = "user-1",
		profile = AcademicProfile(
			userId = "user-1",
			identityCardNumber = 12345678,
			usbId = "12-34567",
			email = "12-34567@usb.ve",
			firstNames = "Ada",
			lastNames = "Lovelace",
			careerName = "Ingenieria",
			careerCode = 12039,
			scholarship = false
		)
	)
)

private val DEFAULT_USER = User(
	id = "user-1",
	cid = "12345678",
	usbId = "12-34567",
	email = "12-34567@usb.ve",
	pictureUrl = "https://example.com/profile.jpg",
	fullName = "Ada Lovelace",
	firstNames = "Ada",
	lastNames = "Lovelace",
	careerName = "Ingenieria",
	careerCode = 12039,
	scholarship = false,
	grade = 4.5,
	enrolledSubjects = 5,
	enrolledCredits = 16,
	approvedSubjects = 20,
	approvedCredits = 70,
	retiredSubjects = 1,
	retiredCredits = 3,
	failedSubjects = 2,
	failedCredits = 8,
	lastUpdate = 1234567890L
)
