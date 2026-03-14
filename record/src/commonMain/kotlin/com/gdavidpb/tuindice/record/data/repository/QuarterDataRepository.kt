package com.gdavidpb.tuindice.record.data.repository

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.record.data.repository.quarter.mapper.toLocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.mapper.toQuarter
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class QuarterDataRepository(
	private val localDataSource: QuarterLocalDataSource,
	private val remoteDataSource: QuarterRemoteDataSource,
	private val settingsDataSource: QuarterSettingsDataSource
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
		remoteDataSource.removeQuarter(qid = remove.id)
		localDataSource.removeQuarter(qid = remove.id)
	}

	override suspend fun setSubjectGrade(set: SubjectGradeSet) {
		if (!set.commit) {
			localDataSource.setSubjectGradeAndRecompute(
				qid = set.quarterId,
				sid = set.id,
				grade = set.grade,
				commit = false
			)
			return
		}

		runCatching {
			remoteDataSource.setSubjectGrade(
				qid = set.quarterId,
				sid = set.id,
				grade = set.grade
			)
		}.onSuccess { remoteQuarters ->
			localDataSource.saveQuarters(
				remoteQuarters.map { quarter -> quarter.toLocalQuarter() }
			)
		}.onFailure {
			localDataSource.clearSubjectGradePreview(
				qid = set.quarterId,
				sid = set.id
			)
			throw it
		}
	}
}
