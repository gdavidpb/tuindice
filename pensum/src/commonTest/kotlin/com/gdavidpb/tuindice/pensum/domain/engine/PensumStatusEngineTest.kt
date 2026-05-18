package com.gdavidpb.tuindice.pensum.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.pensum.domain.model.AcademicPensumSnapshot
import com.gdavidpb.tuindice.pensum.domain.model.PensumGraph
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeStatus
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeType
import com.gdavidpb.tuindice.pensum.domain.model.PensumRelationshipType
import kotlin.test.Test
import kotlin.test.assertEquals

class PensumStatusEngineTest {
	private val engine = PensumStatusEngine()

	@Test
	fun resolvesApprovedCurrentAvailableAndBlockedCourses() {
		val result = engine.resolve(
			pensum = samplePensum(),
			academicSnapshot = AcademicPensumSnapshot(
				attempts = listOf(
					attempt("MA1111", TermKind.HISTORICAL, AttemptOutcome.APPROVED),
					attempt("MA2112", TermKind.CURRENT, AttemptOutcome.PENDING)
				)
			)
		)

		assertEquals(PensumNodeStatus.APPROVED, result.nodeStatuses["ma1111"])
		assertEquals(PensumNodeStatus.CURRENT, result.nodeStatuses["ma2112"])
		assertEquals(PensumNodeStatus.AVAILABLE, result.nodeStatuses["ec5344"])
		assertEquals(PensumNodeStatus.BLOCKED, result.nodeStatuses["ec5754"])
		assertEquals(5, result.approvedCredits)
	}

	@Test
	fun resolvesSlotsFromFulfillmentRules() {
		val slot = PensumGraph.Node(
			id = "eg-slot",
			nodeType = PensumNodeType.SLOT,
			displayCode = "EG",
			subjectCode = null,
			name = "Estudios Generales",
			credits = 3,
			category = "GENERAL_STUDIES",
			termId = "T1",
			x = 0.0,
			y = 0.0,
			width = 120.0,
			height = 90.0,
			fulfillmentRules = listOf(
				PensumGraph.FulfillmentRule(
					id = "rule",
					ruleType = "SUBJECT_PREFIX",
					subjectCodes = emptyList(),
					subjectCodePrefixes = listOf("EG"),
					minCredits = 3,
					minSubjects = 1
				)
			)
		)
		val result = engine.resolve(
			pensum = samplePensum(nodes = listOf(slot), edges = emptyList()),
			academicSnapshot = AcademicPensumSnapshot(
				attempts = listOf(attempt("EG1111", TermKind.HISTORICAL, AttemptOutcome.APPROVED, credits = 3))
			)
		)

		assertEquals(PensumNodeStatus.APPROVED, result.nodeStatuses["eg-slot"])
		assertEquals(3, result.approvedCredits)
	}

	private fun samplePensum(
		nodes: List<PensumGraph.Node> = listOf(
			course("ma1111", "MA1111", "Calculo I", credits = 5),
			course("ma2112", "MA2112", "Calculo II", credits = 5),
			course("ec5344", "EC5344", "Radiacion y Antenas", credits = 3),
			course("ec5754", "EC5754", "Procesamiento Concurrente", credits = 4)
		),
		edges: List<PensumGraph.Edge> = listOf(
			edge("ma1111", "ma2112"),
			edge("ma2112", "ec5344", PensumRelationshipType.COREQUISITE),
			edge("ec5344", "ec5754")
		)
	): PensumGraph {
		return PensumGraph(
			id = "computacion-2019",
			year = 2019,
			modalityId = "degree_project",
			modalityName = "Proyecto de Grado",
			totalCredits = 170,
			canvas = PensumGraph.Canvas(width = 1200.0, height = 900.0),
			terms = emptyList(),
			nodes = nodes,
			edges = edges
		)
	}

	private fun course(id: String, code: String, name: String, credits: Int): PensumGraph.Node {
		return PensumGraph.Node(
			id = id,
			nodeType = PensumNodeType.COURSE,
			displayCode = code,
			subjectCode = code,
			name = name,
			credits = credits,
			category = "PROFESSIONAL",
			termId = "T1",
			x = 0.0,
			y = 0.0,
			width = 120.0,
			height = 90.0,
			fulfillmentRules = emptyList()
		)
	}

	private fun edge(
		from: String,
		to: String,
		type: PensumRelationshipType = PensumRelationshipType.REQUIREMENT
	): PensumGraph.Edge {
		return PensumGraph.Edge(
			id = "$from-$to",
			fromNodeId = from,
			toNodeId = to,
			relationshipType = type,
			points = emptyList()
		)
	}

	private fun attempt(
		code: String,
		termKind: TermKind,
		outcome: AttemptOutcome,
		credits: Int = 5
	): AcademicPensumSnapshot.Attempt {
		return AcademicPensumSnapshot.Attempt(
			subjectCode = code,
			credits = credits,
			termKind = termKind,
			outcome = outcome
		)
	}
}
