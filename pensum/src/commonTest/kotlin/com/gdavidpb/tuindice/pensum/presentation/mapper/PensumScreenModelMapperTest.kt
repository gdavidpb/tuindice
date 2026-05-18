package com.gdavidpb.tuindice.pensum.presentation.mapper

import com.gdavidpb.tuindice.pensum.domain.model.ObservedPensum
import com.gdavidpb.tuindice.pensum.domain.model.PensumGraph
import com.gdavidpb.tuindice.pensum.domain.model.PensumModality
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeStatus
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeType
import com.gdavidpb.tuindice.pensum.domain.model.PensumOption
import com.gdavidpb.tuindice.pensum.domain.model.PensumSelection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PensumScreenModelMapperTest {
	@Test
	fun when_nodesTouchInSameTerm_then_displayModelAddsVerticalGap() {
		val firstNode = course(
			id = "ma1111",
			displayCode = "MA1111",
			y = 84.0,
			height = 120.0
		)
		val secondNode = course(
			id = "id1111",
			displayCode = "ID1111",
			y = 204.0,
			height = 120.0
		)

		val model = observedPensum(nodes = listOf(firstNode, secondNode)).toScreenModel()
		val nodesById = model.nodes.associateBy { node -> node.id }

		assertEquals("Ingenieria de Computacion", model.careerName)
		assertEquals(84.0, nodesById.getValue("ma1111").y)
		assertEquals(220.0, nodesById.getValue("id1111").y)
		assertTrue(model.canvas.height >= 388.0)
	}
}

private fun observedPensum(nodes: List<PensumGraph.Node>): ObservedPensum {
	val graph = PensumGraph(
		id = "computacion-2019-degree-project",
		year = 2019,
		modalityId = "degree_project",
		modalityName = "Proyecto de Grado",
		totalCredits = 8,
		canvas = PensumGraph.Canvas(width = 520.0, height = 320.0),
		terms = listOf(
			PensumGraph.Term(
				id = "T1",
				label = "Primer trimestre",
				x = 0.0,
				width = 240.0
			)
		),
		nodes = nodes,
		edges = emptyList()
	)
	return ObservedPensum(
		careerName = "Ingenieria de Computacion",
		selection = PensumSelection(
			pensumId = graph.id,
			year = graph.year,
			modalityId = graph.modalityId,
			modalityName = graph.modalityName,
			inferred = false
		),
		availablePensums = listOf(
			PensumOption(
				id = graph.id,
				year = graph.year
			)
		),
		availableModalities = listOf(
			PensumModality(
				id = graph.modalityId,
				name = graph.modalityName,
				isDefault = true
			)
		),
		pensum = graph,
		pensums = listOf(graph),
		approvedCredits = 0,
		nodeStatuses = nodes.associate { node -> node.id to PensumNodeStatus.AVAILABLE }
	)
}

private fun course(
	id: String,
	displayCode: String,
	y: Double,
	height: Double
): PensumGraph.Node {
	return PensumGraph.Node(
		id = id,
		nodeType = PensumNodeType.COURSE,
		displayCode = displayCode,
		subjectCode = displayCode,
		name = displayCode,
		credits = 4,
		category = "BASIC",
		termId = "T1",
		x = 40.0,
		y = y,
		width = 160.0,
		height = height,
		fulfillmentRules = emptyList()
	)
}
