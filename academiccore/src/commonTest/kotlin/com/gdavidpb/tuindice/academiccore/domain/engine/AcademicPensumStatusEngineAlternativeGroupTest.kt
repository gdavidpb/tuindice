package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumGraph
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus.APPROVED
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus.AVAILABLE
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus.BLOCKED
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus.CURRENT
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumProgress
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSnapshot
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import kotlin.test.Test
import kotlin.test.assertEquals

// Matemáticas I as Ingeniería de Computación splits it: MA1111 (regular) or MA1121 (honor), both
// requirements of FS1111 and of Matemáticas II (again split: MA1112 or MA1122).
class AcademicPensumStatusEngineAlternativeGroupTest {
	private val engine = AcademicPensumStatusEngine()

	private data class Case(
		val name: String,
		val pensum: AcademicPensumGraph,
		val attempts: List<AcademicPensumSnapshot.Attempt>,
		val statuses: Map<String, AcademicPensumNodeStatus>,
		val fulfillments: Map<String, String>,
		val approvedCredits: Int
	)

	@Test
	fun resolvesAlternativeGroupsAsOneRequirement() {
		cases().forEach { case ->
			val progress = engine.resolve(case.pensum, AcademicPensumSnapshot(attempts = case.attempts))

			case.statuses.forEach { (nodeId, status) ->
				assertEquals(status, progress.nodeStatuses[nodeId], "${case.name}: status of $nodeId")
			}
			assertEquals(case.fulfillments, progress.fulfilledCodes(), "${case.name}: fulfillments")
			assertEquals(case.approvedCredits, progress.approvedCredits, "${case.name}: approved credits")
		}
	}

	private fun cases(): List<Case> = singleMemberCases() + groupInteractionCases()

	private fun singleMemberCases(): List<Case> {
		return listOf(
			Case(
				name = "regular member approved approves the honor sibling and unlocks downstream",
				pensum = splitPensum(),
				attempts = listOf(approved("MA1111", name = "Matematicas I")),
				statuses = mapOf(
					"math1" to APPROVED,
					"math1-honor" to APPROVED,
					"fs1111" to AVAILABLE,
					"math2" to AVAILABLE,
					"math2-honor" to AVAILABLE
				),
				fulfillments = mapOf("math1-honor" to "MA1111"),
				approvedCredits = 4
			),
			Case(
				name = "honor member approved approves the regular sibling",
				pensum = splitPensum(),
				attempts = listOf(approved("MA1121", credits = 5)),
				statuses = mapOf(
					"math1" to APPROVED,
					"math1-honor" to APPROVED,
					"fs1111" to AVAILABLE,
					"math2" to AVAILABLE
				),
				fulfillments = mapOf("math1" to "MA1121"),
				approvedCredits = 5
			),
			Case(
				name = "a member approved via EQUIVALENCE approves the group with the legacy attempt",
				pensum = splitPensum(math1Equivalences = listOf("MA1011")),
				attempts = listOf(approved("MA1011")),
				statuses = mapOf("math1" to APPROVED, "math1-honor" to APPROVED, "fs1111" to AVAILABLE),
				fulfillments = mapOf("math1" to "MA1011", "math1-honor" to "MA1011"),
				approvedCredits = 4
			),
			Case(
				name = "a current member makes the sibling current, downstream stays blocked",
				pensum = splitPensum(),
				attempts = listOf(current("MA1121")),
				statuses = mapOf(
					"math1" to CURRENT,
					"math1-honor" to CURRENT,
					"fs1111" to BLOCKED,
					"math2" to BLOCKED
				),
				fulfillments = mapOf("math1" to "MA1121"),
				approvedCredits = 0
			),
		)
	}

	private fun groupInteractionCases(): List<Case> {
		return listOf(
			Case(
				name = "approved wins over current: retaking the sibling never downgrades the group",
				pensum = splitPensum(),
				attempts = listOf(approved("MA1111", termOrder = 20191), current("MA1121")),
				statuses = mapOf("math1" to APPROVED, "math1-honor" to APPROVED, "fs1111" to AVAILABLE),
				fulfillments = mapOf("math1-honor" to "MA1111"),
				approvedCredits = 4
			),
			Case(
				name = "both members passed: credits count once, no fulfillment on either",
				pensum = splitPensum(),
				attempts = listOf(approved("MA1111", termOrder = 20191), approved("MA1121", credits = 5)),
				statuses = mapOf("math1" to APPROVED, "math1-honor" to APPROVED),
				fulfillments = emptyMap(),
				approvedCredits = 5
			),
			Case(
				name = "two chained groups: one member of each unlocks the next level",
				pensum = splitPensum(),
				attempts = listOf(approved("MA1121", termOrder = 20191), approved("MA1112")),
				statuses = mapOf(
					"math1" to APPROVED,
					"math1-honor" to APPROVED,
					"math2" to APPROVED,
					"math2-honor" to APPROVED,
					"fs1111" to AVAILABLE
				),
				fulfillments = mapOf("math1" to "MA1121", "math2-honor" to "MA1112"),
				approvedCredits = 9
			),
			Case(
				name = "without groups the split nodes behave as today: all requirements needed",
				pensum = splitPensum(grouped = false),
				attempts = listOf(approved("MA1111")),
				statuses = mapOf(
					"math1" to APPROVED,
					"math1-honor" to AVAILABLE,
					"fs1111" to BLOCKED,
					"math2" to BLOCKED
				),
				fulfillments = emptyMap(),
				approvedCredits = 4
			)
		)
	}

	@Test
	fun siblingFulfillmentCarriesTheApprovingAttemptName() {
		val progress = engine.resolve(
			splitPensum(),
			AcademicPensumSnapshot(attempts = listOf(approved("MA1111", name = "Matematicas I")))
		)

		assertEquals(
			AcademicPensumProgress.NodeFulfillment(subjectCode = "MA1111", subjectName = "Matematicas I"),
			progress.nodeFulfillments["math1-honor"]
		)
	}

	private fun AcademicPensumProgress.fulfilledCodes(): Map<String, String> {
		return nodeFulfillments.mapValues { (_, fulfillment) -> fulfillment.subjectCode }
	}

	private fun splitPensum(
		grouped: Boolean = true,
		math1Equivalences: List<String> = emptyList()
	): AcademicPensumGraph {
		val math1Group = "t1-math1".takeIf { grouped }
		val math2Group = "t2-math2".takeIf { grouped }
		return AcademicPensumGraph(
			nodes = listOf(
				course("math1", "MA1111", credits = 4, group = math1Group, equivalences = math1Equivalences),
				course("math1-honor", "MA1121", credits = 5, group = math1Group),
				course("math2", "MA1112", credits = 4, group = math2Group),
				course("math2-honor", "MA1122", credits = 5, group = math2Group),
				course("fs1111", "FS1111", credits = 4)
			),
			edges = listOf(
				requirement("math1", "fs1111"),
				requirement("math1-honor", "fs1111"),
				requirement("math1", "math2"),
				requirement("math1-honor", "math2"),
				requirement("math1", "math2-honor"),
				requirement("math1-honor", "math2-honor")
			)
		)
	}

	private fun course(
		id: String,
		code: String,
		credits: Int,
		group: String? = null,
		equivalences: List<String> = emptyList()
	): AcademicPensumGraph.Node {
		return AcademicPensumGraph.Node(
			id = id,
			nodeType = AcademicPensumGraph.NodeType.COURSE,
			subjectCode = code,
			credits = credits,
			fulfillmentRules = if (equivalences.isEmpty()) {
				emptyList()
			} else {
				listOf(
					AcademicPensumGraph.FulfillmentRule(
						ruleType = "EQUIVALENCE",
						subjectCodes = equivalences,
						subjectCodePrefixes = emptyList(),
						minCredits = null,
						minSubjects = null
					)
				)
			},
			alternativeGroup = group
		)
	}

	private fun requirement(from: String, to: String): AcademicPensumGraph.Edge {
		return AcademicPensumGraph.Edge(
			fromNodeId = from,
			toNodeId = to,
			relationshipType = AcademicPensumGraph.RelationshipType.REQUIREMENT
		)
	}

	private fun approved(
		code: String,
		credits: Int = 4,
		name: String = code,
		termOrder: Int = 20201
	): AcademicPensumSnapshot.Attempt {
		return attempt(code, credits, name, termOrder, AttemptOutcome.APPROVED)
	}

	private fun current(code: String): AcademicPensumSnapshot.Attempt {
		return attempt(code, credits = 5, name = code, termOrder = 20203, AttemptOutcome.PENDING)
	}

	private fun attempt(
		code: String,
		credits: Int,
		name: String,
		termOrder: Int,
		outcome: AttemptOutcome
	): AcademicPensumSnapshot.Attempt {
		// Approved attempts are historical; anything else is being taken this term.
		val termKind = if (outcome == AttemptOutcome.APPROVED) TermKind.HISTORICAL else TermKind.CURRENT
		return AcademicPensumSnapshot.Attempt(
			id = "$termOrder-$code",
			subjectCode = code,
			subjectName = name,
			credits = credits,
			termOrder = termOrder,
			positionInTerm = 0,
			termKind = termKind,
			outcome = outcome
		)
	}
}
