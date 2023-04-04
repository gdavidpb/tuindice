package com.gdavidpb.tuindice.record.data.quarter

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.quarter.QuarterRemoveTransaction
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectUpdateTransaction
import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.persistence.domain.repository.TrackerRepository
import com.gdavidpb.tuindice.record.data.quarter.source.LocalDataSource
import com.gdavidpb.tuindice.record.data.quarter.source.RemoteDataSource
import com.gdavidpb.tuindice.record.data.quarter.source.SettingsDataSource
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
	override suspend fun getQuarters(
		uid: String
	): Flow<List<Quarter>> {
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

	override suspend fun removeQuarter(
		uid: String,
		transaction: Transaction<QuarterRemoveTransaction>
	) {
		localDataSource.removeQuarter(uid, transaction)

		trackerRepository.trackTransaction(transaction) {
			remoteDataSource.removeQuarter(transaction)
		}
	}

	override suspend fun updateSubject(
		uid: String,
		transaction: Transaction<SubjectUpdateTransaction>
	) {
		localDataSource.updateSubject(uid, transaction)

		if (transaction.dispatchToRemote) {
			trackerRepository.trackTransaction(transaction) {
				val subject = remoteDataSource.updateSubject(transaction)

				localDataSource.saveSubjects(uid, listOf(subject))
			}
		}
	}
}