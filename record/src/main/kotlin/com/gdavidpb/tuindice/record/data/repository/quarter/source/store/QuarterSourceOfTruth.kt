package com.gdavidpb.tuindice.record.data.repository.quarter.source.store

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.record.data.repository.quarter.LocalDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.SettingsDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toQuarterRemove
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toQuarterUpdate
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.SourceOfTruth

class QuarterSourceOfTruth(
	private val localDataSource: LocalDataSource,
	private val settingsDataSource: SettingsDataSource
) : SourceOfTruth<QuarterKey, List<LocalQuarter>, List<Quarter>> by SourceOfTruth.of(
	reader = { key: QuarterKey ->
		require(key is QuarterKey.Read)

		when (key) {
			is QuarterKey.Read.All ->
				localDataSource.getQuartersFlow(
					uid = key.uid
				).map { quarters -> quarters.map { quarter -> quarter.toQuarter() } }
		}
	},
	writer = { key: QuarterKey, input: List<LocalQuarter> ->
		when (key) {
			is QuarterKey.Read.All -> {
				settingsDataSource.setGetQuartersOnCooldown()

				localDataSource.saveQuarters(
					uid = key.uid,
					quarters = input
				)
			}

			is QuarterKey.Write.Update ->
				localDataSource.updateQuarter(
					uid = key.uid,
					update = input.first().toQuarterUpdate()
				)

			is QuarterKey.Remove.ById ->
				localDataSource.removeQuarter(
					uid = key.uid,
					remove = input.first().toQuarterRemove()
				)
		}
	},
	delete = { key ->
		require(key is QuarterKey.Remove)

		when (key) {
			is QuarterKey.Remove.ById ->
				localDataSource.removeQuarter(
					uid = key.uid,
					remove = QuarterRemove(id = key.qid)
				)
		}
	}
)