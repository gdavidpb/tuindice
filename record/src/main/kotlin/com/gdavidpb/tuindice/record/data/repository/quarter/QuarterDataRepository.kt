package com.gdavidpb.tuindice.record.data.repository.quarter

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.mapper.toRemoteQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toLocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toQuarter
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class QuarterDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val settingsDataSource: SettingsDataSource
) : QuarterRepository {
	override suspend fun getQuartersFlow(): Flow<List<Quarter>> {
		val isOnCooldown = settingsDataSource.isGetQuartersOnCooldown()

		if (!isOnCooldown) {
			val remoteQuarters = remoteDataSource.getQuarters()
			val localQuarters = remoteQuarters.map { it.toLocalQuarter() }

			localDataSource.saveQuarters(localQuarters)
			settingsDataSource.setGetQuartersOnCooldown()
		}

		return localDataSource.getQuartersFlow()
			.map { localQuarters -> localQuarters.map { it.toQuarter() } }
	}

	override suspend fun getQuarters(): List<Quarter> {
		return getQuartersFlow().first()
	}

	override suspend fun removeQuarter(remove: QuarterRemove) {
		localDataSource.removeQuarter(qid = remove.id)
		remoteDataSource.removeQuarter(qid = remove.id)
	}

	override suspend fun setSubjectGrade(set: SubjectGradeSet) {
		val result = localDataSource.setSubjectGradeAndRecompute(
			qid = set.quarterId,
			sid = set.id,
			grade = set.grade,
			commit = set.commit
		)

		if (set.commit && result.updatedTargetQuarter != null) {
			val quarterToUpdate = result.updatedTargetQuarter
				.toQuarter()
				.toRemoteQuarter()

			remoteDataSource.addQuarter(quarter = quarterToUpdate)
		}
	}
}