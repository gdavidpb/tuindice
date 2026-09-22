package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumGraph
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumProgress
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSlotKind
import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicPensumSlotRulesTest {
	@Test
	fun acceptsSubject_followsTheServedRuleSemantics() {
		data class Case(
			val description: String,
			val rule: AcademicPensumGraph.FulfillmentRule,
			val subjectCode: String,
			val credits: Int,
			val accepts: Boolean
		)

		val cases = listOf(
			Case("listed code", rule(codes = listOf("MC5123")), "MC5123", 3, accepts = true),
			Case("listed code, normalized", rule(codes = listOf(" mc5123 ")), " Mc5123 ", 3, accepts = true),
			Case("prefix", rule(ruleType = "SUBJECT_PREFIX", prefixes = listOf("CSX")), "CSX316", 3, accepts = true),
			Case("code outside the lists", rule(codes = listOf("MC5123")), "MC5124", 3, accepts = false),
			Case("rule with empty lists", rule(), "MC5123", 3, accepts = false),
			Case("blank subject code", rule(prefixes = listOf("MC")), "  ", 3, accepts = false),
			Case("below the minimum credits", rule(codes = listOf("MC5123")).copy(minCredits = 3), "MC5123", 2, accepts = false),
			Case("at the minimum credits", rule(codes = listOf("MC5123")).copy(minCredits = 3), "MC5123", 3, accepts = true),
			Case(
				"a slot asking for several subjects never takes one",
				rule(codes = listOf("MC5123")).copy(minSubjects = 2),
				"MC5123",
				3,
				accepts = false
			),
			Case(
				"generic elective lists never match",
				rule(ruleType = "GENERIC_ELECTIVE", codes = listOf("MC5123"), prefixes = listOf("MC")),
				"MC5123",
				3,
				accepts = false
			)
		)

		cases.forEach { case ->
			assertEquals(
				case.accepts,
				case.rule.acceptsSubject(subjectCode = case.subjectCode, credits = case.credits),
				case.description
			)
		}
	}

	@Test
	fun fixedCourseSubjectCodes_areCourseCodesAndTheirRuleCodes_neverSlotCodes() {
		val pensum = AcademicPensumGraph(
			nodes = listOf(
				course(
					id = "ci2525",
					code = "ci2525",
					rules = listOf(rule(ruleType = "EQUIVALENCE", codes = listOf("CI2521")))
				),
				course(id = "ma1111", code = "MA1111"),
				slot(id = "elective", rules = listOf(rule(codes = listOf("MC5123"))))
			),
			edges = emptyList()
		)

		assertEquals(setOf("CI2525", "CI2521", "MA1111"), pensum.fixedCourseSubjectCodes())
	}

	@Test
	fun slotKind_readsTheDeclaredKindFirst_thenTheCategory() {
		data class Case(val description: String, val node: AcademicPensumGraph.Node, val kind: AcademicPensumSlotKind)

		val cases = listOf(
			Case(
				"general eligibility kind",
				slot(rules = listOf(rule(kind = "GENERAL"))),
				AcademicPensumSlotKind.GENERAL_STUDIES
			),
			Case(
				"elective eligibility kind",
				slot(rules = listOf(rule(kind = " elective "))),
				AcademicPensumSlotKind.ELECTIVE
			),
			Case(
				"the declared kind wins over the category",
				slot(rules = listOf(rule(kind = "ELECTIVE")), category = "GENERAL_STUDIES"),
				AcademicPensumSlotKind.ELECTIVE
			),
			Case("general studies category", slot(category = "GENERAL_STUDIES"), AcademicPensumSlotKind.GENERAL_STUDIES),
			Case("area elective category", slot(category = "AREA_ELECTIVE"), AcademicPensumSlotKind.ELECTIVE),
			Case("free elective category", slot(category = "FREE_ELECTIVE"), AcademicPensumSlotKind.ELECTIVE),
			Case("unknown kind and no category", slot(rules = listOf(rule(kind = "FREE"))), AcademicPensumSlotKind.ELECTIVE)
		)

		cases.forEach { case -> assertEquals(case.kind, case.node.slotKind(), case.description) }
	}

	@Test
	fun fulfilledSubjectStatuses_mapEachFulfillingCodeToItsNodeStatus_approvedFirst() {
		val progress = AcademicPensumProgress(
			approvedCredits = 0,
			nodeStatuses = mapOf(
				"elective-1" to AcademicPensumNodeStatus.APPROVED,
				"elective-2" to AcademicPensumNodeStatus.CURRENT,
				"ci2525" to AcademicPensumNodeStatus.APPROVED,
				"eg-1" to AcademicPensumNodeStatus.CURRENT
			),
			nodeFulfillments = mapOf(
				"elective-2" to AcademicPensumProgress.NodeFulfillment(subjectCode = "MC5123", subjectName = "Retos"),
				"elective-1" to AcademicPensumProgress.NodeFulfillment(subjectCode = "MC5123", subjectName = "Retos"),
				"ci2525" to AcademicPensumProgress.NodeFulfillment(subjectCode = "CI2521", subjectName = "Discretas I"),
				"eg-1" to AcademicPensumProgress.NodeFulfillment(subjectCode = "CSX316", subjectName = "La guerra"),
				"ghost" to AcademicPensumProgress.NodeFulfillment(subjectCode = "ZZ1111", subjectName = "Sin nodo")
			)
		)

		assertEquals(
			mapOf(
				"MC5123" to AcademicPensumNodeStatus.APPROVED,
				"CI2521" to AcademicPensumNodeStatus.APPROVED,
				"CSX316" to AcademicPensumNodeStatus.CURRENT
			),
			progress.fulfilledSubjectStatuses()
		)
	}

	private fun rule(
		ruleType: String = "SUBJECT_ELIGIBILITY",
		codes: List<String> = emptyList(),
		prefixes: List<String> = emptyList(),
		kind: String? = null
	): AcademicPensumGraph.FulfillmentRule {
		return AcademicPensumGraph.FulfillmentRule(
			ruleType = ruleType,
			subjectCodes = codes,
			subjectCodePrefixes = prefixes,
			minCredits = null,
			minSubjects = null,
			slotEligibilityKind = kind
		)
	}

	private fun course(
		id: String,
		code: String,
		rules: List<AcademicPensumGraph.FulfillmentRule> = emptyList()
	): AcademicPensumGraph.Node {
		return AcademicPensumGraph.Node(
			id = id,
			nodeType = AcademicPensumGraph.NodeType.COURSE,
			subjectCode = code,
			credits = 4,
			fulfillmentRules = rules
		)
	}

	private fun slot(
		id: String = "slot",
		rules: List<AcademicPensumGraph.FulfillmentRule> = emptyList(),
		category: String? = null
	): AcademicPensumGraph.Node {
		return AcademicPensumGraph.Node(
			id = id,
			nodeType = AcademicPensumGraph.NodeType.SLOT,
			subjectCode = null,
			credits = 3,
			fulfillmentRules = rules,
			category = category
		)
	}
}
