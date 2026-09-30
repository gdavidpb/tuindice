package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumGraph
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumProgress
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSlotKind

// The one definition of "this rule accepts this subject", shared by AcademicPensumStatusEngine and
// by every surface that labels a subject against the pensum (the planner, the subject search), so
// none of them can disagree with the pensum screen. It mirrors the backend's PensumCoverageEngine:
// rules arrive already expanded, a SUBJECT_ELIGIBILITY rule carrying the catalog codes of its kind
// and no prefixes, a GENERIC_ELECTIVE rule carrying nothing.
fun AcademicPensumGraph.FulfillmentRule.acceptsSubject(subjectCode: String, credits: Int): Boolean {
	val code = subjectCode.normalizedPensumSubjectCodeOrNull() ?: return false
	val acceptedCodes = subjectCodes.mapNotNull { accepted -> accepted.normalizedPensumSubjectCodeOrNull() }
	val acceptedPrefixes = subjectCodePrefixes.mapNotNull { prefix -> prefix.normalizedPensumSubjectCodeOrNull() }

	return ruleType != GENERIC_ELECTIVE_RULE_TYPE &&
		(minSubjects == null || minSubjects <= 1) &&
		(minCredits == null || credits >= minCredits) &&
		(code in acceptedCodes || acceptedPrefixes.any(code::startsWith))
}

fun AcademicPensumGraph.Node.acceptsSubject(subjectCode: String, credits: Int): Boolean {
	return fulfillmentRules.any { rule -> rule.acceptsSubject(subjectCode = subjectCode, credits = credits) }
}

// Codes a COURSE node owns: its own and those its rules list (equivalences). They approve that
// course and never fill a slot.
fun AcademicPensumGraph.fixedCourseSubjectCodes(): Set<String> {
	return nodes
		.filter { node -> node.nodeType == AcademicPensumGraph.NodeType.COURSE }
		.flatMap { node ->
			listOfNotNull(node.subjectCode.normalizedPensumSubjectCodeOrNull()) +
				node.fulfillmentRules.flatMap { rule ->
					rule.subjectCodes.mapNotNull { code -> code.normalizedPensumSubjectCodeOrNull() }
				}
		}
		.toSet()
}

// A slot's family: the kind its eligibility rule declares, else the category the pensum carries.
// Anything that is not Estudios Generales is an elective (area, free, professional).
fun AcademicPensumGraph.Node.slotKind(): AcademicPensumSlotKind {
	val declaredKind = fulfillmentRules.firstNotNullOfOrNull { rule ->
		when (rule.slotEligibilityKind?.trim()?.uppercase()) {
			GENERAL_SLOT_ELIGIBILITY_KIND -> AcademicPensumSlotKind.GENERAL_STUDIES
			ELECTIVE_SLOT_ELIGIBILITY_KIND -> AcademicPensumSlotKind.ELECTIVE
			else -> null
		}
	}

	return declaredKind ?: if (category?.trim()?.uppercase() == GENERAL_STUDIES_CATEGORY) {
		AcademicPensumSlotKind.GENERAL_STUDIES
	} else {
		AcademicPensumSlotKind.ELECTIVE
	}
}

// Subject codes that fulfil a node through a recorded fulfillment (a slot, or a course reached by
// equivalence), with that node's status. An approved fulfillment outranks a current one.
fun AcademicPensumProgress.fulfilledSubjectStatuses(): Map<String, AcademicPensumNodeStatus> {
	return nodeFulfillments.entries
		.mapNotNull { (nodeId, fulfillment) ->
			nodeStatuses[nodeId]?.let { status -> fulfillment.subjectCode to status }
		}
		.groupBy(keySelector = { (code, _) -> code }, valueTransform = { (_, status) -> status })
		.mapValues { (_, statuses) ->
			statuses.firstOrNull { status -> status == AcademicPensumNodeStatus.APPROVED } ?: statuses.first()
		}
}

internal fun String?.normalizedPensumSubjectCodeOrNull(): String? {
	return this?.trim()?.uppercase()?.takeIf(String::isNotBlank)
}

private const val GENERIC_ELECTIVE_RULE_TYPE = "GENERIC_ELECTIVE"
private const val GENERAL_SLOT_ELIGIBILITY_KIND = "GENERAL"
private const val ELECTIVE_SLOT_ELIGIBILITY_KIND = "ELECTIVE"
private const val GENERAL_STUDIES_CATEGORY = "GENERAL_STUDIES"
