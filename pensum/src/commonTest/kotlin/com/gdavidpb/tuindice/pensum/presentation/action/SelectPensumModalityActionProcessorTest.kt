package com.gdavidpb.tuindice.pensum.presentation.action

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumModalityUseCase
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
import tuindice.pensum.generated.resources.pensum_failed_timeout
import tuindice.pensum.generated.resources.pensum_local_data_warning_timeout

class SelectPensumModalityActionProcessorTest {
	@Test
	fun when_selectModalityStartsWhileContentIsVisible_then_refreshingFlagIsEnabledAndWarningIsCleared() = runTest {
		val processor = createProcessor(pensumRepository = RecordingSelectModalityRepository())
		val contentState = selectModalityContentState(
			localDataMessage = UiText.Resource(Res.string.pensum_local_data_warning_timeout)
		)
		val effects = mutableListOf<Pensum.Effect>()

		val mutations = processor.process(
			action = Pensum.Action.SelectModality(modalityId = "thesis"),
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
	fun when_selectModalitySucceedsWhileContentIsVisible_then_refreshingFlagIsDisabled() = runTest {
		val processor = createProcessor(pensumRepository = RecordingSelectModalityRepository())
		val contentState = selectModalityContentState()
		val effects = mutableListOf<Pensum.Effect>()

		val finalState = processor.process(
			action = Pensum.Action.SelectModality(modalityId = "thesis"),
			sideEffect = effects::add
		).toList().reduceMutations(contentState)

		assertEquals(
			contentState.copy(isRefreshing = false),
			finalState
		)
		assertEquals(emptyList(), effects)
	}

	@Test
	fun when_selectModalityReturnsNotFound_then_stateBecomesEmptyWithoutSnackbar() = runTest {
		val processor = createProcessor(
			pensumRepository = ThrowingSelectModalityRepository(
				throwable = clientRequestException(
					statusCode = HttpStatusCode.NotFound,
					path = "/pensums/v4"
				)
			)
		)
		val effects = mutableListOf<Pensum.Effect>()

		val finalState = processor.process(
			action = Pensum.Action.SelectModality(modalityId = "thesis"),
			sideEffect = effects::add
		).toList().reduceMutations(Pensum.State.Loading)

		assertEquals(Pensum.State.Empty, finalState)
		assertEquals(emptyList(), effects)
	}

	@Test
	fun when_selectModalityTimesOutWhileLoading_then_stateBecomesFailed() = runTest {
		val processor = createProcessor(
			pensumRepository = ThrowingSelectModalityRepository(
				throwable = Throwable("timeout")
			)
		)
		val effects = mutableListOf<Pensum.Effect>()

		val finalState = processor.process(
			action = Pensum.Action.SelectModality(modalityId = "thesis"),
			sideEffect = effects::add
		).toList().reduceMutations(Pensum.State.Loading)

		assertEquals(
			Pensum.State.Failed(
				message = UiText.Resource(Res.string.pensum_failed_timeout)
			),
			finalState
		)
		assertEquals(emptyList(), effects)
	}

	@Test
	fun when_selectModalityTimesOutWhileContentIsVisible_then_contentRemainsAndTimeoutWarningIsShown() = runTest {
		val processor = createProcessor(
			pensumRepository = ThrowingSelectModalityRepository(
				throwable = Throwable("timeout")
			)
		)
		val contentState = selectModalityContentState()
		val effects = mutableListOf<Pensum.Effect>()

		val finalState = processor.process(
			action = Pensum.Action.SelectModality(modalityId = "thesis"),
			sideEffect = effects::add
		).toList().reduceMutations(contentState)

		assertEquals(
			contentState.copy(
				localDataMessage = UiText.Resource(Res.string.pensum_local_data_warning_timeout)
			),
			finalState
		)
		assertEquals(emptyList(), effects)
	}

	@Test
	fun when_selectModalityExecutes_then_modalityIdIsForwardedToRepository() = runTest {
		val pensumRepository = RecordingSelectModalityRepository()
		val processor = createProcessor(pensumRepository = pensumRepository)

		processor.process(
			action = Pensum.Action.SelectModality(modalityId = "thesis"),
			sideEffect = {}
		).toList()

		assertEquals(listOf("thesis"), pensumRepository.selectedModalityIds)
	}
}

private fun createProcessor(
	pensumRepository: PensumRepository,
	isNetworkAvailable: Boolean = true
): SelectPensumModalityActionProcessor {
	return SelectPensumModalityActionProcessor(
		selectPensumModalityUseCase = SelectPensumModalityUseCase(
			pensumRepository = pensumRepository,
			reportingRepository = RecordingReportingRepository(),
			exceptionHandler = UpdatePensumExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = isNetworkAvailable)
			)
		)
	)
}

private class RecordingSelectModalityRepository : PensumRepository {
	val selectedModalityIds = mutableListOf<String>()

	override fun observePensumFlow(): Flow<PensumObservation> = emptyFlow()

	override suspend fun refreshPensum() = Unit

	override suspend fun selectPensum(year: Int) = Unit

	override suspend fun selectModality(modalityId: String) {
		selectedModalityIds += modalityId
	}

	override suspend fun selectSelection(year: Int, modalityId: String) = Unit
}

private class ThrowingSelectModalityRepository(
	private val throwable: Throwable
) : PensumRepository {
	override fun observePensumFlow(): Flow<PensumObservation> = emptyFlow()

	override suspend fun refreshPensum() = Unit

	override suspend fun selectPensum(year: Int) = Unit

	override suspend fun selectModality(modalityId: String) {
		throw throwable
	}

	override suspend fun selectSelection(year: Int, modalityId: String) = Unit
}

private fun selectModalityContentState(
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
