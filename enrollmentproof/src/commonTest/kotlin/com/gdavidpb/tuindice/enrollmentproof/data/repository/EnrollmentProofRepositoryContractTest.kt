package com.gdavidpb.tuindice.enrollmentproof.data.repository

import com.gdavidpb.tuindice.enrollmentproof.domain.exception.EnrollmentProofNotFoundException
import com.gdavidpb.tuindice.enrollmentproof.testing.CURRENT_QUARTER_NAME
import com.gdavidpb.tuindice.enrollmentproof.testing.DEFAULT_ENROLLMENT_PROOF
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeDatabaseDataSource
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeEnrollmentProofApiDataSource
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.RecordingStorageDataSource
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
		val storageDataSource = RecordingStorageDataSource()
		val repository = EnrollmentProofDataRepository(
			databaseDataSource = FakeDatabaseDataSource(),
			enrollmentProofApiDataSource = apiDataSource,
			storageDataSource = storageDataSource,
			networkRepository = FakeNetworkRepository(isAvailable = true)
		)

		val enrollmentProof = repository.getEnrollmentProof()

		assertEquals(DEFAULT_ENROLLMENT_PROOF, enrollmentProof)
		assertEquals(1, apiDataSource.invocationCount)
		assertEquals(
			listOf(CURRENT_QUARTER_NAME to DEFAULT_ENROLLMENT_PROOF),
			storageDataSource.savedProofs
		)
	}

	@Test
	fun getEnrollmentProof_returnsCachedFileWithoutCallingApiWhenFileAlreadyExists() = runTest {
		val apiDataSource = FakeEnrollmentProofApiDataSource()
		val storageDataSource = RecordingStorageDataSource(
			initialFiles = mapOf(CURRENT_QUARTER_NAME to DEFAULT_ENROLLMENT_PROOF)
		)
		val repository = EnrollmentProofDataRepository(
			databaseDataSource = FakeDatabaseDataSource(),
			enrollmentProofApiDataSource = apiDataSource,
			storageDataSource = storageDataSource,
			networkRepository = FakeNetworkRepository(isAvailable = true)
		)

		val enrollmentProof = repository.getEnrollmentProof()

		assertEquals(DEFAULT_ENROLLMENT_PROOF, enrollmentProof)
		assertEquals(0, apiDataSource.invocationCount)
		assertEquals(emptyList(), storageDataSource.savedProofs)
	}

	@Test
	fun getEnrollmentProof_returnsCachedFileWhenNetworkIsUnavailable() = runTest {
		val apiDataSource = FakeEnrollmentProofApiDataSource()
		val storageDataSource = RecordingStorageDataSource(
			initialFiles = mapOf(CURRENT_QUARTER_NAME to DEFAULT_ENROLLMENT_PROOF)
		)
		val repository = EnrollmentProofDataRepository(
			databaseDataSource = FakeDatabaseDataSource(),
			enrollmentProofApiDataSource = apiDataSource,
			storageDataSource = storageDataSource,
			networkRepository = FakeNetworkRepository(isAvailable = false)
		)

		val enrollmentProof = repository.getEnrollmentProof()

		assertEquals(DEFAULT_ENROLLMENT_PROOF, enrollmentProof)
		assertEquals(0, apiDataSource.invocationCount)
	}

	@Test
	fun getEnrollmentProof_throwsNotFoundWhenCurrentQuarterIsMissing() = runTest {
		val repository = EnrollmentProofDataRepository(
			databaseDataSource = FakeDatabaseDataSource(currentQuarterName = null),
			enrollmentProofApiDataSource = FakeEnrollmentProofApiDataSource(),
			storageDataSource = RecordingStorageDataSource(),
			networkRepository = FakeNetworkRepository(isAvailable = true)
		)

		assertFailsWith<EnrollmentProofNotFoundException> {
			repository.getEnrollmentProof()
		}
	}
}
