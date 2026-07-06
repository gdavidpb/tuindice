package com.gdavidpb.tuindice.enrollmentproof.data.repository

import com.gdavidpb.tuindice.enrollmentproof.data.source.EnrollmentProofDataSource
import com.gdavidpb.tuindice.enrollmentproof.domain.exception.EnrollmentProofNotFoundException
import com.gdavidpb.tuindice.enrollmentproof.domain.exception.EnrollmentProofOfflineException
import com.gdavidpb.tuindice.enrollmentproof.testing.CURRENT_QUARTER_NAME
import com.gdavidpb.tuindice.enrollmentproof.testing.DEFAULT_ENROLLMENT_PROOF
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeDatabaseDataSource
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeEnrollmentProofApiDataSource
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.RecordingStorageDataSource
import com.gdavidpb.tuindice.enrollmentproof.testing.clientRequestException
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
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
			credentialsRepository = credentialsRepository
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
			credentialsRepository = FakeCredentialsRepository(password = "secret123")
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
			credentialsRepository = FakeCredentialsRepository(password = "secret123")
		)

		val enrollmentProof = repository.getEnrollmentProof()

		assertEquals(DEFAULT_ENROLLMENT_PROOF, enrollmentProof)
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
			credentialsRepository = FakeCredentialsRepository(password = "secret123")
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
			credentialsRepository = FakeCredentialsRepository(password = "secret123")
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
			credentialsRepository = FakeCredentialsRepository(password = "secret123")
		)

		val enrollmentProof = repository.getEnrollmentProof()

		assertEquals(DEFAULT_ENROLLMENT_PROOF, enrollmentProof)
		assertEquals(0, apiDataSource.invocationCount)
	}

	@Test
	fun getEnrollmentProof_throwsNotFoundWhenCurrentQuarterIsMissing() = runTest {
		val repository = EnrollmentProofDataSource(
			databaseDataSource = FakeDatabaseDataSource(currentQuarterName = null),
			enrollmentProofApiDataSource = FakeEnrollmentProofApiDataSource(),
			storageDataSource = RecordingStorageDataSource(),
			networkRepository = FakeNetworkRepository(isAvailable = true),
			credentialsRepository = FakeCredentialsRepository(password = "secret123")
		)

		assertFailsWith<EnrollmentProofNotFoundException> {
			repository.getEnrollmentProof()
		}
	}
}
