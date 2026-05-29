package com.gdavidpb.tuindice.pensum.presentation.mapper

import com.gdavidpb.tuindice.pensum.domain.model.ObservedPensum
import com.gdavidpb.tuindice.pensum.domain.model.PensumGraph
import com.gdavidpb.tuindice.pensum.domain.model.PensumModality
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeStatus
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeType
import com.gdavidpb.tuindice.pensum.domain.model.PensumOption
import com.gdavidpb.tuindice.pensum.domain.model.PensumProgress
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

	@Test
	fun when_selectedModalityIsNotFirst_then_displayModelKeepsBackendModalityOrder() {
		val degreeProject = graph(
			id = "computacion-2019-degree-project",
			modalityId = "degree_project",
			modalityName = "Proyecto de Grado"
		)

		val model = observedPensum(
			pensum = degreeProject,
			availableModalities = listOf(
				PensumModality(
					id = "degree_project",
					name = "Proyecto de Grado",
					isDefault = true
				),
				PensumModality(
					id = "long_internship",
					name = "Pasantia Larga",
					isDefault = false
				),
				PensumModality(
					id = "exclusive_degree_project",
					name = "Proyecto de Grado Exclusivo",
					isDefault = false
				)
			),
			nodes = emptyList()
		).toScreenModel()

		assertEquals(
			listOf("degree_project", "long_internship", "exclusive_degree_project"),
			model.modalityOptions.map { modality -> modality.id }
		)
		assertEquals(
			listOf(true, false, false),
			model.modalityOptions.map { modality -> modality.isDefault }
		)
	}

	@Test
	fun when_readModelProvidesIndex_then_selectorUsesIndexInsteadOfSelectedGraphList() {
		val selectedGraph = graph(
			id = "computacion-2019-degree-project",
			modalityId = "degree_project",
			modalityName = "Proyecto de Grado"
		)

		val model = observedPensum(
			pensum = selectedGraph,
			pensums = listOf(selectedGraph),
			availablePensums = listOf(
				PensumOption(
					id = "2016",
					year = 2016
				),
				PensumOption(
					id = "2017",
					year = 2017
				),
				PensumOption(
					id = "2018",
					year = 2018
				),
				PensumOption(
					id = "2019",
					year = 2019
				)
			),
			availableModalities = listOf(
				PensumModality(
					id = "degree_project",
					name = "Proyecto de Grado",
					isDefault = true
				),
				PensumModality(
					id = "long_internship",
					name = "Pasantia Larga",
					isDefault = false
				),
				PensumModality(
					id = "exclusive_degree_project",
					name = "Proyecto de Grado Exclusivo",
					isDefault = false
				)
			),
			nodes = emptyList()
		).toScreenModel()

		assertEquals(
			listOf(2016, 2017, 2018, 2019),
			model.pensumOptions.map { option -> option.year }
		)
		model.pensumOptions.forEach { option ->
			assertEquals(
				listOf("degree_project", "long_internship", "exclusive_degree_project"),
				option.modalityOptions.map { modality -> modality.id }
			)
		}
	}

	@Test
	fun when_slotHasFulfillment_then_displayModelExposesFilledSubjectForCardAndStats() {
		val slot = PensumGraph.Node(
			id = "eg-slot",
			nodeType = PensumNodeType.SLOT,
			displayCode = "EG",
			subjectCode = null,
			name = "Estudios Generales",
			credits = 3,
			category = "GENERAL_STUDIES",
			termId = "T1",
			x = 40.0,
			y = 84.0,
			width = 160.0,
			height = 120.0,
			fulfillmentRules = emptyList()
		)

		val model = observedPensum(
			nodes = listOf(slot),
			nodeFulfillments = mapOf(
				"eg-slot" to PensumProgress.NodeFulfillment(
					subjectCode = "EG1111",
					subjectName = "Sociedad y Cultura"
				)
			)
		).toScreenModel()
		val node = model.nodes.single()

		assertEquals("EG", node.displayCode)
		assertEquals("EG1111", node.fulfilledSubject?.code)
		assertEquals("Sociedad y Cultura", node.fulfilledSubject?.name)
		assertEquals("EG1111", node.subjectStatsCode)
		assertEquals(true, node.hasSubjectStatsAction)
	}
}

private fun observedPensum(
	nodes: List<PensumGraph.Node>,
	pensum: PensumGraph = graph(nodes = nodes),
	pensums: List<PensumGraph> = listOf(pensum),
	availablePensums: List<PensumOption> = listOf(
		PensumOption(
			id = pensum.year.toString(),
			year = pensum.year
		)
	),
	availableModalities: List<PensumModality> = listOf(
		PensumModality(
			id = pensum.modalityId,
			name = pensum.modalityName,
			isDefault = true
		)
	),
	nodeFulfillments: Map<String, PensumProgress.NodeFulfillment> = emptyMap()
): ObservedPensum {
	return ObservedPensum(
		careerName = "Ingenieria de Computacion",
		selection = PensumSelection(
			pensumId = pensum.id,
			year = pensum.year,
			modalityId = pensum.modalityId,
			modalityName = pensum.modalityName,
			inferred = false
		),
		availablePensums = availablePensums,
		availableModalities = availableModalities,
		pensum = pensum,
		pensums = pensums,
		approvedCredits = 0,
		nodeStatuses = nodes.associate { node -> node.id to PensumNodeStatus.AVAILABLE },
		nodeFulfillments = nodeFulfillments
	)
}

private fun graph(
	id: String = "computacion-2019-degree-project",
	modalityId: String = "degree_project",
	modalityName: String = "Proyecto de Grado",
	nodes: List<PensumGraph.Node> = emptyList()
): PensumGraph {
	return PensumGraph(
		id = id,
		year = 2019,
		modalityId = modalityId,
		modalityName = modalityName,
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
