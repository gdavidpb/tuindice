package com.gdavidpb.tuindice.subjects.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.domain.usecase.LoadSubjectDetailUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectDetailUseCase
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.testing.RecordingSubjectStatsRepository
import com.gdavidpb.tuindice.subjects.testing.readySubjectDetail
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.awaitUntilState
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

class SubjectDetailViewModelContractTest {
	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun loadWithFreshLocalData_rendersContentWithoutLoading() = runTest {
		val fixture = createFixture(
			repository = RecordingSubjectStatsRepository(
				freshResult = readySubjectDetail()
			)
		)
		val viewModel = fixture.viewModel

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SubjectDetail.State.Idle, awaitItem())

				viewModel.loadSubjectDetailAction(subjectCode = "MAT101")

				val content = assertIs<SubjectDetail.State.Content>(awaitItem())
				assertEquals("MAT101", content.detail.id)
				assertEquals(SubjectSegmentTab.CAREER, content.detail.selectedTab)

				cancelAndIgnoreRemainingEvents()
			}

			assertEquals(listOf("MAT101"), fixture.repository.freshCalls)
			assertEquals(emptyList(), fixture.repository.refreshCalls)
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun loadWithoutFreshData_showsLoadingThenContent() = runTest {
		val fixture = createFixture(
			repository = RecordingSubjectStatsRepository(
				results = listOf(readySubjectDetail())
			)
		)
		val viewModel = fixture.viewModel

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SubjectDetail.State.Idle, awaitItem())

				viewModel.loadSubjectDetailAction(subjectCode = "MAT101")

				awaitUntilState<SubjectDetail.State.Loading>()
				awaitUntilState<SubjectDetail.State.Content>()

				cancelAndIgnoreRemainingEvents()
			}

			assertEquals(listOf("MAT101"), fixture.repository.refreshCalls)
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun loadWithoutData_rendersUnavailable() = runTest {
		val fixture = createFixture(
			repository = RecordingSubjectStatsRepository(
				results = listOf(
					SubjectDetailResult.Unavailable(subjectCode = "MAT404", expiresAt = 123L)
				)
			)
		)
		val viewModel = fixture.viewModel

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SubjectDetail.State.Idle, awaitItem())

				viewModel.loadSubjectDetailAction(subjectCode = "MAT404")

				val unavailable = awaitUntilState<SubjectDetail.State.Unavailable>()
				assertEquals("MAT404", unavailable.subjectCode)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun loadFailure_rendersFailed_andRefreshRecovers() = runTest {
		val fixture = createFixture(
			repository = RecordingSubjectStatsRepository(
				results = listOf(
					IllegalStateException("boom"),
					readySubjectDetail()
				)
			)
		)
		val viewModel = fixture.viewModel

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SubjectDetail.State.Idle, awaitItem())

				viewModel.loadSubjectDetailAction(subjectCode = "MAT101")
				val failed = awaitUntilState<SubjectDetail.State.Failed>()
				assertEquals("MAT101", failed.subjectCode)

				viewModel.refreshSubjectDetailAction(subjectCode = "MAT101")
				awaitUntilState<SubjectDetail.State.Content>()

				cancelAndIgnoreRemainingEvents()
			}

			assertEquals(listOf("MAT101", "MAT101"), fixture.repository.refreshCalls)
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun selectSegmentTab_onContent_updatesSelectedTab() = runTest {
		val fixture = createFixture(
			repository = RecordingSubjectStatsRepository(
				freshResult = readySubjectDetail()
			)
		)
		val viewModel = fixture.viewModel

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SubjectDetail.State.Idle, awaitItem())

				viewModel.loadSubjectDetailAction(subjectCode = "MAT101")
				awaitUntilState<SubjectDetail.State.Content> { state ->
					state.detail.selectedTab == SubjectSegmentTab.CAREER
				}

				viewModel.selectSubjectSegmentTabAction(tab = SubjectSegmentTab.GLOBAL)
				awaitUntilState<SubjectDetail.State.Content> { state ->
					state.detail.selectedTab == SubjectSegmentTab.GLOBAL
				}

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	private fun createFixture(
		repository: RecordingSubjectStatsRepository
	): SubjectDetailFixture {
		val reportingRepository = RecordingReportingRepository()

		val viewModel = SubjectDetailViewModel(
			loadSubjectDetailUseCase = LoadSubjectDetailUseCase(
				subjectStatsRepository = repository,
				reportingRepository = reportingRepository
			),
			refreshSubjectDetailUseCase = RefreshSubjectDetailUseCase(
				subjectStatsRepository = repository,
				reportingRepository = reportingRepository
			),
			eventPublisher = NoOpEventPublisher
		)

		return SubjectDetailFixture(
			viewModel = viewModel,
			repository = repository
		)
	}
}

private data class SubjectDetailFixture(
	val viewModel: SubjectDetailViewModel,
	val repository: RecordingSubjectStatsRepository
)
