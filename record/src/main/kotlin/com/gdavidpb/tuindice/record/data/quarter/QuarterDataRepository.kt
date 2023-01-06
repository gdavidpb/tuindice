package com.gdavidpb.tuindice.record.data.quarter

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.record.data.quarter.source.LocalDataSource
import com.gdavidpb.tuindice.record.data.quarter.source.RemoteDataSource
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.request.UpdateQuarterRequest

class QuarterDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val networkRepository: NetworkRepository
) : QuarterRepository {
	override suspend fun getQuarters(uid: String, forceRemote: Boolean): List<Quarter> {
		val isNetworkAvailable = networkRepository.isAvailable()

		if (isNetworkAvailable && forceRemote)
			remoteDataSource.getQuarters().also { quarters ->
				localDataSource.saveQuarters(uid, quarters)
			}

		return localDataSource.getQuarters(uid)
	}

	override suspend fun removeQuarter(uid: String, qid: String) {
		remoteDataSource.removeQuarter(qid)
		localDataSource.removeQuarter(uid, qid)
	}

	override suspend fun updateQuarter(
		uid: String,
		qid: String,
		request: UpdateQuarterRequest
	): Quarter {
		return remoteDataSource.updateQuarter(qid, request)
	}
}