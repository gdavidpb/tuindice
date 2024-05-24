package com.gdavidpb.tuindice.record.data.repository.quarter.source.store

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.record.data.repository.quarter.LocalDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.SettingsDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toQuarter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.SourceOfTruth

class QuarterSourceOfTruth(
	private val localDataSource: LocalDataSource,
	private val settingsDataSource: SettingsDataSource
) : SourceOfTruth<QuarterKey, List<LocalQuarter>, List<Quarter>> by SourceOfTruth.of(
	reader = { key: QuarterKey ->
		require(key is QuarterKey.Read || key is QuarterKey.Compute)

		when (key) {
			is QuarterKey.Read.All ->
				localDataSource.getQuartersFlow(
					uid = key.uid
				).map { quarters -> quarters.map { quarter -> quarter.toQuarter() } }

			is QuarterKey.Read.ById ->
				flow {
					val quarter = localDataSource
						.getQuarter(
							uid = key.uid,
							qid = key.qid
						)
						?.toQuarter()

					val quarters = listOfNotNull(quarter)

					emit(quarters)
				}

			is QuarterKey.Compute.BySetSubjectGrade ->
				flow {
					val updatedQuarters = localDataSource
						.computeSetSubjectGrade(
							uid = key.uid,
							qid = key.qid,
							sid = key.sid,
							grade = key.grade
						)

					emit(updatedQuarters)
				}.map { quarters -> quarters.map { quarter -> quarter.toQuarter() } }

			else ->
				throw IllegalStateException()
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

			is QuarterKey.Read.ById ->
				localDataSource.saveQuarters(
					uid = key.uid,
					quarters = input
				)

			is QuarterKey.Write.SaveAll ->
				localDataSource.saveQuarters(
					uid = key.uid,
					quarters = input
				)

			is QuarterKey.Remove.ById ->
				localDataSource.removeQuarter(
					uid = key.uid,
					qid = input.first().id
				)

			is QuarterKey.Compute.BySetSubjectGrade -> {}
		}
	},
	delete = { key ->
		require(key is QuarterKey.Remove)

		when (key) {
			is QuarterKey.Remove.ById ->
				localDataSource.removeQuarter(
					uid = key.uid,
					qid = key.qid
				)
		}
	}
)