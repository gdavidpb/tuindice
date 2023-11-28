package com.gdavidpb.tuindice.record.data.repository.quarter

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.utils.extension.noAwait
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform

class QuarterDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val cacheDataSource: CacheDataSource,
	private val settingsDataSource: SettingsDataSource
) : QuarterRepository {
	override suspend fun getQuartersStream(
		uid: String
	): Flow<List<Quarter>> {
		return localDataSource.getQuartersStream(uid)
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

	override suspend fun removeQuarter(
		uid: String,
		remove: QuarterRemove
	) {
		localDataSource.removeQuarter(uid, remove)
		noAwait { remoteDataSource.removeQuarter(remove) }
	}

	override suspend fun updateSubject(
		uid: String,
		update: SubjectUpdate
	) {
		localDataSource.updateSubject(uid, update)

		val quarter = localDataSource.getQuarter(
			uid = uid,
			qid = update.quarterId
		)

		val quarters = localDataSource.getQuarters(
			uid = uid
		)

		if (quarter != null)
			cacheDataSource.computeQuarters(
				uid = uid,
				origin = quarter,
				quarters = quarters
			).also { updatedQuarters ->
				if (updatedQuarters.isNotEmpty())
					localDataSource.saveQuarters(
						uid = uid,
						quarters = updatedQuarters
					)
			}

		if (update.dispatchToRemote) noAwait { remoteDataSource.updateSubject(update) }
	}
}