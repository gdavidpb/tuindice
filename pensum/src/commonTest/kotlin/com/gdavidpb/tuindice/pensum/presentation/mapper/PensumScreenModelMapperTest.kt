package com.gdavidpb.tuindice.pensum.presentation.mapper

import com.gdavidpb.tuindice.pensum.domain.model.ObservedPensum
import com.gdavidpb.tuindice.pensum.domain.model.PensumGraph
import com.gdavidpb.tuindice.pensum.domain.model.PensumModality
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeStatus
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeType
import com.gdavidpb.tuindice.pensum.domain.model.PensumOption
import com.gdavidpb.tuindice.pensum.domain.model.PensumProgress
import com.gdavidpb.tuindice.pensum.domain.model.PensumRelationshipType
import com.gdavidpb.tuindice.pensum.domain.model.PensumSelection
import com.gdavidpb.tuindice.pensum.presentation.model.PensumEdgeRelationshipType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

	@Test
	fun when_nodeIsBlocked_then_displayModelKeepsApprovedBackgroundAndDisconnectsIncomingEdges() {
		val approvedNode = course(
			id = "ma1111",
			displayCode = "MA1111",
			y = 84.0,
			height = 120.0
		)
		val blockedNode = course(
			id = "ci5406",
			displayCode = "CI5406",
			y = 252.0,
			height = 120.0
		)
		val edge = edge(fromNodeId = approvedNode.id, toNodeId = blockedNode.id)
		val pensum = graph(
			nodes = listOf(approvedNode, blockedNode),
			edges = listOf(edge)
		)

		val model = observedPensum(
			nodes = listOf(approvedNode, blockedNode),
			pensum = pensum,
			nodeStatuses = mapOf(
				approvedNode.id to PensumNodeStatus.APPROVED,
				blockedNode.id to PensumNodeStatus.BLOCKED
			)
		).toScreenModel()
		val nodesById = model.nodes.associateBy { node -> node.id }

		assertEquals(
			nodesById.getValue(approvedNode.id).visualStyle.containerArgb,
			nodesById.getValue(blockedNode.id).visualStyle.containerArgb
		)
		assertEquals(false, nodesById.getValue(approvedNode.id).isBlocked)
		assertEquals(true, nodesById.getValue(blockedNode.id).isBlocked)
		assertEquals(true, model.edges.single().isDisconnected)
	}

	@Test
	fun when_currentNodeExists_then_displayModelShowsCurrentFocus() {
		val currentNode = course(
			id = "ma2112",
			displayCode = "MA2112",
			y = 84.0,
			height = 120.0
		)

		val model = observedPensum(
			nodes = listOf(currentNode),
			nodeStatuses = mapOf(currentNode.id to PensumNodeStatus.CURRENT)
		).toScreenModel()

		assertTrue(model.isCurrentFocusVisible)
	}

	@Test
	fun when_noCurrentNodeExists_then_displayModelHidesCurrentFocus() {
		val availableNode = course(
			id = "ma2112",
			displayCode = "MA2112",
			y = 84.0,
			height = 120.0
		)

		val model = observedPensum(
			nodes = listOf(availableNode),
			nodeStatuses = mapOf(availableNode.id to PensumNodeStatus.AVAILABLE)
		).toScreenModel()

		assertFalse(model.isCurrentFocusVisible)
	}

	@Test
	fun when_edgesExist_then_subjectDetailExposesRelationsAndBlockingReasons() {
		val approvedRequirement = course(
			id = "ma1111",
			displayCode = "MA1111",
			y = 84.0,
			height = 120.0
		)
		val pendingCorequisite = course(
			id = "ci2693",
			displayCode = "CI2693",
			y = 228.0,
			height = 120.0
		)
		val blockedNode = course(
			id = "ci4325",
			displayCode = "CI4325",
			y = 372.0,
			height = 120.0
		)
		val unlockedNode = course(
			id = "ci5406",
			displayCode = "CI5406",
			y = 516.0,
			height = 120.0
		)
		val pensum = graph(
			nodes = listOf(approvedRequirement, pendingCorequisite, blockedNode, unlockedNode),
			edges = listOf(
				edge(
					fromNodeId = approvedRequirement.id,
					toNodeId = blockedNode.id
				),
				edge(
					fromNodeId = pendingCorequisite.id,
					toNodeId = blockedNode.id,
					relationshipType = PensumRelationshipType.COREQUISITE
				),
				edge(
					fromNodeId = blockedNode.id,
					toNodeId = unlockedNode.id
				)
			)
		)

		val model = observedPensum(
			nodes = pensum.nodes,
			pensum = pensum,
			nodeStatuses = mapOf(
				approvedRequirement.id to PensumNodeStatus.APPROVED,
				pendingCorequisite.id to PensumNodeStatus.AVAILABLE,
				blockedNode.id to PensumNodeStatus.BLOCKED,
				unlockedNode.id to PensumNodeStatus.BLOCKED
			)
		).toScreenModel()
		val detail = model.nodes.single { node -> node.id == blockedNode.id }.detail

		assertEquals(listOf(approvedRequirement.id), detail.requirements.map { item -> item.nodeId })
		assertEquals(listOf(pendingCorequisite.id), detail.corequisites.map { item -> item.nodeId })
		assertEquals(listOf(unlockedNode.id), detail.unlocks.map { item -> item.nodeId })
		assertEquals(listOf(pendingCorequisite.id), detail.blockingReasons.map { item -> item.nodeId })
		assertEquals(PensumEdgeRelationshipType.COREQUISITE, detail.blockingReasons.single().relationshipType)
		assertEquals(
			model.nodes.single { node -> node.id == pendingCorequisite.id }.visualStyle,
			detail.corequisites.single().visualStyle
		)
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
	nodeFulfillments: Map<String, PensumProgress.NodeFulfillment> = emptyMap(),
	nodeStatuses: Map<String, PensumNodeStatus> = nodes.associate { node -> node.id to PensumNodeStatus.AVAILABLE }
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
		nodeStatuses = nodeStatuses,
		nodeFulfillments = nodeFulfillments
	)
}

private fun graph(
	id: String = "computacion-2019-degree-project",
	modalityId: String = "degree_project",
	modalityName: String = "Proyecto de Grado",
	nodes: List<PensumGraph.Node> = emptyList(),
	edges: List<PensumGraph.Edge> = emptyList()
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
		edges = edges
	)
}

private fun edge(
	fromNodeId: String,
	toNodeId: String,
	relationshipType: PensumRelationshipType = PensumRelationshipType.REQUIREMENT
): PensumGraph.Edge {
	return PensumGraph.Edge(
		id = "${fromNodeId}_to_$toNodeId",
		fromNodeId = fromNodeId,
		toNodeId = toNodeId,
		relationshipType = relationshipType,
		points = listOf(
			PensumGraph.Point(x = 0.0, y = 0.0),
			PensumGraph.Point(x = 1.0, y = 1.0)
		)
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
