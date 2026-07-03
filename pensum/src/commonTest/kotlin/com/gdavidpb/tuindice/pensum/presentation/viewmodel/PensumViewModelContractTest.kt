package com.gdavidpb.tuindice.pensum.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.pensum.domain.repository.PensumSettingsRepository
import com.gdavidpb.tuindice.pensum.domain.usecase.EnsurePensumLoadedUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.ObservePensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.ObservePensumSummaryCollapsedUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumModalityUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumSelectionUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SetPensumSummaryCollapsedUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.UpdatePensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.exceptionhandler.UpdatePensumExceptionHandler
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.machine.PensumMachine
import com.gdavidpb.tuindice.pensum.testing.sampleObservedPensum
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import com.gdavidpb.tuindice.testkit.ktor.serverResponseException
import com.gdavidpb.tuindice.testkit.mvi.awaitUntilState
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_local_data_warning_service
import kotlin.test.Test
import kotlin.test.assertEquals

class PensumViewModelContractTest {
	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun observedContent_rendersContent() = runTest {
		val fixture = createFixture()
		val viewModel = fixture.viewModel

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(Pensum.State.Idle, awaitItem())

				fixture.repository.emit(PensumObservation.Content(sampleObservedPensum()))

				val content = awaitUntilState<Pensum.State.Content> { true }
				assertEquals("Ingenieria de Computacion", content.model.careerName)
				assertEquals(false, content.isRefreshing)
				assertEquals(null, content.localDataMessage)
				assertEquals(false, content.isSummaryCollapsed)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun observedContent_usesPersistedSummaryCollapsedState() = runTest {
		val fixture = createFixture()
		val viewModel = fixture.viewModel
		fixture.settingsRepository.setSummaryCollapsed(true)

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(Pensum.State.Idle, awaitItem())

				fixture.repository.emit(PensumObservation.Content(sampleObservedPensum()))
				val content = awaitUntilState<Pensum.State.Content> { true }
				assertEquals(true, content.isSummaryCollapsed)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun toggleSummaryCollapsed_updatesContentAndPersistsPreference() = runTest {
		val fixture = createFixture()
		val viewModel = fixture.viewModel

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(Pensum.State.Idle, awaitItem())

				fixture.repository.emit(PensumObservation.Content(sampleObservedPensum()))
				awaitUntilState<Pensum.State.Content> { true }

				viewModel.toggleSummaryCollapsedAction()
				val collapsed = awaitUntilState<Pensum.State.Content> { state ->
					state.isSummaryCollapsed
				}
				assertEquals(true, collapsed.isSummaryCollapsed)
				assertEquals(true, fixture.settingsRepository.awaitSummaryCollapsedCall())

				viewModel.toggleSummaryCollapsedAction()
				val expanded = awaitUntilState<Pensum.State.Content> { state ->
					!state.isSummaryCollapsed
				}
				assertEquals(false, expanded.isSummaryCollapsed)
				assertEquals(false, fixture.settingsRepository.awaitSummaryCollapsedCall())

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun contentThatDisappears_keepsLastContent() = runTest {
		val fixture = createFixture()
		val viewModel = fixture.viewModel

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(Pensum.State.Idle, awaitItem())

				fixture.repository.emit(PensumObservation.Content(sampleObservedPensum()))
				val content = awaitUntilState<Pensum.State.Content> { true }

				fixture.repository.emit(PensumObservation.Missing)
				advanceUntilIdle()
				assertEquals(content, viewModel.state.value)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun initialRefreshLoading_movesIdleToLoadingUntilResultArrives() = runTest {
		val fixture = createFixture()
		val viewModel = fixture.viewModel
		fixture.repository.blockRefresh = true

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(Pensum.State.Idle, awaitItem())

				viewModel.refreshPensumAction()
				awaitUntilState<Pensum.State.Loading> { true }

				fixture.repository.releaseRefresh()
				advanceUntilIdle()
				assertEquals(Pensum.State.Loading, viewModel.state.value)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun ensureLoaded_whenContentAlreadyExists_doesNotShowRefreshingOrHitRepository() = runTest {
		val fixture = createFixture()
		val viewModel = fixture.viewModel

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(Pensum.State.Idle, awaitItem())

				fixture.repository.emit(PensumObservation.Content(sampleObservedPensum()))
				awaitUntilState<Pensum.State.Content> { true }

				viewModel.ensurePensumLoadedAction()
				advanceUntilIdle()

				val content = viewModel.state.value as Pensum.State.Content
				assertEquals(false, content.isRefreshing)
				assertEquals(0, fixture.repository.hasSelectedPensumResponseCalls)
				assertEquals(0, fixture.repository.refreshCalls)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun ensureLoaded_whenCacheExistsBeforeObservation_doesNotRefreshRemote() = runTest {
		val fixture = createFixture()
		val viewModel = fixture.viewModel
		fixture.repository.hasCachedPensum = true

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(Pensum.State.Idle, awaitItem())

				viewModel.ensurePensumLoadedAction()
				advanceUntilIdle()

				assertEquals(Pensum.State.Idle, viewModel.state.value)
				assertEquals(1, fixture.repository.hasSelectedPensumResponseCalls)
				assertEquals(0, fixture.repository.refreshCalls)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun ensureLoaded_whenCacheIsMissing_showsLoadingWhileRefreshing() = runTest {
		val fixture = createFixture()
		val viewModel = fixture.viewModel
		fixture.repository.blockRefresh = true

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(Pensum.State.Idle, awaitItem())

				viewModel.ensurePensumLoadedAction()
				awaitUntilState<Pensum.State.Loading> { true }

				assertEquals(1, fixture.repository.hasSelectedPensumResponseCalls)
				assertEquals(1, fixture.repository.refreshCalls)

				fixture.repository.releaseRefresh()
				advanceUntilIdle()

				assertEquals(Pensum.State.Loading, viewModel.state.value)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun recordDataUnavailable_thenContentRecovers() = runTest {
		val fixture = createFixture()
		val viewModel = fixture.viewModel

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(Pensum.State.Idle, awaitItem())

				fixture.repository.emit(PensumObservation.RecordDataUnavailable)
				awaitUntilState<Pensum.State.RecordDataUnavailable> { true }

				fixture.repository.emit(PensumObservation.Content(sampleObservedPensum()))
				awaitUntilState<Pensum.State.Content> { true }

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun refreshOverContent_togglesRefreshingFlag() = runTest {
		val fixture = createFixture()
		val viewModel = fixture.viewModel

		fixture.repository.blockRefresh = true

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(Pensum.State.Idle, awaitItem())

				fixture.repository.emit(PensumObservation.Content(sampleObservedPensum()))
				awaitUntilState<Pensum.State.Content> { true }

				viewModel.refreshPensumAction()
				awaitUntilState<Pensum.State.Content> { state -> state.isRefreshing }

				fixture.repository.releaseRefresh()
				awaitUntilState<Pensum.State.Content> { state -> !state.isRefreshing }

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun refreshNotFound_movesToEmpty() = runTest {
		val fixture = createFixture()
		val viewModel = fixture.viewModel

		fixture.repository.refreshThrowable = clientRequestException(
			statusCode = HttpStatusCode.NotFound,
			path = "/pensums/v4"
		)

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(Pensum.State.Idle, awaitItem())

				fixture.repository.emit(PensumObservation.Content(sampleObservedPensum()))
				awaitUntilState<Pensum.State.Content> { true }

				viewModel.refreshPensumAction()
				awaitUntilState<Pensum.State.Empty> { true }

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun refreshFailureOverContent_keepsContentWithLocalDataWarning() = runTest {
		val fixture = createFixture()
		val viewModel = fixture.viewModel

		fixture.repository.refreshThrowable = serverResponseException(
			statusCode = HttpStatusCode.ServiceUnavailable,
			path = "/pensums/v4"
		)

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(Pensum.State.Idle, awaitItem())

				fixture.repository.emit(PensumObservation.Content(sampleObservedPensum()))
				awaitUntilState<Pensum.State.Content> { true }

				viewModel.refreshPensumAction()

				val warned = awaitUntilState<Pensum.State.Content> { state ->
					state.localDataMessage != null
				}
				assertEquals(
					UiText.Resource(Res.string.pensum_local_data_warning_service),
					warned.localDataMessage
				)
				assertEquals(false, warned.isRefreshing)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun observationFailure_movesToFailed() = runTest {
		val fixture = createFixture()
		val viewModel = fixture.viewModel

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(Pensum.State.Idle, awaitItem())

				fixture.repository.failObservation(Throwable("boom"))
				awaitUntilState<Pensum.State.Failed> { true }

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun selectActions_delegateToRepository() = runTest {
		val fixture = createFixture()
		val viewModel = fixture.viewModel

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(Pensum.State.Idle, awaitItem())

				fixture.repository.emit(PensumObservation.Content(sampleObservedPensum()))
				awaitUntilState<Pensum.State.Content> { true }

				viewModel.selectPensumAction(year = 2023)
				assertEquals(2023, fixture.repository.awaitSelectPensumCall())

				viewModel.selectModalityAction(modalityId = "thesis")
				assertEquals("thesis", fixture.repository.awaitSelectModalityCall())

				viewModel.selectSelectionAction(year = 2019, modalityId = "degree_project")
				assertEquals(
					2019 to "degree_project",
					fixture.repository.awaitSelectSelectionCall()
				)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	private fun createFixture(
		isNetworkAvailable: Boolean = true,
		dispatchers: TuIndiceDispatchers = TestTuIndiceDispatchers(Dispatchers.Unconfined)
	): PensumFixture {
		val repository = ControllablePensumRepository()
		val settingsRepository = ControllablePensumSettingsRepository()
		val reportingRepository = RecordingReportingRepository()
		val exceptionHandler = UpdatePensumExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = isNetworkAvailable)
		)

		val viewModel = PensumViewModel(
			screenMachine = PensumMachine(
				observePensumUseCase = ObservePensumUseCase(
					pensumRepository = repository,
					reportingRepository = reportingRepository
				),
				observePensumSummaryCollapsedUseCase = ObservePensumSummaryCollapsedUseCase(
					pensumSettingsRepository = settingsRepository,
					reportingRepository = reportingRepository
				),
				ensurePensumLoadedUseCase = EnsurePensumLoadedUseCase(
					pensumRepository = repository,
					reportingRepository = reportingRepository,
					exceptionHandler = exceptionHandler
				),
				updatePensumUseCase = UpdatePensumUseCase(
					pensumRepository = repository,
					reportingRepository = reportingRepository,
					exceptionHandler = exceptionHandler
				),
				selectPensumUseCase = SelectPensumUseCase(
					pensumRepository = repository,
					reportingRepository = reportingRepository,
					exceptionHandler = exceptionHandler
				),
				selectPensumModalityUseCase = SelectPensumModalityUseCase(
					pensumRepository = repository,
					reportingRepository = reportingRepository,
					exceptionHandler = exceptionHandler
				),
				selectPensumSelectionUseCase = SelectPensumSelectionUseCase(
					pensumRepository = repository,
					reportingRepository = reportingRepository,
					exceptionHandler = exceptionHandler
				),
				setPensumSummaryCollapsedUseCase = SetPensumSummaryCollapsedUseCase(
					pensumSettingsRepository = settingsRepository,
					reportingRepository = reportingRepository
				),
				pensumSettingsRepository = settingsRepository
			),
			eventPublisher = NoOpEventPublisher,
			dispatchers = dispatchers
		)

		return PensumFixture(
			viewModel = viewModel,
			repository = repository,
			settingsRepository = settingsRepository
		)
	}
}

private data class PensumFixture(
	val viewModel: PensumViewModel,
	val repository: ControllablePensumRepository,
	val settingsRepository: ControllablePensumSettingsRepository
)

private class ControllablePensumSettingsRepository : PensumSettingsRepository {
	private val summaryCollapsed = MutableStateFlow(false)
	private val summaryCollapsedCalls = Channel<Boolean>(Channel.UNLIMITED)

	override fun observeSummaryCollapsed(): Flow<Boolean> = summaryCollapsed

	override fun isSummaryCollapsed(): Boolean = summaryCollapsed.value

	override fun setSummaryCollapsed(isCollapsed: Boolean) {
		summaryCollapsed.value = isCollapsed
		summaryCollapsedCalls.trySend(isCollapsed)
	}

	suspend fun awaitSummaryCollapsedCall(): Boolean = summaryCollapsedCalls.receive()
}

private class ControllablePensumRepository : PensumRepository {
	private val observations = Channel<Result<PensumObservation>>(Channel.UNLIMITED)
	private val refreshGate = Channel<Unit>(Channel.UNLIMITED)
	private val selectPensumCalls = Channel<Int>(Channel.UNLIMITED)
	private val selectModalityCalls = Channel<String>(Channel.UNLIMITED)
	private val selectSelectionCalls = Channel<Pair<Int, String>>(Channel.UNLIMITED)

	var hasCachedPensum = false
	var blockRefresh = false
	var refreshThrowable: Throwable? = null
	var hasSelectedPensumResponseCalls = 0
		private set
	var refreshCalls = 0
		private set

	fun emit(observation: PensumObservation) {
		observations.trySend(Result.success(observation))
	}

	fun failObservation(throwable: Throwable) {
		observations.trySend(Result.failure(throwable))
	}

	fun releaseRefresh() {
		refreshGate.trySend(Unit)
	}

	suspend fun awaitSelectPensumCall(): Int = selectPensumCalls.receive()

	suspend fun awaitSelectModalityCall(): String = selectModalityCalls.receive()

	suspend fun awaitSelectSelectionCall(): Pair<Int, String> = selectSelectionCalls.receive()

	override fun observePensumFlow(): Flow<PensumObservation> = flow {
		for (result in observations) {
			emit(result.getOrThrow())
		}
	}

	override suspend fun refreshPensum() {
		refreshCalls++
		if (blockRefresh) refreshGate.receive()
		refreshThrowable?.let { throw it }
	}

	override suspend fun hasSelectedPensumResponse(): Boolean {
		hasSelectedPensumResponseCalls++
		return hasCachedPensum
	}

	override suspend fun selectPensum(year: Int) {
		selectPensumCalls.trySend(year)
	}

	override suspend fun selectModality(modalityId: String) {
		selectModalityCalls.trySend(modalityId)
	}

	override suspend fun selectSelection(year: Int, modalityId: String) {
		selectSelectionCalls.trySend(year to modalityId)
	}
}
