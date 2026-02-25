package com.gdavidpb.tuindice.enrollmentproof.data.repository

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class EnrollmentProofDataRepositoryTest {
	@Test
	fun getEnrollmentProof_whenOnlineAndMissing_fetchesAndCachesRemoteProof() = runBlocking {
		val quarterName = "2026-1"
		val remoteProof = EnrollmentProof(
			source = "/tmp/enrollment-proof-2026-1.pdf",
			content = "remote-content"
		)
		val storage = FakeStorageDataSource()
		val api = FakeEnrollmentProofApiDataSource(remoteProof)
		val repository = EnrollmentProofDataRepository(
			databaseDataSource = FakeDatabaseDataSource(quarterName),
			enrollmentProofApiDataSource = api,
			storageDataSource = storage,
			networkRepository = FakeNetworkStatusGateway(available = true)
		)

		val result = repository.getEnrollmentProof()

		assertEquals(remoteProof, result)
		assertEquals(1, api.calls)
		assertEquals(remoteProof, storage.entries[quarterName])
		assertEquals(listOf(quarterName), storage.savedKeys)
	}

	@Test
	fun getEnrollmentProof_whenOfflineAndCached_returnsStorageWithoutApiCall() = runBlocking {
		val quarterName = "2026-2"
		val cachedProof = EnrollmentProof(
			source = "/tmp/enrollment-proof-2026-2.pdf",
			content = "cached-content"
		)
		val storage = FakeStorageDataSource(
			initialEntries = mutableMapOf(quarterName to cachedProof)
		)
		val api = FakeEnrollmentProofApiDataSource(
			response = EnrollmentProof(
				source = "/tmp/unused.pdf",
				content = "unused-content"
			)
		)
		val repository = EnrollmentProofDataRepository(
			databaseDataSource = FakeDatabaseDataSource(quarterName),
			enrollmentProofApiDataSource = api,
			storageDataSource = storage,
			networkRepository = FakeNetworkStatusGateway(available = false)
		)

		val result = repository.getEnrollmentProof()

		assertEquals(cachedProof, result)
		assertEquals(0, api.calls)
		assertEquals(emptyList(), storage.savedKeys)
	}
}

private class FakeDatabaseDataSource(
	private val currentQuarterName: String?
) : DatabaseDataSource {
	override suspend fun getCurrentQuarterName(): String? = currentQuarterName
}

private class FakeEnrollmentProofApiDataSource(
	private val response: EnrollmentProof
) : EnrollmentProofApiDataSource {
	var calls: Int = 0

	override suspend fun getEnrollmentProof(): EnrollmentProof {
		calls++
		return response
	}
}

private class FakeStorageDataSource(
	initialEntries: MutableMap<String, EnrollmentProof> = mutableMapOf()
) : StorageDataSource {
	val entries: MutableMap<String, EnrollmentProof> = initialEntries
	val savedKeys: MutableList<String> = mutableListOf()

	override suspend fun getEnrollmentProof(name: String): EnrollmentProof {
		return checkNotNull(entries[name]) {
			"Enrollment proof not found for $name"
		}
	}

	override suspend fun enrollmentProofExists(name: String): Boolean {
		return entries.containsKey(name)
	}

	override suspend fun saveEnrollmentProof(name: String, enrollmentProof: EnrollmentProof) {
		savedKeys.add(name)
		entries[name] = enrollmentProof
	}
}

private class FakeNetworkStatusGateway(
	private val available: Boolean
) : NetworkRepository {
	override fun isAvailable(): Boolean = available
}
