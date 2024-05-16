package com.gdavidpb.tuindice.record.data.repository.quarter

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.utils.extension.noAwait
import com.gdavidpb.tuindice.record.data.repository.quarter.source.store.QuarterConverter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.store.QuarterFetcher
import com.gdavidpb.tuindice.record.data.repository.quarter.source.store.QuarterKey
import com.gdavidpb.tuindice.record.data.repository.quarter.source.store.QuarterReadResponse
import com.gdavidpb.tuindice.record.data.repository.quarter.source.store.QuarterSourceOfTruth
import com.gdavidpb.tuindice.record.data.repository.quarter.source.store.QuarterUpdater
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.QuarterUpdate
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.MutableStoreBuilder
import org.mobilenativefoundation.store.store5.StoreReadRequest

@OptIn(ExperimentalStoreApi::class)
class QuarterDataRepository(
	private val fetcher: QuarterFetcher,
	private val sourceOfTruth: QuarterSourceOfTruth,
	private val converter: QuarterConverter,
	private val updater: QuarterUpdater,
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val cacheDataSource: CacheDataSource,
	private val settingsDataSource: SettingsDataSource
) : MutableStore<QuarterKey, List<Quarter>> by MutableStoreBuilder.from(
	fetcher = fetcher,
	sourceOfTruth = sourceOfTruth,
	converter = converter
).build(
	updater = updater
), QuarterRepository {
	override suspend fun getQuartersFlow(uid: String): Flow<List<Quarter>> {
		val isOnCooldown = settingsDataSource.isGetQuartersOnCooldown()

		val quarters = if (isOnCooldown)
			stream<QuarterReadResponse>(
				request = StoreReadRequest.cached(
					key = QuarterKey.Read.All(uid),
					refresh = false
				)
			)
		else
			stream<QuarterReadResponse>(
				request = StoreReadRequest.fresh(
					key = QuarterKey.Read.All(uid),
					fallBackToSourceOfTruth = true
				)
			)

		return quarters
			.distinctUntilChanged()
			.mapNotNull { response -> response.dataOrNull() }
	}

	override suspend fun updateQuarter(uid: String, update: QuarterUpdate) {
		localDataSource.updateQuarter(uid, update)

		val quarter = localDataSource.getQuarter(
			uid = uid,
			qid = update.id
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

		if (update.dispatchToRemote) noAwait { remoteDataSource.updateQuarter(update) }
	}

	override suspend fun removeQuarter(uid: String, remove: QuarterRemove) {
		clear(
			QuarterKey.Remove.ById(
				uid = uid,
				qid = remove.id
			)
		)
	}
}