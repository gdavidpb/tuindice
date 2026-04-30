package com.gdavidpb.tuindice.subjects.presentation.action

import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.subjects.domain.model.SubjectAttemptBin
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetail as SubjectDetailModel
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDifficultyBand
import com.gdavidpb.tuindice.subjects.domain.model.SubjectGradeBin
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.domain.model.SubjectStatsSegment
import com.gdavidpb.tuindice.subjects.domain.repository.SubjectStatsRepository
import com.gdavidpb.tuindice.subjects.domain.usecase.LoadSubjectDetailUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectDetailUseCase
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest

class SubjectDetailActionProcessorContractTest {
	@Test
	fun loadSubjectDetail_usesFreshLocalDataWithoutShowingLoadingRemote() = runTest {
		val cached = readySubjectDetail(expiresAt = Long.MAX_VALUE)
		val repository = RecordingSubjectStatsRepository(
			freshResult = cached,
			results = emptyList()
		)
		val processor = LoadSubjectDetailActionProcessor(
			loadSubjectDetailUseCase = LoadSubjectDetailUseCase(
				subjectStatsRepository = repository,
				reportingRepository = RecordingReportingRepository()
			)
		)

		val states = collectStates(
			initialState = SubjectDetail.State.Idle,
			mutations = processor.process(
				action = SubjectDetail.Action.LoadSubjectDetail(subjectCode = "MAT101"),
				sideEffect = {}
			)
		)

		assertEquals(1, states.size)
		assertIs<SubjectDetail.State.Content>(states.single())
		assertEquals(listOf("MAT101"), repository.freshCalls)
		assertEquals(emptyList(), repository.refreshCalls)
	}

	@Test
	fun loadSubjectDetail_showsLoadingWhenRemoteRefreshIsNeeded() = runTest {
		val repository = RecordingSubjectStatsRepository(
			results = listOf(readySubjectDetail(expiresAt = Long.MAX_VALUE))
		)
		val processor = LoadSubjectDetailActionProcessor(
			loadSubjectDetailUseCase = LoadSubjectDetailUseCase(
				subjectStatsRepository = repository,
				reportingRepository = RecordingReportingRepository()
			)
		)

		val states = collectStates(
			initialState = SubjectDetail.State.Idle,
			mutations = processor.process(
				action = SubjectDetail.Action.LoadSubjectDetail(subjectCode = "MAT101"),
				sideEffect = {}
			)
		)

		assertEquals(2, states.size)
		assertEquals(SubjectDetail.State.Loading, states.first())
		assertIs<SubjectDetail.State.Content>(states.last())
		assertEquals(listOf("MAT101"), repository.freshCalls)
		assertEquals(listOf("MAT101"), repository.refreshCalls)
	}

	@Test
	fun loadSubjectDetail_reducesStateToCareerContentWhenCareerSegmentExists() = runTest {
		val repository = RecordingSubjectStatsRepository(
			results = listOf(readySubjectDetail(expiresAt = Long.MAX_VALUE))
		)
		val processor = LoadSubjectDetailActionProcessor(
			loadSubjectDetailUseCase = LoadSubjectDetailUseCase(
				subjectStatsRepository = repository,
				reportingRepository = RecordingReportingRepository()
			)
		)

		val state = reduceState(
			initialState = SubjectDetail.State.Loading,
			mutations = processor.process(
				action = SubjectDetail.Action.LoadSubjectDetail(subjectCode = "MAT101"),
				sideEffect = {}
			)
		)

		val contentState = assertIs<SubjectDetail.State.Content>(state)
		assertEquals("MAT101", contentState.detail.id)
		assertEquals("Sobre MAT101", contentState.topBarTitle)
		assertEquals(SubjectSegmentTab.CAREER, contentState.selectedTab)
		assertEquals(listOf("MAT101"), repository.freshCalls)
		assertEquals(listOf("MAT101"), repository.refreshCalls)
	}

	@Test
	fun loadSubjectDetail_reducesStateToUnavailableWhenRepositoryReturnsNoData() = runTest {
		val repository = RecordingSubjectStatsRepository(
			results = listOf(SubjectDetailResult.Unavailable(subjectCode = "MAT404", expiresAt = 123L))
		)
		val processor = LoadSubjectDetailActionProcessor(
			loadSubjectDetailUseCase = LoadSubjectDetailUseCase(
				subjectStatsRepository = repository,
				reportingRepository = RecordingReportingRepository()
			)
		)

		val state = reduceState(
			initialState = SubjectDetail.State.Loading,
			mutations = processor.process(
				action = SubjectDetail.Action.LoadSubjectDetail(subjectCode = "MAT404"),
				sideEffect = {}
			)
		)

		val unavailableState = assertIs<SubjectDetail.State.Unavailable>(state)
		assertEquals("MAT404", unavailableState.subjectCode)
		assertEquals("Sobre MAT404", unavailableState.topBarTitle)
		assertEquals(listOf("MAT404"), repository.freshCalls)
		assertEquals(listOf("MAT404"), repository.refreshCalls)
	}

	@Test
	fun refreshSubjectDetail_forcesRefreshAfterFailure() = runTest {
		val repository = RecordingSubjectStatsRepository(
			results = listOf(IllegalStateException("boom"), readySubjectDetail(expiresAt = Long.MAX_VALUE))
		)
		val reportingRepository = RecordingReportingRepository()
		val loadProcessor = LoadSubjectDetailActionProcessor(
			loadSubjectDetailUseCase = LoadSubjectDetailUseCase(
				subjectStatsRepository = repository,
				reportingRepository = reportingRepository
			)
		)
		val refreshProcessor = RefreshSubjectDetailActionProcessor(
			refreshSubjectDetailUseCase = RefreshSubjectDetailUseCase(
				subjectStatsRepository = repository,
				reportingRepository = reportingRepository
			)
		)

		val failedState = reduceState(
			initialState = SubjectDetail.State.Loading,
			mutations = loadProcessor.process(
				action = SubjectDetail.Action.LoadSubjectDetail(subjectCode = "MAT101"),
				sideEffect = {}
			)
		)
		assertEquals(
			"Sobre MAT101",
			assertIs<SubjectDetail.State.Failed>(failedState).topBarTitle
		)

		val recoveredState = reduceState(
			initialState = failedState,
			mutations = refreshProcessor.process(
				action = SubjectDetail.Action.RefreshSubjectDetail(subjectCode = "MAT101"),
				sideEffect = {}
			)
		)
		assertIs<SubjectDetail.State.Content>(recoveredState)
		assertEquals(listOf("MAT101"), repository.freshCalls)
		assertEquals(listOf("MAT101", "MAT101"), repository.refreshCalls)
	}
}

private class RecordingSubjectStatsRepository(
	private val freshResult: SubjectDetailResult? = null,
	results: List<Any>
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

private suspend fun collectStates(
	initialState: SubjectDetail.State,
	mutations: Flow<Mutation<SubjectDetail.State>>
): List<SubjectDetail.State> {
	var currentState = initialState
	return mutations.toList().map { mutation ->
		mutation(currentState).also { nextState ->
			currentState = nextState
		}
	}
}

private suspend fun reduceState(
	initialState: SubjectDetail.State,
	mutations: Flow<Mutation<SubjectDetail.State>>
): SubjectDetail.State {
	return collectStates(
		initialState = initialState,
		mutations = mutations
	).last()
}

private fun readySubjectDetail(
	subjectCode: String = "MAT101",
	expiresAt: Long
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
