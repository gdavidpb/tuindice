package com.gdavidpb.tuindice.record.data.quarter

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.persistence.data.api.request.SubjectDataRequest
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionAction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionType
import com.gdavidpb.tuindice.persistence.domain.repository.TrackerRepository
import com.gdavidpb.tuindice.persistence.utils.extension.createInProgressTransaction
import com.gdavidpb.tuindice.record.data.quarter.source.LocalDataSource
import com.gdavidpb.tuindice.record.data.quarter.source.RemoteDataSource
import com.gdavidpb.tuindice.record.data.quarter.source.SettingsDataSource
import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform

class QuarterDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val settingsDataSource: SettingsDataSource,
	private val trackerRepository: TrackerRepository
) : QuarterRepository {
	override suspend fun getQuarters(uid: String): Flow<List<Quarter>> {
		return localDataSource.getQuarters(uid)
			.distinctUntilChanged()
			.transform { quarters ->
				val isOnCooldown = settingsDataSource.isGetQuartersOnCooldown()

				if (quarters.isNotEmpty() && !isOnCooldown)
					emit(quarters)
				else
					emit(remoteDataSource.getQuarters().also { response ->
						localDataSource.saveQuarters(uid, response)
						settingsDataSource.setGetQuartersCooldown()
					})
			}
	}

	override suspend fun removeQuarter(uid: String, qid: String) {
		with(localDataSource) {
			removeQuarter(uid, qid)

			val transaction = createInProgressTransaction(
				reference = qid,
				type = TransactionType.QUARTER,
				action = TransactionAction.DELETE
			)

			trackerRepository.trackTransaction(transaction) {
				remoteDataSource.removeQuarter(qid)
			}
		}
	}

	override suspend fun updateSubject(uid: String, update: SubjectUpdate) {
		with(localDataSource) {
			updateSubject(uid, update)

			if (update.dispatchToRemote) {
				val transaction = createInProgressTransaction(
					reference = update.subjectId,
					type = TransactionType.SUBJECT,
					action = TransactionAction.UPDATE,
					data = SubjectDataRequest(
						id = update.subjectId,
						grade = update.grade
					)
				)

				trackerRepository.trackTransaction(transaction) {
					remoteDataSource.updateSubject(update).also { subject ->
						saveSubjects(uid, listOf(subject))
					}
				}
			}
		}
	}
}