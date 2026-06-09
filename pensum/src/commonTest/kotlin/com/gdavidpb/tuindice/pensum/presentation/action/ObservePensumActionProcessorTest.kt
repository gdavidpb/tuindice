package com.gdavidpb.tuindice.pensum.presentation.action

import com.gdavidpb.tuindice.pensum.domain.model.ObservedPensum
import com.gdavidpb.tuindice.pensum.domain.model.PensumGraph
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.model.PensumSelection
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.pensum.domain.usecase.ObservePensumUseCase
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.model.PensumCanvasItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenSelection
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.reduceMutations
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest

class ObservePensumActionProcessorTest {
	@Test
	fun when_recordDataIsNotReady_then_stateStaysLoading() = runTest {
		val processor = createProcessor(PensumObservation.WaitingForRecordData)

		val finalState = processor.process(
			action = Pensum.Action.ObservePensum,
			sideEffect = {}
		).toList().reduceMutations(Pensum.State.Loading)

		assertEquals(Pensum.State.Loading, finalState)
	}

	@Test
	fun when_recordDataFailed_then_stateBecomesRecordDataUnavailable() = runTest {
		val processor = createProcessor(PensumObservation.RecordDataUnavailable)

		val finalState = processor.process(
			action = Pensum.Action.ObservePensum,
			sideEffect = {}
		).toList().reduceMutations(Pensum.State.Loading)

		assertEquals(Pensum.State.RecordDataUnavailable, finalState)
	}

	@Test
	fun when_contentObservationArrivesDuringRefresh_then_refreshingFlagIsPreserved() = runTest {
		val processor = createProcessor(PensumObservation.Content(sampleObservedPensum()))
		val currentState = Pensum.State.Content(
			model = sampleScreenModel(),
			isRefreshing = true
		)

		val finalState = processor.process(
			action = Pensum.Action.ObservePensum,
			sideEffect = {}
		).toList().reduceMutations(currentState)

		assertEquals(true, (finalState as Pensum.State.Content).isRefreshing)
	}

	private fun createProcessor(observation: PensumObservation): ObservePensumActionProcessor {
		return ObservePensumActionProcessor(
			observePensumUseCase = ObservePensumUseCase(
				pensumRepository = FakePensumRepository(observation),
				reportingRepository = RecordingReportingRepository()
			)
		)
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

private fun sampleScreenModel(): PensumScreenModel {
	return PensumScreenModel(
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
	)
}

private class FakePensumRepository(
	private val observation: PensumObservation
) : PensumRepository {
	override fun observePensumFlow(): Flow<PensumObservation> = flowOf(observation)

	override suspend fun refreshPensum() = Unit

	override suspend fun selectPensum(year: Int) = Unit

	override suspend fun selectModality(modalityId: String) = Unit

	override suspend fun selectSelection(year: Int, modalityId: String) = Unit
}
