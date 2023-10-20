package com.gdavidpb.tuindice.record.data.quarter

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.record.data.quarter.source.LocalDataSource
import com.gdavidpb.tuindice.record.data.quarter.source.RemoteDataSource
import com.gdavidpb.tuindice.record.data.quarter.source.SettingsDataSource
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.transform

class QuarterDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val settingsDataSource: SettingsDataSource
) : QuarterRepository {
	override suspend fun getQuarters(
		uid: String
	): Flow<List<Quarter>> {
		return localDataSource.getQuarters(uid)
			.distinctUntilChanged()
			.transform { localQuarters ->
				val isOnCooldown = settingsDataSource.isGetQuartersOnCooldown()

				if (isOnCooldown)
					emit(localQuarters)
				else {
					if (localQuarters.isNotEmpty()) emit(localQuarters)

					val remoteQuarters = remoteDataSource.getQuarters()

					localDataSource.saveQuarters(uid, remoteQuarters)

					settingsDataSource.setGetQuartersOnCooldown()

					emit(remoteQuarters)
				}
			}
	}

	override suspend fun removeQuarter(uid: String, remove: QuarterRemove) {
		localDataSource.removeQuarter(uid, remove)

		remoteDataSource.removeQuarter(remove)
	}

	override suspend fun updateSubject(uid: String, update: SubjectUpdate) {
		localDataSource.updateSubject(uid, update)

		if (update.dispatchToRemote) {
			val subject = remoteDataSource.updateSubject(update)

			localDataSource.saveSubjects(uid, listOf(subject))
		}
	}
}