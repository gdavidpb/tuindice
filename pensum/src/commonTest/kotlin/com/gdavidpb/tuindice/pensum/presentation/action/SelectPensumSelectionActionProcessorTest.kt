package com.gdavidpb.tuindice.pensum.presentation.action

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumSelectionUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.exceptionhandler.UpdatePensumExceptionHandler
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.model.PensumCanvasItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenSelection
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import com.gdavidpb.tuindice.testkit.mvi.reduceMutations
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_failed_network_unavailable
import tuindice.pensum.generated.resources.pensum_local_data_warning_network

class SelectPensumSelectionActionProcessorTest {
	@Test
	fun when_selectSelectionStartsWhileContentIsVisible_then_refreshingFlagIsEnabledAndWarningIsCleared() = runTest {
		val processor = createProcessor(pensumRepository = RecordingSelectSelectionRepository())
		val contentState = selectSelectionContentState(
			localDataMessage = UiText.Resource(Res.string.pensum_local_data_warning_network)
		)
		val effects = mutableListOf<Pensum.Effect>()

		val mutations = processor.process(
			action = Pensum.Action.SelectSelection(year = 2017, modalityId = "thesis"),
			sideEffect = effects::add
		).toList()
		val loadingState = mutations.first().invoke(contentState)

		assertEquals(
			contentState.copy(isRefreshing = true, localDataMessage = null),
			loadingState
		)
		assertEquals(emptyList(), effects)
	}

	@Test
	fun when_selectSelectionSucceedsWhileContentIsVisible_then_refreshingFlagIsDisabled() = runTest {
		val processor = createProcessor(pensumRepository = RecordingSelectSelectionRepository())
		val contentState = selectSelectionContentState()
		val effects = mutableListOf<Pensum.Effect>()

		val finalState = processor.process(
			action = Pensum.Action.SelectSelection(year = 2017, modalityId = "thesis"),
			sideEffect = effects::add
		).toList().reduceMutations(contentState)

		assertEquals(
			contentState.copy(isRefreshing = false),
			finalState
		)
		assertEquals(emptyList(), effects)
	}

	@Test
	fun when_selectSelectionReturnsNotFoundWhileContentIsVisible_then_stateBecomesEmpty() = runTest {
		val processor = createProcessor(
			pensumRepository = ThrowingSelectSelectionRepository(
				throwable = clientRequestException(
					statusCode = HttpStatusCode.NotFound,
					path = "/pensums/v4"
				)
			)
		)
		val contentState = selectSelectionContentState()
		val effects = mutableListOf<Pensum.Effect>()

		val finalState = processor.process(
			action = Pensum.Action.SelectSelection(year = 2017, modalityId = "thesis"),
			sideEffect = effects::add
		).toList().reduceMutations(contentState)

		assertEquals(Pensum.State.Empty, finalState)
		assertEquals(emptyList(), effects)
	}

	@Test
	fun when_selectSelectionFailsWhileLoadingWithoutNetwork_then_stateBecomesFailedWithNetworkMessage() = runTest {
		val processor = createProcessor(
			pensumRepository = ThrowingSelectSelectionRepository(throwable = selectionConnectionThrowable()),
			isNetworkAvailable = false
		)
		val effects = mutableListOf<Pensum.Effect>()

		val finalState = processor.process(
			action = Pensum.Action.SelectSelection(year = 2017, modalityId = "thesis"),
			sideEffect = effects::add
		).toList().reduceMutations(Pensum.State.Loading)

		assertEquals(
			Pensum.State.Failed(
				message = UiText.Resource(Res.string.pensum_failed_network_unavailable)
			),
			finalState
		)
		assertEquals(emptyList(), effects)
	}

	@Test
	fun when_selectSelectionFailsWhileContentIsVisibleWithoutNetwork_then_contentRemainsAndNetworkWarningIsShown() = runTest {
		val processor = createProcessor(
			pensumRepository = ThrowingSelectSelectionRepository(throwable = selectionConnectionThrowable()),
			isNetworkAvailable = false
		)
		val contentState = selectSelectionContentState()
		val effects = mutableListOf<Pensum.Effect>()

		val finalState = processor.process(
			action = Pensum.Action.SelectSelection(year = 2017, modalityId = "thesis"),
			sideEffect = effects::add
		).toList().reduceMutations(contentState)

		assertEquals(
			contentState.copy(
				localDataMessage = UiText.Resource(Res.string.pensum_local_data_warning_network)
			),
			finalState
		)
		assertEquals(emptyList(), effects)
	}

	@Test
	fun when_selectSelectionExecutes_then_yearAndModalityIdAreForwardedToRepository() = runTest {
		val pensumRepository = RecordingSelectSelectionRepository()
		val processor = createProcessor(pensumRepository = pensumRepository)

		processor.process(
			action = Pensum.Action.SelectSelection(year = 2017, modalityId = "thesis"),
			sideEffect = {}
		).toList()

		assertEquals(listOf(2017 to "thesis"), pensumRepository.selectedSelections)
	}
}

private fun createProcessor(
	pensumRepository: PensumRepository,
	isNetworkAvailable: Boolean = true
): SelectPensumSelectionActionProcessor {
	return SelectPensumSelectionActionProcessor(
		selectPensumSelectionUseCase = SelectPensumSelectionUseCase(
			pensumRepository = pensumRepository,
			reportingRepository = RecordingReportingRepository(),
			exceptionHandler = UpdatePensumExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = isNetworkAvailable)
			)
		)
	)
}

private class RecordingSelectSelectionRepository : PensumRepository {
	val selectedSelections = mutableListOf<Pair<Int, String>>()

	override fun observePensumFlow(): Flow<PensumObservation> = emptyFlow()

	override suspend fun refreshPensum() = Unit

	override suspend fun selectPensum(year: Int) = Unit

	override suspend fun selectModality(modalityId: String) = Unit

	override suspend fun selectSelection(year: Int, modalityId: String) {
		selectedSelections += year to modalityId
	}
}

private class ThrowingSelectSelectionRepository(
	private val throwable: Throwable
) : PensumRepository {
	override fun observePensumFlow(): Flow<PensumObservation> = emptyFlow()

	override suspend fun refreshPensum() = Unit

	override suspend fun selectPensum(year: Int) = Unit

	override suspend fun selectModality(modalityId: String) = Unit

	override suspend fun selectSelection(year: Int, modalityId: String) {
		throw throwable
	}
}

private fun selectionConnectionThrowable(): Throwable {
	return Throwable("internet connection appears to be offline")
}

private fun selectSelectionContentState(
	localDataMessage: UiText? = null
): Pensum.State.Content {
	return Pensum.State.Content(
		model = PensumScreenModel(
			careerName = "Ingenieria de Computacion",
			selection = PensumScreenSelection(
				year = 2019,
				modalityId = "degree_project"
			),
			pensumOptions = emptyList(),
			modalityOptions = emptyList(),
			progressPercent = 0,
			approvedCredits = 0,
			totalCredits = 0,
			isCurrentFocusVisible = false,
			canvas = PensumCanvasItem(width = 0.0, height = 0.0),
			terms = emptyList(),
			nodes = emptyList(),
			edges = emptyList()
		),
		localDataMessage = localDataMessage
	)
}
