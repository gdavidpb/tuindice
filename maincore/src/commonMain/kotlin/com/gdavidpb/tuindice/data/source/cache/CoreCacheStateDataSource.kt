package com.gdavidpb.tuindice.data.source.cache

import com.gdavidpb.tuindice.domain.repository.CoreCacheStateRepository
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataRepository
import kotlinx.coroutines.flow.first

class CoreCacheStateDataSource(
	private val userLocalDataSource: LocalDataRepository,
	private val recordLocalDataSource: AcademicRecordLocalDataRepository
) : CoreCacheStateRepository {
	override suspend fun requiresBaseRehydration(): Boolean {
		if (userLocalDataSource.getUserFlow().first() == null) return true
		if (!recordLocalDataSource.hasAcademicRecord()) return true

		return !recordLocalDataSource.observeHasSyncedRecordFlow().first()
	}
}
