package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumGraph
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSnapshot
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicPensumStatusEngineTest {
	private val engine = AcademicPensumStatusEngine()

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

		assertEquals(AcademicPensumNodeStatus.APPROVED, result.nodeStatuses["ma1111"])
		assertEquals(AcademicPensumNodeStatus.CURRENT, result.nodeStatuses["ma2112"])
		assertEquals(AcademicPensumNodeStatus.AVAILABLE, result.nodeStatuses["ec5344"])
		assertEquals(AcademicPensumNodeStatus.BLOCKED, result.nodeStatuses["ec5754"])
		assertEquals(5, result.approvedCredits)
	}

	@Test
	fun resolvesApprovedSlotFromExplicitFulfillmentRule() {
		val slot = AcademicPensumGraph.Node(
			id = "area-slot",
			nodeType = AcademicPensumGraph.NodeType.SLOT,
			subjectCode = null,
			credits = 3,
			fulfillmentRules = listOf(
				AcademicPensumGraph.FulfillmentRule(
					ruleType = "SUBJECT_PREFIX",
					subjectCodes = emptyList(),
					subjectCodePrefixes = listOf("OP"),
					minCredits = 3,
					minSubjects = 1
				)
			)
		)

		val result = engine.resolve(
			pensum = samplePensum(nodes = listOf(slot), edges = emptyList()),
			academicSnapshot = AcademicPensumSnapshot(
				attempts = listOf(
					attempt("OP1111", TermKind.HISTORICAL, AttemptOutcome.APPROVED, credits = 3, name = "Topicos"),
					attempt("OP2222", TermKind.CURRENT, AttemptOutcome.PENDING, credits = 3)
				)
			)
		)

		assertEquals(AcademicPensumNodeStatus.APPROVED, result.nodeStatuses["area-slot"])
		assertEquals("OP1111", result.nodeFulfillments.getValue("area-slot").subjectCode)
		assertEquals("Topicos", result.nodeFulfillments.getValue("area-slot").subjectName)
		assertEquals(3, result.approvedCredits)
	}

	@Test
	fun resolvesApprovedCourseViaEquivalenceRule() {
		val course = courseWithEquivalence(id = "lla111", code = "LLA111", equivalentCodes = listOf("LL1111"), credits = 3)

		val result = engine.resolve(
			pensum = samplePensum(nodes = listOf(course), edges = emptyList()),
			academicSnapshot = AcademicPensumSnapshot(
				attempts = listOf(attempt("LL1111", TermKind.HISTORICAL, AttemptOutcome.APPROVED, credits = 3, name = "Lenguaje I"))
			)
		)

		assertEquals(AcademicPensumNodeStatus.APPROVED, result.nodeStatuses["lla111"])
		assertEquals("LL1111", result.nodeFulfillments.getValue("lla111").subjectCode)
		assertEquals("Lenguaje I", result.nodeFulfillments.getValue("lla111").subjectName)
		assertEquals(3, result.approvedCredits)
	}

	@Test
	fun resolvesCurrentCourseViaEquivalenceRule() {
		val course = courseWithEquivalence(id = "lla112", code = "LLA112", equivalentCodes = listOf("LL1112"), credits = 3)

		val result = engine.resolve(
			pensum = samplePensum(nodes = listOf(course), edges = emptyList()),
			academicSnapshot = AcademicPensumSnapshot(
				attempts = listOf(attempt("LL1112", TermKind.CURRENT, AttemptOutcome.PENDING, credits = 3))
			)
		)

		assertEquals(AcademicPensumNodeStatus.CURRENT, result.nodeStatuses["lla112"])
		assertEquals("LL1112", result.nodeFulfillments.getValue("lla112").subjectCode)
	}

	@Test
	fun approvingByItsOwnCode_neverEmitsAFulfillmentEvenWithAnEquivalenceRulePresent() {
		val course = courseWithEquivalence(id = "lla111", code = "LLA111", equivalentCodes = listOf("LL1111"), credits = 3)

		val result = engine.resolve(
			pensum = samplePensum(nodes = listOf(course), edges = emptyList()),
			academicSnapshot = AcademicPensumSnapshot(
				attempts = listOf(attempt("LLA111", TermKind.HISTORICAL, AttemptOutcome.APPROVED, credits = 3))
			)
		)

		assertEquals(AcademicPensumNodeStatus.APPROVED, result.nodeStatuses["lla111"])
		assertEquals(0, result.nodeFulfillments.size)
	}

	@Test
	fun aRuleTypeOtherThanEquivalence_neverApprovesACourseNodeByAMatchingAttempt() {
		val course = AcademicPensumGraph.Node(
			id = "lla111",
			nodeType = AcademicPensumGraph.NodeType.COURSE,
			subjectCode = "LLA111",
			credits = 3,
			fulfillmentRules = listOf(
				AcademicPensumGraph.FulfillmentRule(
					ruleType = "SUBJECT_PREFIX",
					subjectCodes = emptyList(),
					subjectCodePrefixes = listOf("LL"),
					minCredits = null,
					minSubjects = null
				)
			)
		)

		val result = engine.resolve(
			pensum = samplePensum(nodes = listOf(course), edges = emptyList()),
			academicSnapshot = AcademicPensumSnapshot(
				attempts = listOf(attempt("LL1111", TermKind.HISTORICAL, AttemptOutcome.APPROVED, credits = 3))
			)
		)

		assertEquals(AcademicPensumNodeStatus.AVAILABLE, result.nodeStatuses["lla111"])
		assertEquals(0, result.nodeFulfillments.size)
	}

	private fun courseWithEquivalence(
		id: String,
		code: String,
		equivalentCodes: List<String>,
		credits: Int
	): AcademicPensumGraph.Node {
		return AcademicPensumGraph.Node(
			id = id,
			nodeType = AcademicPensumGraph.NodeType.COURSE,
			subjectCode = code,
			credits = credits,
			fulfillmentRules = listOf(
				AcademicPensumGraph.FulfillmentRule(
					ruleType = "EQUIVALENCE",
					subjectCodes = equivalentCodes,
					subjectCodePrefixes = emptyList(),
					minCredits = null,
					minSubjects = null
				)
			)
		)
	}

	private fun samplePensum(
		nodes: List<AcademicPensumGraph.Node> = listOf(
			course("ma1111", "MA1111", credits = 5),
			course("ma2112", "MA2112", credits = 5),
			course("ec5344", "EC5344", credits = 3),
			course("ec5754", "EC5754", credits = 4)
		),
		edges: List<AcademicPensumGraph.Edge> = listOf(
			edge("ma1111", "ma2112"),
			edge("ma2112", "ec5344", AcademicPensumGraph.RelationshipType.COREQUISITE),
			edge("ec5344", "ec5754")
		)
	): AcademicPensumGraph {
		return AcademicPensumGraph(
			nodes = nodes,
			edges = edges
		)
	}

	private fun course(id: String, code: String, credits: Int): AcademicPensumGraph.Node {
		return AcademicPensumGraph.Node(
			id = id,
			nodeType = AcademicPensumGraph.NodeType.COURSE,
			subjectCode = code,
			credits = credits,
			fulfillmentRules = emptyList()
		)
	}

	private fun edge(
		from: String,
		to: String,
		type: AcademicPensumGraph.RelationshipType = AcademicPensumGraph.RelationshipType.REQUIREMENT
	): AcademicPensumGraph.Edge {
		return AcademicPensumGraph.Edge(
			fromNodeId = from,
			toNodeId = to,
			relationshipType = type
		)
	}

	private fun attempt(
		code: String,
		termKind: TermKind,
		outcome: AttemptOutcome,
		credits: Int = 5,
		name: String = code,
		termOrder: Int = 20201,
		positionInTerm: Int = 0
	): AcademicPensumSnapshot.Attempt {
		return AcademicPensumSnapshot.Attempt(
			id = "$termOrder-$positionInTerm-$code",
			subjectCode = code,
			subjectName = name,
			credits = credits,
			termOrder = termOrder,
			positionInTerm = positionInTerm,
			termKind = termKind,
			outcome = outcome
		)
	}
}
