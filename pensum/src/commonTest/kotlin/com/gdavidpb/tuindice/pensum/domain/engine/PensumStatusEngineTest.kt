package com.gdavidpb.tuindice.pensum.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSnapshot
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
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
	fun resolvesApprovedSlotFromExplicitFulfillmentRule() {
		val slot = PensumGraph.Node(
			id = "area-slot",
			nodeType = PensumNodeType.SLOT,
			displayCode = "EA",
			subjectCode = null,
			name = "Electiva de Area",
			credits = 3,
			category = "AREA_ELECTIVE",
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
					subjectCodePrefixes = listOf("OP"),
					slotEligibilityKind = null,
					minCredits = 3,
					minSubjects = 1
				)
			)
		)
		val result = engine.resolve(
			pensum = samplePensum(nodes = listOf(slot), edges = emptyList()),
			academicSnapshot = AcademicPensumSnapshot(
				attempts = listOf(
					attempt("OP1111", TermKind.HISTORICAL, AttemptOutcome.APPROVED, credits = 3, name = "Topicos de Area"),
					attempt("OP2222", TermKind.CURRENT, AttemptOutcome.PENDING, credits = 3)
				)
			)
		)

		assertEquals(PensumNodeStatus.APPROVED, result.nodeStatuses["area-slot"])
		assertEquals("OP1111", result.nodeFulfillments.getValue("area-slot").subjectCode)
		assertEquals("Topicos de Area", result.nodeFulfillments.getValue("area-slot").subjectName)
		assertEquals(3, result.approvedCredits)
	}

	@Test
	fun resolvesCurrentSlotWithoutAddingApprovedCredits() {
		val generalStudiesSlot = slot(
			id = "eg-slot",
			displayCode = "EG",
			category = "GENERAL_STUDIES",
			prefixes = listOf("EG")
		)
		val result = engine.resolve(
			pensum = samplePensum(nodes = listOf(generalStudiesSlot), edges = emptyList()),
			academicSnapshot = AcademicPensumSnapshot(
				attempts = listOf(
					attempt("EG1111", TermKind.CURRENT, AttemptOutcome.PENDING, credits = 3, name = "Estudio General")
				)
			)
		)

		assertEquals(PensumNodeStatus.CURRENT, result.nodeStatuses["eg-slot"])
		assertEquals("EG1111", result.nodeFulfillments.getValue("eg-slot").subjectCode)
		assertEquals(0, result.approvedCredits)
	}

	@Test
	fun doesNotFulfillSlotsWithFixedCoursesFromSamePensum() {
		val fixedCourse = course("ma1111", "MA1111", "Calculo I", credits = 5)
		val electiveSlot = slot(
			id = "el-slot",
			displayCode = "EL",
			category = "FREE_ELECTIVE",
			prefixes = listOf("MA")
		)
		val result = engine.resolve(
			pensum = samplePensum(nodes = listOf(fixedCourse, electiveSlot), edges = emptyList()),
			academicSnapshot = AcademicPensumSnapshot(
				attempts = listOf(
					attempt("MA1111", TermKind.HISTORICAL, AttemptOutcome.APPROVED, credits = 5)
				)
			)
		)

		assertEquals(PensumNodeStatus.APPROVED, result.nodeStatuses["ma1111"])
		assertEquals(PensumNodeStatus.AVAILABLE, result.nodeStatuses["el-slot"])
		assertEquals(false, result.nodeFulfillments.containsKey("el-slot"))
		assertEquals(5, result.approvedCredits)
	}

	@Test
	fun assignsEquivalentSlotsChronologicallyByPensumOrder() {
		val firstSlot = slot(
			id = "eg-slot-1",
			displayCode = "EG",
			category = "GENERAL_STUDIES",
			prefixes = listOf("EG")
		)
		val secondSlot = slot(
			id = "eg-slot-2",
			displayCode = "EG",
			category = "GENERAL_STUDIES",
			prefixes = listOf("EG")
		)
		val result = engine.resolve(
			pensum = samplePensum(nodes = listOf(firstSlot, secondSlot), edges = emptyList()),
			academicSnapshot = AcademicPensumSnapshot(
				attempts = listOf(
					attempt(
						code = "EG2222",
						termKind = TermKind.HISTORICAL,
						outcome = AttemptOutcome.APPROVED,
						credits = 3,
						termOrder = 20202
					),
					attempt(
						code = "EG1111",
						termKind = TermKind.HISTORICAL,
						outcome = AttemptOutcome.APPROVED,
						credits = 3,
						termOrder = 20201
					)
				)
			)
		)

		assertEquals(PensumNodeStatus.APPROVED, result.nodeStatuses["eg-slot-1"])
		assertEquals(PensumNodeStatus.APPROVED, result.nodeStatuses["eg-slot-2"])
		assertEquals("EG1111", result.nodeFulfillments.getValue("eg-slot-1").subjectCode)
		assertEquals("EG2222", result.nodeFulfillments.getValue("eg-slot-2").subjectCode)
		assertEquals(6, result.approvedCredits)
	}

	@Test
	fun doesNotResolveSlotWithoutExplicitRule() {
		val affineSlot = slot(
			id = "af-slot",
			displayCode = "AF",
			category = "FREE_ELECTIVE",
			prefixes = emptyList()
		).copy(fulfillmentRules = emptyList())
		val result = engine.resolve(
			pensum = samplePensum(nodes = listOf(affineSlot), edges = emptyList()),
			academicSnapshot = AcademicPensumSnapshot(
				attempts = listOf(
					attempt("MA3322", TermKind.HISTORICAL, AttemptOutcome.APPROVED, credits = 3)
				)
			)
		)

		assertEquals(PensumNodeStatus.AVAILABLE, result.nodeStatuses["af-slot"])
		assertEquals(false, result.nodeFulfillments.containsKey("af-slot"))
		assertEquals(0, result.approvedCredits)
	}

	@Test
	fun doesNotResolveGenericElectiveRulesEvenWhenPrefixesArePresent() {
		val genericSlot = slot(
			id = "generic-slot",
			displayCode = "EL",
			category = "FREE_ELECTIVE",
			prefixes = listOf("CI")
		).copy(
			fulfillmentRules = listOf(
				PensumGraph.FulfillmentRule(
					id = "generic-rule",
					ruleType = "GENERIC_ELECTIVE",
					subjectCodes = emptyList(),
					subjectCodePrefixes = listOf("CI"),
					slotEligibilityKind = null,
					minCredits = 3,
					minSubjects = 1
				)
			)
		)
		val result = engine.resolve(
			pensum = samplePensum(nodes = listOf(genericSlot), edges = emptyList()),
			academicSnapshot = AcademicPensumSnapshot(
				attempts = listOf(
					attempt("CI4321", TermKind.HISTORICAL, AttemptOutcome.APPROVED, credits = 3)
				)
			)
		)

		assertEquals(PensumNodeStatus.AVAILABLE, result.nodeStatuses["generic-slot"])
		assertEquals(false, result.nodeFulfillments.containsKey("generic-slot"))
		assertEquals(0, result.approvedCredits)
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

	private fun slot(
		id: String,
		displayCode: String,
		category: String,
		prefixes: List<String>,
		credits: Int = 3
	): PensumGraph.Node {
		return PensumGraph.Node(
			id = id,
			nodeType = PensumNodeType.SLOT,
			displayCode = displayCode,
			subjectCode = null,
			name = displayCode,
			credits = credits,
			category = category,
			termId = "T1",
			x = 0.0,
			y = 0.0,
			width = 120.0,
			height = 90.0,
			fulfillmentRules = listOf(
				PensumGraph.FulfillmentRule(
					id = "$id-rule",
					ruleType = "SUBJECT_PREFIX",
					subjectCodes = emptyList(),
					subjectCodePrefixes = prefixes,
					slotEligibilityKind = null,
					minCredits = credits,
					minSubjects = 1
				)
			)
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
