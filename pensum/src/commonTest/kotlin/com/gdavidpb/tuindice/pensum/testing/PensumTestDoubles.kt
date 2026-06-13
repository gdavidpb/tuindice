package com.gdavidpb.tuindice.pensum.testing

import com.gdavidpb.tuindice.pensum.domain.model.ObservedPensum
import com.gdavidpb.tuindice.pensum.domain.model.PensumGraph
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.model.PensumSelection
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RecordingPensumRepository(
	private val observations: List<PensumObservation> = emptyList(),
	var observeThrowable: Throwable? = null,
	var refreshThrowable: Throwable? = null,
	var selectionThrowable: Throwable? = null
) : PensumRepository {
	var refreshCalls = 0
		private set
	val selectedPensumYears = mutableListOf<Int>()
	val selectedModalityIds = mutableListOf<String>()
	val selectedSelections = mutableListOf<Pair<Int, String>>()

	override fun observePensumFlow(): Flow<PensumObservation> = flow {
		observations.forEach { observation -> emit(observation) }
		observeThrowable?.let { throwable -> throw throwable }
	}

	override suspend fun refreshPensum() {
		refreshCalls++
		refreshThrowable?.let { throwable -> throw throwable }
	}

	override suspend fun selectPensum(year: Int) {
		selectionThrowable?.let { throwable -> throw throwable }
		selectedPensumYears += year
	}

	override suspend fun selectModality(modalityId: String) {
		selectionThrowable?.let { throwable -> throw throwable }
		selectedModalityIds += modalityId
	}

	override suspend fun selectSelection(year: Int, modalityId: String) {
		selectionThrowable?.let { throwable -> throw throwable }
		selectedSelections += year to modalityId
	}
}

fun sampleObservedPensum(): ObservedPensum {
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

fun samplePensumGraph(): PensumGraph {
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
