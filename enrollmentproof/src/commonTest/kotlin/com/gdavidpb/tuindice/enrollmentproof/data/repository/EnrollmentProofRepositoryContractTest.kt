package com.gdavidpb.tuindice.enrollmentproof.data.repository

import com.gdavidpb.tuindice.base.domain.model.EnrollmentSituation
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.enrollmentproof.data.source.EnrollmentProofDataSource
import com.gdavidpb.tuindice.enrollmentproof.domain.exception.EnrollmentProofNotFoundException
import com.gdavidpb.tuindice.enrollmentproof.domain.exception.EnrollmentProofOfflineException
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProofNotFoundReason
import com.gdavidpb.tuindice.enrollmentproof.testing.CURRENT_QUARTER_NAME
import com.gdavidpb.tuindice.enrollmentproof.testing.DEFAULT_ENROLLMENT_PROOF
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeDatabaseDataSource
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeEnrollmentProofApiDataSource
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.RecordingStorageDataSource
import com.gdavidpb.tuindice.enrollmentproof.testing.clientRequestException
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EnrollmentProofRepositoryContractTest {
	@Test
	fun getEnrollmentProof_fetchesFromApiAndCachesWhenNetworkIsAvailableAndFileIsMissing() = runTest {
		val apiDataSource = FakeEnrollmentProofApiDataSource(
			enrollmentProof = DEFAULT_ENROLLMENT_PROOF
		)
		val credentialsRepository = FakeCredentialsRepository(password = "secret123")
		val storageDataSource = RecordingStorageDataSource()
		val repository = EnrollmentProofDataSource(
			databaseDataSource = FakeDatabaseDataSource(),
			enrollmentProofApiDataSource = apiDataSource,
			storageDataSource = storageDataSource,
			networkRepository = FakeNetworkRepository(isAvailable = true),
			credentialsRepository = credentialsRepository,
			syncStatusRepository = FakeSyncStatusRepository()
		)

		val enrollmentProof = repository.getEnrollmentProof()

		assertEquals(DEFAULT_ENROLLMENT_PROOF, enrollmentProof)
		assertEquals(1, apiDataSource.invocationCount)
		assertEquals("secret123", apiDataSource.lastPassword)
		assertEquals(
			listOf(CURRENT_QUARTER_NAME to DEFAULT_ENROLLMENT_PROOF),
			storageDataSource.savedProofs
		)
	}

	@Test
	fun getEnrollmentProof_refreshesCachedFileWhenNetworkIsAvailable() = runTest {
		val refreshedProof = DEFAULT_ENROLLMENT_PROOF.copy(content = "dXBkYXRlZC1wcm9vZg==")
		val apiDataSource = FakeEnrollmentProofApiDataSource(enrollmentProof = refreshedProof)
		val storageDataSource = RecordingStorageDataSource(
			initialFiles = mapOf(CURRENT_QUARTER_NAME to DEFAULT_ENROLLMENT_PROOF)
		)
		val repository = EnrollmentProofDataSource(
			databaseDataSource = FakeDatabaseDataSource(),
			enrollmentProofApiDataSource = apiDataSource,
			storageDataSource = storageDataSource,
			networkRepository = FakeNetworkRepository(isAvailable = true),
			credentialsRepository = FakeCredentialsRepository(password = "secret123"),
			syncStatusRepository = FakeSyncStatusRepository()
		)

		val enrollmentProof = repository.getEnrollmentProof()

		assertEquals(refreshedProof, enrollmentProof)
		assertEquals(1, apiDataSource.invocationCount)
		assertEquals(
			listOf(CURRENT_QUARTER_NAME to refreshedProof),
			storageDataSource.savedProofs
		)
	}

	@Test
	fun getEnrollmentProof_keepsCachedFileWhenRefreshFails() = runTest {
		val apiDataSource = FakeEnrollmentProofApiDataSource(
			throwable = clientRequestException(HttpStatusCode.ServiceUnavailable)
		)
		val storageDataSource = RecordingStorageDataSource(
			initialFiles = mapOf(CURRENT_QUARTER_NAME to DEFAULT_ENROLLMENT_PROOF)
		)
		val repository = EnrollmentProofDataSource(
			databaseDataSource = FakeDatabaseDataSource(),
			enrollmentProofApiDataSource = apiDataSource,
			storageDataSource = storageDataSource,
			networkRepository = FakeNetworkRepository(isAvailable = true),
			credentialsRepository = FakeCredentialsRepository(password = "secret123"),
			syncStatusRepository = FakeSyncStatusRepository()
		)

		val enrollmentProof = repository.getEnrollmentProof()

		assertEquals(DEFAULT_ENROLLMENT_PROOF.copy(isFromCache = true), enrollmentProof)
		assertEquals(1, apiDataSource.invocationCount)
		assertEquals(emptyList(), storageDataSource.savedProofs)
	}

	@Test
	fun getEnrollmentProof_propagatesRefreshErrorWhenFileIsMissing() = runTest {
		val apiDataSource = FakeEnrollmentProofApiDataSource(
			throwable = clientRequestException(HttpStatusCode.ServiceUnavailable)
		)
		val repository = EnrollmentProofDataSource(
			databaseDataSource = FakeDatabaseDataSource(),
			enrollmentProofApiDataSource = apiDataSource,
			storageDataSource = RecordingStorageDataSource(),
			networkRepository = FakeNetworkRepository(isAvailable = true),
			credentialsRepository = FakeCredentialsRepository(password = "secret123"),
			syncStatusRepository = FakeSyncStatusRepository()
		)

		assertFailsWith<ClientRequestException> {
			repository.getEnrollmentProof()
		}
	}

	@Test
	fun getEnrollmentProof_throwsOfflineWhenNetworkIsUnavailableAndFileIsMissing() = runTest {
		val apiDataSource = FakeEnrollmentProofApiDataSource()
		val repository = EnrollmentProofDataSource(
			databaseDataSource = FakeDatabaseDataSource(),
			enrollmentProofApiDataSource = apiDataSource,
			storageDataSource = RecordingStorageDataSource(),
			networkRepository = FakeNetworkRepository(isAvailable = false),
			credentialsRepository = FakeCredentialsRepository(password = "secret123"),
			syncStatusRepository = FakeSyncStatusRepository()
		)

		assertFailsWith<EnrollmentProofOfflineException> {
			repository.getEnrollmentProof()
		}
		assertEquals(0, apiDataSource.invocationCount)
	}

	@Test
	fun getEnrollmentProof_returnsCachedFileWhenNetworkIsUnavailable() = runTest {
		val apiDataSource = FakeEnrollmentProofApiDataSource()
		val storageDataSource = RecordingStorageDataSource(
			initialFiles = mapOf(CURRENT_QUARTER_NAME to DEFAULT_ENROLLMENT_PROOF)
		)
		val repository = EnrollmentProofDataSource(
			databaseDataSource = FakeDatabaseDataSource(),
			enrollmentProofApiDataSource = apiDataSource,
			storageDataSource = storageDataSource,
			networkRepository = FakeNetworkRepository(isAvailable = false),
			credentialsRepository = FakeCredentialsRepository(password = "secret123"),
			syncStatusRepository = FakeSyncStatusRepository()
		)

		val enrollmentProof = repository.getEnrollmentProof()

		assertEquals(DEFAULT_ENROLLMENT_PROOF.copy(isFromCache = true), enrollmentProof)
		assertEquals(0, apiDataSource.invocationCount)
	}

	@Test
	fun getEnrollmentProof_throwsNotFoundWhenCurrentQuarterIsMissing() = runTest {
		val repository = EnrollmentProofDataSource(
			databaseDataSource = FakeDatabaseDataSource(currentQuarterName = null),
			enrollmentProofApiDataSource = FakeEnrollmentProofApiDataSource(),
			storageDataSource = RecordingStorageDataSource(),
			networkRepository = FakeNetworkRepository(isAvailable = true),
			credentialsRepository = FakeCredentialsRepository(password = "secret123"),
			syncStatusRepository = FakeSyncStatusRepository()
		)

		assertFailsWith<EnrollmentProofNotFoundException> {
			repository.getEnrollmentProof()
		}
	}

	@Test
	fun getEnrollmentProof_freshFileIsNotMarkedAsFromCache() = runTest {
		val repository = repositoryWith(
			api = FakeEnrollmentProofApiDataSource(enrollmentProof = DEFAULT_ENROLLMENT_PROOF),
			storage = RecordingStorageDataSource(mapOf(CURRENT_QUARTER_NAME to DEFAULT_ENROLLMENT_PROOF))
		)

		assertEquals(false, repository.getEnrollmentProof().isFromCache)
	}

	@Test
	fun getEnrollmentProof_savedCopyBacksOnlyTransientFailures() = runTest {
		listOf(
			clientRequestException(HttpStatusCode.ServiceUnavailable),
			clientRequestException(HttpStatusCode.InternalServerError),
			IllegalStateException("network is unreachable"),
			IllegalStateException("Request timeout has expired")
		).forEach { failure ->
			val repository = repositoryWith(
				api = FakeEnrollmentProofApiDataSource(throwable = failure),
				storage = RecordingStorageDataSource(mapOf(CURRENT_QUARTER_NAME to DEFAULT_ENROLLMENT_PROOF))
			)

			assertEquals(true, repository.getEnrollmentProof().isFromCache, "$failure")
		}
	}

	@Test
	fun getEnrollmentProof_savedCopyNeverBacksAConflictOrABadRequest() = runTest {
		val storage = RecordingStorageDataSource(mapOf(CURRENT_QUARTER_NAME to DEFAULT_ENROLLMENT_PROOF))

		listOf(HttpStatusCode.Conflict, HttpStatusCode.BadRequest).forEach { status ->
			val repository = repositoryWith(
				api = FakeEnrollmentProofApiDataSource(throwable = clientRequestException(status)),
				storage = storage
			)

			assertFailsWith<ClientRequestException>("$status") { repository.getEnrollmentProof() }
		}
	}

	@Test
	fun getEnrollmentProof_notFoundNeverOpensTheSavedCopy_andSaysWhyFromTheLastSync() = runTest {
		val storage = RecordingStorageDataSource(mapOf(CURRENT_QUARTER_NAME to DEFAULT_ENROLLMENT_PROOF))
		val annulled = SyncReport.success().withSituation(EnrollmentSituation(code = "06"))
		val notEnrolled = SyncReport.success().withEnrollmentStatus(SyncSourceStatus.NotEnrolled)

		mapOf(
			annulled to EnrollmentProofNotFoundReason.Annulled,
			notEnrolled to EnrollmentProofNotFoundReason.NotEnrolled,
			SyncReport.success() to EnrollmentProofNotFoundReason.Unknown
		).forEach { (report, expectedReason) ->
			val repository = repositoryWith(
				api = FakeEnrollmentProofApiDataSource(throwable = clientRequestException(HttpStatusCode.NotFound)),
				storage = storage,
				syncStatus = FakeSyncStatusRepository(initialReport = report)
			)

			val failure = assertFailsWith<EnrollmentProofNotFoundException> { repository.getEnrollmentProof() }

			assertEquals(expectedReason, failure.reason)
		}
	}

	@Test
	fun getEnrollmentProof_withoutACurrentTerm_explainsItLikeANotFound() = runTest {
		val repository = repositoryWith(
			database = FakeDatabaseDataSource(currentQuarterName = null),
			syncStatus = FakeSyncStatusRepository(
				initialReport = SyncReport.success().withSituation(EnrollmentSituation(code = "01"))
			)
		)

		val failure = assertFailsWith<EnrollmentProofNotFoundException> { repository.getEnrollmentProof() }

		assertEquals(EnrollmentProofNotFoundReason.Annulled, failure.reason)
	}

	private fun repositoryWith(
		api: FakeEnrollmentProofApiDataSource = FakeEnrollmentProofApiDataSource(),
		storage: RecordingStorageDataSource = RecordingStorageDataSource(),
		database: FakeDatabaseDataSource = FakeDatabaseDataSource(),
		syncStatus: FakeSyncStatusRepository = FakeSyncStatusRepository()
	) = EnrollmentProofDataSource(
		databaseDataSource = database,
		enrollmentProofApiDataSource = api,
		storageDataSource = storage,
		networkRepository = FakeNetworkRepository(isAvailable = true),
		credentialsRepository = FakeCredentialsRepository(password = "secret123"),
		syncStatusRepository = syncStatus
	)

	private fun SyncReport.withSituation(situation: EnrollmentSituation) = copy(
		sources = sources.copy(enrollment = sources.enrollment.copy(situation = situation))
	)

	private fun SyncReport.withEnrollmentStatus(status: SyncSourceStatus) = copy(
		sources = sources.copy(enrollment = sources.enrollment.copy(status = status))
	)
}
