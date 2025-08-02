package com.gdavidpb.tuindice.record.data.repository.quarter

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.store.QuarterConverter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.store.QuarterFetcher
import com.gdavidpb.tuindice.record.data.repository.quarter.source.store.QuarterKey
import com.gdavidpb.tuindice.record.data.repository.quarter.source.store.QuarterReadResponse
import com.gdavidpb.tuindice.record.data.repository.quarter.source.store.QuarterSourceOfTruth
import com.gdavidpb.tuindice.record.data.repository.quarter.source.store.QuarterUpdater
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.MutableStoreBuilder
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import org.mobilenativefoundation.store.store5.impl.extensions.get

@OptIn(ExperimentalStoreApi::class)
class QuarterDataRepository(
	private val fetcher: QuarterFetcher,
	private val sourceOfTruth: QuarterSourceOfTruth,
	private val converter: QuarterConverter,
	private val updater: QuarterUpdater,
	private val settingsDataSource: SettingsDataSource
) : MutableStore<QuarterKey, List<Quarter>> by MutableStoreBuilder.from(
	fetcher = fetcher,
	sourceOfTruth = sourceOfTruth,
	converter = converter
).build(
	updater = updater
), QuarterRepository {
	override suspend fun getQuartersFlow(): Flow<List<Quarter>> {
		val isOnCooldown = settingsDataSource.isGetQuartersOnCooldown()

		val quarters = if (isOnCooldown)
			stream<QuarterReadResponse>(
				request = StoreReadRequest.cached(
					key = QuarterKey.Read.All,
					refresh = false
				)
			)
		else
			stream<QuarterReadResponse>(
				request = StoreReadRequest.fresh(
					key = QuarterKey.Read.All,
					fallBackToSourceOfTruth = true
				)
			)

		return quarters
			.distinctUntilChanged()
			.mapNotNull { response -> response.dataOrNull() }
	}

	override suspend fun getQuarters(): List<Quarter> {
		val isOnCooldown = settingsDataSource.isGetQuartersOnCooldown()

		val quarters = if (isOnCooldown)
			get<QuarterKey, List<Quarter>, QuarterReadResponse>(
				key = QuarterKey.Read.All
			)
		else
			fresh<QuarterKey, List<Quarter>, QuarterReadResponse>(
				key = QuarterKey.Read.All
			)

		return quarters
	}

	override suspend fun removeQuarter(remove: QuarterRemove) {
		clear(
			QuarterKey.Remove.ById(
				qid = remove.id
			)
		)
	}

	override suspend fun setSubjectGrade(set: SubjectGradeSet) {
		val updatedQuarters = get<QuarterKey, List<Quarter>, QuarterReadResponse>(
			QuarterKey.Compute.BySetSubjectGrade(
				qid = set.quarterId,
				sid = set.id,
				grade = set.grade
			)
		)

		write(
			StoreWriteRequest.of(
				key = QuarterKey.Write.SaveAll(
					quarters = updatedQuarters,
					dispatchToRemote = set.dispatchToRemote
				),
				value = updatedQuarters,
				created = System.currentTimeMillis()
			)
		)
	}
}