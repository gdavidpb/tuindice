package com.gdavidpb.tuindice.subjects.data.repository

import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.subjects.data.source.SubjectStatsDataSource
import com.gdavidpb.tuindice.subjects.domain.model.SubjectAttemptBin
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetail
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDifficultyBand
import com.gdavidpb.tuindice.subjects.domain.model.SubjectGradeBin
import com.gdavidpb.tuindice.subjects.domain.model.SubjectStatsSegment
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SubjectStatsRepositoryContractTest {
	@Test
	fun getFreshSubjectDetail_returnsFreshLocalCacheWithoutCallingApi() = runTest {
		val cached = readySubjectDetail(expiresAt = Long.MAX_VALUE)
		val local = FakeLocalDataRepository(initial = cached)
		val api = FakeApiDataRepository(
			nextResult = readySubjectDetail(
				subjectCode = "REMOTE",
				expiresAt = Long.MAX_VALUE
			)
		)
		val repository = SubjectStatsDataSource(localDataSource = local, apiDataSource = api)

		val result = repository.getFreshSubjectDetail(subjectCode = "MAT101")

		assertEquals(cached, result)
		assertEquals(emptyList(), api.subjectCodes)
		assertEquals(emptyList(), local.savedResults)
	}

	@Test
	fun getFreshSubjectDetail_returnsNullWhenLocalCacheIsExpired() = runTest {
		val expired = readySubjectDetail(expiresAt = 1L)
		val local = FakeLocalDataRepository(initial = expired)
		val api = FakeApiDataRepository(
			nextResult = readySubjectDetail(
				subjectCode = "REMOTE",
				expiresAt = Long.MAX_VALUE
			)
		)
		val repository = SubjectStatsDataSource(localDataSource = local, apiDataSource = api)

		val result = repository.getFreshSubjectDetail(subjectCode = "MAT101")

		assertEquals(null, result)
		assertEquals(emptyList(), api.subjectCodes)
		assertEquals(emptyList(), local.savedResults)
	}

	@Test
	fun refreshSubjectDetail_persistsRemoteResult() = runTest {
		val expired = readySubjectDetail(expiresAt = 1L)
		val remote = readySubjectDetail(subjectCode = "MAT101", expiresAt = Long.MAX_VALUE)
		val local = FakeLocalDataRepository(initial = expired)
		val api = FakeApiDataRepository(nextResult = remote)
		val repository = SubjectStatsDataSource(localDataSource = local, apiDataSource = api)

		val result = repository.refreshSubjectDetail(subjectCode = "MAT101")

		assertEquals(remote, result)
		assertEquals(listOf("MAT101"), api.subjectCodes)
		assertEquals(listOf<SubjectDetailResult>(remote), local.savedResults)
	}

	@Test
	fun refreshSubjectDetail_fallsBackToCachedValueWhenRemoteRefreshFails() = runTest {
		val expired = readySubjectDetail(expiresAt = 1L)
		val local = FakeLocalDataRepository(initial = expired)
		val api = FakeApiDataRepository(nextThrowable = IllegalStateException("boom"))
		val repository = SubjectStatsDataSource(localDataSource = local, apiDataSource = api)

		val result = repository.refreshSubjectDetail(subjectCode = "MAT101")

		assertEquals(expired, result)
		assertEquals(listOf("MAT101"), api.subjectCodes)
		assertEquals(emptyList(), local.savedResults)
	}

	@Test
	fun refreshSubjectDetail_throwsWhenRemoteFailsAndNoCacheExists() = runTest {
		val local = FakeLocalDataRepository(initial = null)
		val api = FakeApiDataRepository(nextThrowable = IllegalStateException("boom"))
		val repository = SubjectStatsDataSource(localDataSource = local, apiDataSource = api)

		assertFailsWith<IllegalStateException> {
			repository.refreshSubjectDetail(subjectCode = "MAT101")
		}
	}

	private class FakeLocalDataRepository(
		initial: SubjectDetailResult?
	) : SubjectStatsLocalDataRepository {
		private var result = initial
		val savedResults = mutableListOf<SubjectDetailResult>()

		override suspend fun getSubjectDetail(subjectCode: String): SubjectDetailResult? = result

		override suspend fun saveSubjectDetail(result: SubjectDetailResult) {
			this.result = result
			savedResults += result
		}
	}

	private class FakeApiDataRepository(
		private val nextResult: SubjectDetailResult? = null,
		private val nextThrowable: Throwable? = null
	) : SubjectStatsApiDataRepository {
		val subjectCodes = mutableListOf<String>()

		override suspend fun getSubjectDetail(subjectCode: String): SubjectDetailResult {
			subjectCodes += subjectCode
			nextThrowable?.let { throw it }
			return requireNotNull(nextResult)
		}
	}
}

private fun readySubjectDetail(
	subjectCode: String = "MAT101",
	expiresAt: Long
): SubjectDetailResult.Ready {
	return SubjectDetailResult.Ready(
		detail = SubjectDetail(
			id = subjectCode,
			name = "Calculo I",
			credits = 5,
			gradingMode = GradingMode.NUMERIC,
			generatedAt = 1710000000000,
			expiresAt = expiresAt,
			careerSegment = SubjectStatsSegment(
				sampleStudents = 18,
				closedAttempts = 24,
				numericLatestStudents = 18,
				latestApprovedCount = 12,
				latestFailedCount = 4,
				latestRetiredCount = 1,
				latestUnreportedCount = 1,
				averageGrade = 3.7,
				medianGrade = 4.0,
				stddevGrade = 0.8,
				firstAttemptPassRate = 0.55,
				approvalRate = 0.72,
				latestFailureRate = 0.22,
				latestWithdrawalRate = 0.06,
				retakeRate = 0.33,
				avgAttemptsToPass = 1.4,
				medianAttemptsToPass = 1.0,
				difficultyScore = 41,
				difficultyBand = SubjectDifficultyBand.MEDIUM,
				firstClosedTermStartAt = 1672444800000,
				lastClosedTermStartAt = 1704067200000,
				latestGradeBins = listOf(
					SubjectGradeBin(grade = 1, count = 1),
					SubjectGradeBin(grade = 5, count = 3)
				),
				allGradeBins = listOf(
					SubjectGradeBin(grade = 1, count = 2),
					SubjectGradeBin(grade = 5, count = 4)
				),
				attemptsToPassBins = listOf(
					SubjectAttemptBin(bucket = "1", count = 8),
					SubjectAttemptBin(bucket = "3_plus", count = 2)
				)
			),
			globalSegment = null
		)
	)
}
