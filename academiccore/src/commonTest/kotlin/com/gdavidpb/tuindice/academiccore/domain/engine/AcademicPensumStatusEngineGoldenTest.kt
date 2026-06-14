package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumGraph
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSnapshot
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicPensumStatusEngineGoldenTest {
	private val engine = AcademicPensumStatusEngine()

	@Test
	fun requirementChain_unlocksOneLevelPerApproval() {
		val pensum = AcademicPensumGraph(
			nodes = listOf(
				course(id = "a", code = "MA1111", credits = 4),
				course(id = "b", code = "MA1112", credits = 4),
				course(id = "c", code = "MA2115", credits = 4)
			),
			edges = listOf(
				requirement(from = "a", to = "b"),
				requirement(from = "b", to = "c")
			)
		)

		val progress = engine.resolve(
			pensum,
			snapshot(approved(id = "s1", code = "MA1111"))
		)

		assertEquals(AcademicPensumNodeStatus.APPROVED, progress.nodeStatuses["a"])
		assertEquals(AcademicPensumNodeStatus.AVAILABLE, progress.nodeStatuses["b"])
		assertEquals(AcademicPensumNodeStatus.BLOCKED, progress.nodeStatuses["c"])
		assertEquals(4, progress.approvedCredits)
	}

	@Test
	fun corequisite_unlocksFromCurrentTerm_whileRequirementStaysBlocked() {
		val pensum = AcademicPensumGraph(
			nodes = listOf(
				course(id = "a", code = "MA1111", credits = 4),
				course(id = "coreq", code = "FS1111", credits = 3),
				course(id = "req", code = "QM1181", credits = 3)
			),
			edges = listOf(
				AcademicPensumGraph.Edge(
					fromNodeId = "a",
					toNodeId = "coreq",
					relationshipType = AcademicPensumGraph.RelationshipType.COREQUISITE
				),
				requirement(from = "a", to = "req")
			)
		)

		val progress = engine.resolve(
			pensum,
			snapshot(
				attempt(id = "s1", code = "MA1111", termKind = TermKind.CURRENT, outcome = AttemptOutcome.PENDING)
			)
		)

		assertEquals(AcademicPensumNodeStatus.CURRENT, progress.nodeStatuses["a"])
		assertEquals(AcademicPensumNodeStatus.AVAILABLE, progress.nodeStatuses["coreq"])
		assertEquals(AcademicPensumNodeStatus.BLOCKED, progress.nodeStatuses["req"])
	}

	@Test
	fun slotFulfillment_matchesByPrefixAndMinCredits_withoutReusingAttempts() {
		val pensum = AcademicPensumGraph(
			nodes = listOf(
				slot(id = "slot1", prefixes = listOf("EP"), minCredits = 2),
				slot(id = "slot2", prefixes = listOf("EP"), minCredits = 2)
			),
			edges = emptyList()
		)

		val progress = engine.resolve(
			pensum,
			snapshot(approved(id = "s1", code = "EP1308", credits = 3))
		)

		assertEquals(AcademicPensumNodeStatus.APPROVED, progress.nodeStatuses["slot1"])
		assertEquals(AcademicPensumNodeStatus.AVAILABLE, progress.nodeStatuses["slot2"])
		assertEquals(
			"EP1308",
			progress.nodeFulfillments.getValue("slot1").subjectCode
		)
		assertEquals(1, progress.nodeFulfillments.size)
	}

	@Test
	fun slotRules_genericElectiveAndMultiSubjectMinimums_neverMatch() {
		val pensum = AcademicPensumGraph(
			nodes = listOf(
				AcademicPensumGraph.Node(
					id = "generic",
					nodeType = AcademicPensumGraph.NodeType.SLOT,
					subjectCode = null,
					credits = 3,
					fulfillmentRules = listOf(
						AcademicPensumGraph.FulfillmentRule(
							ruleType = "GENERIC_ELECTIVE",
							subjectCodes = listOf("EP1308"),
							subjectCodePrefixes = emptyList(),
							minCredits = null,
							minSubjects = null
						)
					)
				),
				AcademicPensumGraph.Node(
					id = "multi",
					nodeType = AcademicPensumGraph.NodeType.SLOT,
					subjectCode = null,
					credits = 3,
					fulfillmentRules = listOf(
						AcademicPensumGraph.FulfillmentRule(
							ruleType = "ELECTIVE",
							subjectCodes = listOf("EP1308"),
							subjectCodePrefixes = emptyList(),
							minCredits = null,
							minSubjects = 2
						)
					)
				)
			),
			edges = emptyList()
		)

		val progress = engine.resolve(
			pensum,
			snapshot(approved(id = "s1", code = "EP1308", credits = 3))
		)

		assertEquals(AcademicPensumNodeStatus.AVAILABLE, progress.nodeStatuses["generic"])
		assertEquals(AcademicPensumNodeStatus.AVAILABLE, progress.nodeStatuses["multi"])
		assertEquals(0, progress.nodeFulfillments.size)
	}

	@Test
	fun fixedCourseSubjects_approveTheirCourseNode_andStayOutOfSlotPools() {
		val pensum = AcademicPensumGraph(
			nodes = listOf(
				course(id = "course", code = "EP1308", credits = 3),
				slot(id = "slot", prefixes = listOf("EP"), minCredits = null)
			),
			edges = emptyList()
		)

		val progress = engine.resolve(
			pensum,
			snapshot(approved(id = "s1", code = "EP1308", credits = 3))
		)

		assertEquals(AcademicPensumNodeStatus.APPROVED, progress.nodeStatuses["course"])
		assertEquals(AcademicPensumNodeStatus.AVAILABLE, progress.nodeStatuses["slot"])
		assertEquals(0, progress.nodeFulfillments.size)
		assertEquals(3, progress.approvedCredits)
	}
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

private fun slot(id: String, prefixes: List<String>, minCredits: Int?): AcademicPensumGraph.Node {
	return AcademicPensumGraph.Node(
		id = id,
		nodeType = AcademicPensumGraph.NodeType.SLOT,
		subjectCode = null,
		credits = 3,
		fulfillmentRules = listOf(
			AcademicPensumGraph.FulfillmentRule(
				ruleType = "ELECTIVE",
				subjectCodes = emptyList(),
				subjectCodePrefixes = prefixes,
				minCredits = minCredits,
				minSubjects = null
			)
		)
	)
}

private fun requirement(from: String, to: String): AcademicPensumGraph.Edge {
	return AcademicPensumGraph.Edge(
		fromNodeId = from,
		toNodeId = to,
		relationshipType = AcademicPensumGraph.RelationshipType.REQUIREMENT
	)
}

private fun snapshot(vararg attempts: AcademicPensumSnapshot.Attempt): AcademicPensumSnapshot {
	return AcademicPensumSnapshot(attempts = attempts.toList())
}

private fun approved(id: String, code: String, credits: Int = 4): AcademicPensumSnapshot.Attempt {
	return attempt(id = id, code = code, credits = credits, termKind = TermKind.HISTORICAL, outcome = AttemptOutcome.APPROVED)
}

private fun attempt(
	id: String,
	code: String,
	credits: Int = 4,
	termKind: TermKind,
	outcome: AttemptOutcome
): AcademicPensumSnapshot.Attempt {
	return AcademicPensumSnapshot.Attempt(
		id = id,
		subjectCode = code,
		subjectName = code,
		credits = credits,
		termOrder = 20241,
		positionInTerm = 0,
		termKind = termKind,
		outcome = outcome
	)
}
