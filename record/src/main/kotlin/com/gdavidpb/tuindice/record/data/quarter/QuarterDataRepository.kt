package com.gdavidpb.tuindice.record.data.quarter

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.record.data.quarter.source.LocalDataSource
import com.gdavidpb.tuindice.record.data.quarter.source.RemoteDataSource
import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform

class QuarterDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource
) : QuarterRepository {
	override suspend fun getQuarters(uid: String): Flow<List<Quarter>> {
		return localDataSource.getQuarters(uid)
			.distinctUntilChanged()
			.transform { quarters ->
				val isUpdated = localDataSource.isUpdated(uid)

				if (quarters.isNotEmpty() && isUpdated)
					emit(quarters)
				else
					emit(remoteDataSource.getQuarters().also { response ->
						localDataSource.saveQuarters(uid, response)
					})
			}
	}

	override suspend fun removeQuarter(uid: String, qid: String) {
		remoteDataSource.removeQuarter(qid)
		localDataSource.removeQuarter(uid, qid)
	}

	override suspend fun updateSubject(uid: String, update: SubjectUpdate): Subject {
		return if (update.dispatchToRemote)
			remoteDataSource.updateSubject(update).also { subject ->
				localDataSource.saveSubjects(uid, listOf(subject))
			}
		else
			localDataSource.updateSubject(uid, update)
	}
}