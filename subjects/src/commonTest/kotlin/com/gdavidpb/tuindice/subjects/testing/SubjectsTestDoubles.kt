package com.gdavidpb.tuindice.subjects.testing

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.subjects.domain.model.SubjectAttemptBin
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetail as SubjectDetailModel
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDifficultyBand
import com.gdavidpb.tuindice.subjects.domain.model.SubjectGradeBin
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchResult
import com.gdavidpb.tuindice.subjects.domain.model.SubjectStatsSegment
import com.gdavidpb.tuindice.subjects.domain.repository.SubjectCatalogRepository
import com.gdavidpb.tuindice.subjects.domain.repository.SubjectStatsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RecordingSubjectStatsRepository(
	private val freshResult: SubjectDetailResult? = null,
	results: List<Any> = emptyList()
) : SubjectStatsRepository {
	private val queue = ArrayDeque(results)
	val freshCalls = mutableListOf<String>()
	val refreshCalls = mutableListOf<String>()

	override suspend fun getFreshSubjectDetail(subjectCode: String): SubjectDetailResult? {
		freshCalls += subjectCode
		return freshResult
	}

	override suspend fun refreshSubjectDetail(subjectCode: String): SubjectDetailResult {
		refreshCalls += subjectCode
		return when (val next = queue.removeFirst()) {
			is Throwable -> throw next
			is SubjectDetailResult -> next
			else -> error("Unsupported test result: $next")
		}
	}
}

class ControllableSubjectCatalogRepository(
	var localResults: List<SubjectSearchResult> = emptyList(),
	private val refreshResponses: ArrayDeque<Any> = ArrayDeque()
) : SubjectCatalogRepository {
	private val refreshGate = Channel<Unit>(Channel.UNLIMITED)

	var blockRefresh = false
	val observeCalls = mutableListOf<String>()
	val observeLimits = mutableListOf<Int>()
	val refreshCalls = mutableListOf<String>()
	val refreshLimits = mutableListOf<Int>()

	fun releaseRefresh() {
		refreshGate.trySend(Unit)
	}

	override fun observeSearchResults(
		query: String,
		limit: Int
	): Flow<List<SubjectSearchResult>> {
		observeCalls += query
		observeLimits += limit
		return flowOf(localResults)
	}

	override suspend fun refreshSearchResults(
		query: String,
		limit: Int
	) {
		refreshCalls += query
		refreshLimits += limit
		if (blockRefresh) refreshGate.receive()
		if (refreshResponses.isEmpty()) return

		when (val next = refreshResponses.removeFirst()) {
			is Throwable -> throw next
			is List<*> -> localResults = next.filterIsInstance<SubjectSearchResult>()
			else -> error("Unsupported search refresh result: $next")
		}
	}
}

fun subjectSearchResult(
	subjectCode: String,
	pensumStatus: AcademicPensumNodeStatus? = null
): SubjectSearchResult {
	return SubjectSearchResult(
		subjectCode = subjectCode,
		name = "Int. a las microondas y sus aplicaciones",
		credits = 3,
		gradingMode = GradingMode.NUMERIC,
		pensumStatus = pensumStatus
	)
}

fun readySubjectDetail(
	subjectCode: String = "MAT101",
	expiresAt: Long = Long.MAX_VALUE
): SubjectDetailResult.Ready {
	return SubjectDetailResult.Ready(
		detail = SubjectDetailModel(
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
