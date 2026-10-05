package com.gdavidpb.tuindice.data.repository.sync

import com.gdavidpb.tuindice.base.domain.model.EnrollmentSituation
import com.gdavidpb.tuindice.base.domain.model.SyncPolicy
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncReportSources
import com.gdavidpb.tuindice.base.domain.model.SyncReportStatus
import com.gdavidpb.tuindice.base.domain.model.SyncSourceReport
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.data.model.SyncResult
import com.gdavidpb.tuindice.data.source.sync.SyncDataSource
import com.gdavidpb.tuindice.data.source.sync.SyncRemoteException
import com.gdavidpb.tuindice.data.source.sync.SyncResultLocalDataSource
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import com.gdavidpb.tuindice.testkit.ktor.serverResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

// The university states a sync can report beyond plain success or failure: an annulled enrollment
// (the situation travels with a Success source), a student with no record yet, and a record DST
// refuses to read.
@OptIn(ExperimentalCoroutinesApi::class)
class SyncUniversityStatesContractTest {
	@Test
	fun scheduleSync_notEnrolled_persistsSituationAndMarksTheEnrollmentRefresh() = runTest {
		val situation = EnrollmentSituation(code = "01", description = "ANULADA")
		val report = annulledReport(situation)
		val syncStatusRepository = FakeSyncStatusRepository()
		val repository = createRepository(
			settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false),
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = FakeSyncRemoteDataSource(
				result = SyncResult(record = DEFAULT_RECORD, user = DEFAULT_USER, sync = report)
			),
			dispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "secret123", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(report.copy(enrollmentReadAt = DEFAULT_USER.lastUpdate), syncStatusRepository.getSyncReport())
		assertEquals(DEFAULT_USER.lastUpdate, syncStatusRepository.getSyncReport().enrollmentReadAt)
	}

	@Test
	fun scheduleSync_partialSync_keepsTheSituationOfThePreviousReportAndTheRefreshMark() = runTest {
		val situation = EnrollmentSituation(code = "06")
		val syncStatusRepository = FakeSyncStatusRepository(
			initialReport = annulledReport(situation).copy(enrollmentReadAt = 900L)
		)
		val repository = createRepository(
			settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false),
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = FakeSyncRemoteDataSource(
				result = SyncResult(
					record = DEFAULT_RECORD,
					user = DEFAULT_USER,
					sync = SyncReport.partialEnrollmentUnavailable()
				)
			),
			dispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "secret123", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		val saved = syncStatusRepository.getSyncReport()

		assertEquals(SyncSourceStatus.Unavailable, saved.sources.enrollment.status)
		assertEquals(situation, saved.sources.enrollment.situation)
		assertEquals(900L, saved.enrollmentReadAt)
	}

	@Test
	fun scheduleSync_aSyncThatReadTheEnrollmentWithoutSituation_clearsThePreviousOne() = runTest {
		val syncStatusRepository = FakeSyncStatusRepository(
			initialReport = annulledReport(EnrollmentSituation(code = "15"))
		)
		val repository = createRepository(
			settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false),
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = FakeSyncRemoteDataSource(),
			dispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "secret123", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(
			SyncReport.success().copy(enrollmentReadAt = DEFAULT_USER.lastUpdate),
			syncStatusRepository.getSyncReport()
		)
	}

	@Test
	fun scheduleSync_failureWithoutBody_keepsTheSituationOfThePreviousReport() = runTest {
		val situation = EnrollmentSituation(code = "12")
		val syncStatusRepository = FakeSyncStatusRepository(initialReport = annulledReport(situation))
		val repository = createRepository(
			settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false),
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = FakeSyncRemoteDataSource(
				throwable = serverResponseException(
					statusCode = HttpStatusCode.InternalServerError,
					path = "/record/v5/sync"
				)
			),
			dispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "secret123", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(situation, syncStatusRepository.getSyncReport().sources.enrollment.situation)
	}

	@Test
	fun scheduleSync_marksNewStudentNoRecord_keepsTheCooldownAndArmsNoRetry() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false)
		val syncStatusRepository = FakeSyncStatusRepository()
		val repository = createRepository(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = FakeSyncRemoteDataSource(
				throwable = SyncRemoteException(
					statusCode = HttpStatusCode.FailedDependency,
					syncReport = null,
					conflictReason = "NEW_STUDENT_NO_RECORD",
					cause = clientRequestException(
						statusCode = HttpStatusCode.FailedDependency,
						path = "/record/v5/sync"
					)
				)
			),
			dispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "secret123", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(SyncStatus.NewStudentNoRecord, syncStatusRepository.getSyncStatus())
		assertEquals(true, settingsDataSource.cooldownMarked)
		assertEquals(false, settingsDataSource.syncRetryBackoffMarked)
	}

	@Test
	fun scheduleSync_marksRecordAccessDenied_whenTheServiceNamesThatReason() = runTest {
		val syncStatusRepository = FakeSyncStatusRepository()
		val repository = createRepository(
			settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false),
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = FakeSyncRemoteDataSource(
				throwable = SyncRemoteException(
					statusCode = HttpStatusCode.ServiceUnavailable,
					syncReport = null,
					conflictReason = "DST_RECORD_ACCESS_DENIED",
					cause = serverResponseException(
						statusCode = HttpStatusCode.ServiceUnavailable,
						path = "/record/v5/sync"
					)
				)
			),
			dispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "secret123", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(SyncStatus.RecordAccessDenied, syncStatusRepository.getSyncStatus())
	}

	private fun annulledReport(situation: EnrollmentSituation): SyncReport {
		return SyncReport(
			status = SyncReportStatus.Success,
			sources = SyncReportSources(
				record = SyncSourceReport(SyncSourceStatus.Success),
				enrollment = SyncSourceReport(SyncSourceStatus.Success, situation)
			)
		)
	}

	private fun createRepository(
		settingsDataSource: SyncSettingsLocalDataRepository,
		syncStatusRepository: SyncStatusRepository,
		remoteDataSource: SyncRemoteDataRepository,
		dispatcher: CoroutineDispatcher
	): SyncDataSource {
		return SyncDataSource(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = remoteDataSource,
			syncResultLocalDataSource = SyncResultLocalDataSource(
				recordLocalDataSource = FakeAcademicRecordLocalDataRepository(),
				userLocalDataSource = FakeUserLocalDataRepository()
			),
			pensumRevalidationRepository = FakePensumRevalidationRepository(),
			coroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
		)
	}
}
