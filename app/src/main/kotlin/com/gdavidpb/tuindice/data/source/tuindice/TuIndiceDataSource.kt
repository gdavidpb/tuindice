package com.gdavidpb.tuindice.data.source.tuindice

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ServicesRepository
import com.gdavidpb.tuindice.base.domain.repository.TuIndiceRepository

class TuIndiceDataSource(
	private val servicesRepository: ServicesRepository,
	private val databaseRepository: DatabaseRepository,
	private val networkRepository: NetworkRepository
) : TuIndiceRepository {
	override suspend fun getQuarters(uid: String): List<Quarter> {
		return when {
			!networkRepository.isAvailable() -> databaseRepository.getQuarters(uid)
			databaseRepository.isUpdated(uid) -> databaseRepository.getQuarters(uid)
			else -> servicesRepository.getQuarters().also { servicesQuarters ->
				databaseRepository.addQuarter(uid, *servicesQuarters.toTypedArray())
			}
		}
	}
}