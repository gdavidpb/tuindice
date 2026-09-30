package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumGraph
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumProgress
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSlotEligibility
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSlotKind
import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicPensumSlotEligibilityResolverTest {
	private val pensum = AcademicPensumGraph(
		nodes = listOf(
			course(id = "ci2525", code = "CI2525", equivalences = listOf("CI2521")),
			course(id = "eg1211", code = "EG1211"),
			slot(id = "elective-1", kind = "ELECTIVE", codes = listOf("MC5123", "MC5122", "EG1211", "CI2521"), minCredits = 3),
			slot(id = "elective-2", kind = "ELECTIVE", codes = listOf("MC5122")),
			slot(id = "eg-1", kind = "GENERAL", codes = listOf("CSX316", "IDW511"))
		),
		edges = emptyList()
	)

	@Test
	fun eligibility_labelsEachSubjectAgainstTheSlotsStillOpen() {
		cases().forEach { case ->
			val resolver = AcademicPensumSlotEligibilityResolver(
				pensum = pensum,
				progress = AcademicPensumProgress(
					approvedCredits = 0,
					nodeStatuses = case.statuses,
					nodeFulfillments = emptyMap()
				)
			)

			assertEquals(
				case.expected,
				resolver.eligibilityOf(subjectCode = case.subjectCode, credits = case.credits),
				case.description
			)
		}
	}

	private data class Case(
		val description: String,
		val statuses: Map<String, AcademicPensumNodeStatus>,
		val subjectCode: String,
		val credits: Int,
		val expected: AcademicPensumSlotEligibility
	)

	private fun cases(): List<Case> {
		val nothingApproved = mapOf(
			"elective-1" to AcademicPensumNodeStatus.AVAILABLE,
			"elective-2" to AcademicPensumNodeStatus.AVAILABLE,
			"eg-1" to AcademicPensumNodeStatus.AVAILABLE
		)
		val elective = AcademicPensumSlotEligibility.CountsTowardSlot(AcademicPensumSlotKind.ELECTIVE)
		val notEligible = AcademicPensumSlotEligibility.NotEligible

		return listOf(
			Case("an open elective slot accepts it", nothingApproved, "MC5123", 3, elective),
			Case(
				"an open general studies slot accepts it",
				nothingApproved,
				" csx316 ",
				3,
				AcademicPensumSlotEligibility.CountsTowardSlot(AcademicPensumSlotKind.GENERAL_STUDIES)
			),
			Case(
				"its only slot is approved already",
				nothingApproved + ("elective-1" to AcademicPensumNodeStatus.APPROVED),
				"MC5123",
				3,
				AcademicPensumSlotEligibility.SlotsFilled(AcademicPensumSlotKind.ELECTIVE)
			),
			Case(
				"a later slot is still open",
				nothingApproved + ("elective-1" to AcademicPensumNodeStatus.APPROVED),
				"MC5122",
				3,
				elective
			),
			Case(
				"a slot filled by a current attempt still counts as open",
				nothingApproved + ("elective-1" to AcademicPensumNodeStatus.CURRENT),
				"MC5123",
				3,
				elective
			),
			Case("below the slot's minimum credits", nothingApproved, "MC5123", 2, notEligible),
			Case("a fixed course never fills a slot", nothingApproved, "EG1211", 3, notEligible),
			Case("a course equivalence never fills a slot", nothingApproved, "CI2521", 4, notEligible),
			Case("no slot accepts it", nothingApproved, "ZZ9999", 3, notEligible)
		)
	}

	private fun course(id: String, code: String, equivalences: List<String> = emptyList()): AcademicPensumGraph.Node {
		return AcademicPensumGraph.Node(
			id = id,
			nodeType = AcademicPensumGraph.NodeType.COURSE,
			subjectCode = code,
			credits = 4,
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
			}
		)
	}

	private fun slot(id: String, kind: String, codes: List<String>, minCredits: Int? = null): AcademicPensumGraph.Node {
		return AcademicPensumGraph.Node(
			id = id,
			nodeType = AcademicPensumGraph.NodeType.SLOT,
			subjectCode = null,
			credits = 3,
			fulfillmentRules = listOf(
				AcademicPensumGraph.FulfillmentRule(
					ruleType = "SUBJECT_ELIGIBILITY",
					subjectCodes = codes,
					subjectCodePrefixes = emptyList(),
					minCredits = minCredits,
					minSubjects = 1,
					slotEligibilityKind = kind
				)
			)
		)
	}
}
