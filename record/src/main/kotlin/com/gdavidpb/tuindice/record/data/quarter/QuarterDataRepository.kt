package com.gdavidpb.tuindice.record.data.quarter

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.persistence.data.room.model.TransactionAction
import com.gdavidpb.tuindice.persistence.data.room.model.TransactionType
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
				val isOnCooldown = localDataSource.isGetQuartersOnCooldown()

				if (quarters.isNotEmpty() && !isOnCooldown)
					emit(quarters)
				else
					emit(remoteDataSource.getQuarters().also { response ->
						localDataSource.saveQuarters(uid, response)
						localDataSource.setGetQuartersCooldown()
					})
			}
	}

	override suspend fun removeQuarter(uid: String, qid: String) {
		with(localDataSource) {
			removeQuarter(uid, qid)

			trackTransaction(
				reference = qid,
				type = TransactionType.QUARTER,
				action = TransactionAction.DELETE
			) {
				remoteDataSource.removeQuarter(qid)
			}
		}
	}

	override suspend fun updateSubject(uid: String, update: SubjectUpdate) {
		with(localDataSource) {
			updateSubject(uid, update)

			if (update.dispatchToRemote)
				trackTransaction(
					reference = update.subjectId,
					type = TransactionType.SUBJECT,
					action = TransactionAction.UPDATE
				) {
					remoteDataSource.updateSubject(update).also { subject ->
						saveSubjects(uid, listOf(subject))
					}
				}
		}
	}
}