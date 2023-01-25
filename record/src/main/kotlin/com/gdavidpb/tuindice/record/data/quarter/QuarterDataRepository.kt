package com.gdavidpb.tuindice.record.data.quarter

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.record.data.quarter.source.LocalDataSource
import com.gdavidpb.tuindice.record.data.quarter.source.RemoteDataSource
import com.gdavidpb.tuindice.record.domain.model.UpdateSubject
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository

class QuarterDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val networkRepository: NetworkRepository
) : QuarterRepository {
	override suspend fun getQuarters(uid: String): List<Quarter> {
		val isNetworkAvailable = networkRepository.isAvailable()
		val isUpdated = localDataSource.isUpdated(uid)

		if (isNetworkAvailable && !isUpdated)
			remoteDataSource.getQuarters().also { quarters ->
				localDataSource.saveQuarters(uid, quarters)
			}

		return localDataSource.getQuarters(uid)
	}

	override suspend fun removeQuarter(uid: String, qid: String) {
		remoteDataSource.removeQuarter(qid)
		localDataSource.removeQuarter(uid, qid)
	}

	override suspend fun updateSubject(uid: String, sid: String, subject: UpdateSubject): Subject {
		val updateSubject = remoteDataSource.updateSubject(subject)

		localDataSource.saveSubjects(uid, updateSubject)

		return updateSubject
	}
}