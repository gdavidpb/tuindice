package com.gdavidpb.tuindice.subjects.data.source

import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.subjects.data.repository.SubjectStatsApiDataRepository
import com.gdavidpb.tuindice.subjects.data.repository.SubjectStatsLocalDataRepository
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import com.gdavidpb.tuindice.subjects.domain.repository.SubjectStatsRepository

class SubjectStatsDataSource(
	private val localDataSource: SubjectStatsLocalDataRepository,
	private val apiDataSource: SubjectStatsApiDataRepository
) : SubjectStatsRepository {
	override suspend fun getSubjectDetail(
		subjectCode: String,
		forceRefresh: Boolean
	): SubjectDetailResult {
		val cached = localDataSource.getSubjectDetail(subjectCode)
		val now = currentTimeMillis()

		if (!forceRefresh && cached?.isFresh(now) == true) {
			return cached
		}

		return runCatching {
			apiDataSource.getSubjectDetail(subjectCode).also { result ->
				localDataSource.saveSubjectDetail(result)
			}
		}.getOrElse { throwable ->
			cached?.takeIf { result -> result.canFallback(now) } ?: throw throwable
		}
	}
}

private fun SubjectDetailResult.isFresh(now: Long): Boolean {
	return when (this) {
		is SubjectDetailResult.Ready -> detail.expiresAt > now
		is SubjectDetailResult.Unavailable -> expiresAt > now
	}
}

private fun SubjectDetailResult.canFallback(now: Long): Boolean {
	return when (this) {
		is SubjectDetailResult.Ready -> detail.expiresAt !in (now + 1)..now
		is SubjectDetailResult.Unavailable -> expiresAt !in (now + 1)..now
	}
}
