package com.gdavidpb.tuindice.pensum.testing

import com.gdavidpb.tuindice.pensum.domain.model.ObservedPensum
import com.gdavidpb.tuindice.pensum.domain.model.PensumGraph
import com.gdavidpb.tuindice.pensum.domain.model.PensumSelection

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
