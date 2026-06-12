package com.gdavidpb.tuindice.pensum.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.pensum.domain.model.ObservedPensum
import com.gdavidpb.tuindice.pensum.domain.model.PensumGraph
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.model.PensumSelection
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.pensum.domain.usecase.ObservePensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumModalityUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumSelectionUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.UpdatePensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.exceptionhandler.UpdatePensumExceptionHandler
import com.gdavidpb.tuindice.pensum.presentation.action.ObservePensumActionProcessor
import com.gdavidpb.tuindice.pensum.presentation.action.RefreshPensumActionProcessor
import com.gdavidpb.tuindice.pensum.presentation.action.SelectPensumActionProcessor
import com.gdavidpb.tuindice.pensum.presentation.action.SelectPensumModalityActionProcessor
import com.gdavidpb.tuindice.pensum.presentation.action.SelectPensumSelectionActionProcessor
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import com.gdavidpb.tuindice.testkit.ktor.serverResponseException
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_local_data_warning_service

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

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun contentThatDisappears_fallsBackToLoading() = runTest {
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

				fixture.repository.emit(PensumObservation.Missing)
				awaitUntilState<Pensum.State.Loading> { true }

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

	private suspend inline fun <reified T : Pensum.State> app.cash.turbine.TurbineTestContext<Pensum.State>.awaitUntilState(
		predicate: (T) -> Boolean
	): T {
		while (true) {
			val state = awaitItem()
			if (state is T && predicate(state)) return state
		}
	}

	private fun createFixture(isNetworkAvailable: Boolean = true): PensumFixture {
		val repository = ControllablePensumRepository()
		val exceptionHandler = UpdatePensumExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = isNetworkAvailable)
		)

		val viewModel = PensumViewModel(
			observePensumActionProcessor = ObservePensumActionProcessor(
				observePensumUseCase = ObservePensumUseCase(
					pensumRepository = repository,
					reportingRepository = RecordingReportingRepository()
				)
			),
			refreshPensumActionProcessor = RefreshPensumActionProcessor(
				updatePensumUseCase = UpdatePensumUseCase(
					pensumRepository = repository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = exceptionHandler
				)
			),
			selectPensumActionProcessor = SelectPensumActionProcessor(
				selectPensumUseCase = SelectPensumUseCase(
					pensumRepository = repository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = exceptionHandler
				)
			),
			selectPensumModalityActionProcessor = SelectPensumModalityActionProcessor(
				selectPensumModalityUseCase = SelectPensumModalityUseCase(
					pensumRepository = repository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = exceptionHandler
				)
			),
			selectPensumSelectionActionProcessor = SelectPensumSelectionActionProcessor(
				selectPensumSelectionUseCase = SelectPensumSelectionUseCase(
					pensumRepository = repository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = exceptionHandler
				)
			),
			eventPublisher = NoOpEventPublisher
		)

		return PensumFixture(
			viewModel = viewModel,
			repository = repository
		)
	}
}

private data class PensumFixture(
	val viewModel: PensumViewModel,
	val repository: ControllablePensumRepository
)

private class ControllablePensumRepository : PensumRepository {
	private val observations = Channel<Result<PensumObservation>>(Channel.UNLIMITED)
	private val refreshGate = Channel<Unit>(Channel.UNLIMITED)
	private val selectPensumCalls = Channel<Int>(Channel.UNLIMITED)
	private val selectModalityCalls = Channel<String>(Channel.UNLIMITED)
	private val selectSelectionCalls = Channel<Pair<Int, String>>(Channel.UNLIMITED)

	var blockRefresh = false
	var refreshThrowable: Throwable? = null

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
		if (blockRefresh) refreshGate.receive()
		refreshThrowable?.let { throw it }
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

private fun sampleObservedPensum(): ObservedPensum {
	return ObservedPensum(
		careerName = "Ingenieria de Computacion",
		selection = PensumSelection(
			pensumId = "0800-2019-degree_project",
			year = 2019,
			modalityId = "degree_project",
			modalityName = "Proyecto de grado",
			inferred = false
		),
		availablePensums = emptyList(),
		availableModalities = emptyList(),
		pensum = samplePensumGraph(),
		pensums = emptyList(),
		approvedCredits = 0,
		nodeStatuses = emptyMap(),
		nodeFulfillments = emptyMap()
	)
}

private fun samplePensumGraph(): PensumGraph {
	return PensumGraph(
		id = "0800-2019-degree_project",
		year = 2019,
		modalityId = "degree_project",
		modalityName = "Proyecto de grado",
		totalCredits = 0,
		canvas = PensumGraph.Canvas(
			width = 0.0,
			height = 0.0
		),
		terms = emptyList(),
		nodes = emptyList(),
		edges = emptyList()
	)
}
