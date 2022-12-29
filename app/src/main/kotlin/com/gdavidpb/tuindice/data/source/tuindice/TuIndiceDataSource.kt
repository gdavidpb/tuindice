package com.gdavidpb.tuindice.data.source.tuindice

import com.gdavidpb.tuindice.base.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.repository.*

class TuIndiceDataSource(
	private val services: ServicesRepository,
	private val database: DatabaseRepository,
	private val storage: StorageRepository,
	private val network: NetworkRepository
) : TuIndiceRepository {
	override suspend fun getEnrollmentProof(quarter: Quarter): EnrollmentProof {
		val isNetworkAvailable = network.isAvailable()
		val isCached = storage.existsEnrollmentProof(quarter)

		return fetch(
			fetchRemoteWhen = isNetworkAvailable && !isCached,
			fetchLocal = { storage.getEnrollmentProof(quarter) },
			fetchRemote = { services.getEnrollmentProof() },
			cache = { enrollmentProof -> storage.saveEnrollmentProof(quarter, enrollmentProof) }
		)
	}

	override suspend fun getQuarters(uid: String): List<Quarter> {
		val isNetworkAvailable = network.isAvailable()
		val isUpdated = !database.isUpdated(uid)

		return fetch(
			fetchRemoteWhen = isNetworkAvailable && !isUpdated,
			fetchLocal = { database.getQuarters(uid) },
			fetchRemote = { services.getQuarters() },
			cache = { quarters -> database.addQuarter(uid, *quarters.toTypedArray()) }
		)
	}

	private suspend fun <T> fetch(
		fetchRemoteWhen: Boolean,
		fetchLocal: suspend () -> T,
		fetchRemote: suspend () -> T,
		cache: suspend (T) -> Unit
	): T {
		if (fetchRemoteWhen) fetchRemote().also { data -> cache(data) }

		return fetchLocal()
	}
}