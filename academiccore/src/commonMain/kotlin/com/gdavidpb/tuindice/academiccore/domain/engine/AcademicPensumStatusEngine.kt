package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumGraph
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumProgress
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSnapshot
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind

class AcademicPensumStatusEngine {
	fun resolve(
		pensum: AcademicPensumGraph,
		academicSnapshot: AcademicPensumSnapshot
	): AcademicPensumProgress {
		val approvedAttempts = academicSnapshot.attempts
			.filter { attempt -> attempt.termKind != TermKind.SYNTHETIC }
			.filter { attempt -> attempt.outcome == AttemptOutcome.APPROVED }
			.sortedChronologically()
		val currentAttempts = academicSnapshot.attempts
			.filter { attempt -> attempt.termKind == TermKind.CURRENT && attempt.outcome != AttemptOutcome.APPROVED }
			.sortedChronologically()
		val approvedSubjectCodes = approvedAttempts.map { attempt -> attempt.normalizedSubjectCode() }.toSet()
		val currentSubjectCodes = currentAttempts.map { attempt -> attempt.normalizedSubjectCode() }.toSet()
		val courseNodes = pensum.nodes.filter { node -> node.nodeType == AcademicPensumGraph.NodeType.COURSE }
		val slotNodes = pensum.nodes.filter { node -> node.nodeType == AcademicPensumGraph.NodeType.SLOT }
		val fixedCourseSubjectCodes = fixedCourseSubjectCodesOf(courseNodes)

		val approvedCourseNodeIds = courseNodes
			.filter { node -> node.isApproved(approvedSubjectCodes) }
			.map(AcademicPensumGraph.Node::id)
			.toSet()
		val approvedCourseEquivalenceFulfillments =
			equivalenceFulfillmentsFor(courseNodes, approvedCourseNodeIds, approvedSubjectCodes, approvedAttempts)
		val approvedSlotFulfillments = slotNodes.assignFulfillments(
			attempts = approvedAttempts.filter { attempt -> attempt.normalizedSubjectCode() !in fixedCourseSubjectCodes }
		)
		val approvedNodeIds = approvedCourseNodeIds + approvedSlotFulfillments.keys
		val currentCourseNodeIds = courseNodes
			.filter { node -> node.id !in approvedNodeIds && node.isCurrent(currentSubjectCodes) }
			.map(AcademicPensumGraph.Node::id)
			.toSet()
		val currentCourseEquivalenceFulfillments =
			equivalenceFulfillmentsFor(courseNodes, currentCourseNodeIds, currentSubjectCodes, currentAttempts)
		val currentSlotFulfillments = slotNodes
			.filter { node -> node.id !in approvedNodeIds }
			.assignFulfillments(
				attempts = currentAttempts.filter { attempt -> attempt.normalizedSubjectCode() !in fixedCourseSubjectCodes }
			)
		val currentNodeIds = currentCourseNodeIds + currentSlotFulfillments.keys

		val statuses = statusesOf(pensum, approvedNodeIds, currentNodeIds)
		val approvedCredits = pensum.nodes
			.filter { node -> statuses[node.id] == AcademicPensumNodeStatus.APPROVED }
			.sumOf(AcademicPensumGraph.Node::credits)

		return AcademicPensumProgress(
			approvedCredits = approvedCredits,
			nodeStatuses = statuses,
			nodeFulfillments = approvedSlotFulfillments + approvedCourseEquivalenceFulfillments +
				currentSlotFulfillments + currentCourseEquivalenceFulfillments
		)
	}

	private fun AcademicPensumGraph.Node.isApproved(approvedSubjectCodes: Set<String>): Boolean {
		return when (nodeType) {
			AcademicPensumGraph.NodeType.COURSE ->
				subjectCode.normalizedSubjectCodeOrNull() in approvedSubjectCodes ||
					equivalenceCodes().any { code -> code in approvedSubjectCodes }

			AcademicPensumGraph.NodeType.SLOT -> false
		}
	}

	private fun AcademicPensumGraph.Node.isCurrent(currentSubjectCodes: Set<String>): Boolean {
		return when (nodeType) {
			AcademicPensumGraph.NodeType.COURSE ->
				subjectCode.normalizedSubjectCodeOrNull() in currentSubjectCodes ||
					equivalenceCodes().any { code -> code in currentSubjectCodes }

			AcademicPensumGraph.NodeType.SLOT -> false
		}
	}

	private fun List<AcademicPensumGraph.Node>.assignFulfillments(
		attempts: List<AcademicPensumSnapshot.Attempt>
	): Map<String, AcademicPensumProgress.NodeFulfillment> {
		val usedAttemptIds = mutableSetOf<String>()
		return mapNotNull { node ->
			val attempt = attempts.firstOrNull { attempt ->
				attempt.id !in usedAttemptIds && node.matchesAttempt(attempt)
			} ?: return@mapNotNull null
			usedAttemptIds += attempt.id
			node.id to AcademicPensumProgress.NodeFulfillment(
				subjectCode = attempt.normalizedSubjectCode(),
				subjectName = attempt.subjectName
			)
		}.toMap()
	}

	private fun AcademicPensumGraph.Node.matchesAttempt(attempt: AcademicPensumSnapshot.Attempt): Boolean {
		return fulfillmentRules.any { rule -> rule.matchesAttempt(attempt) }
	}

	private fun AcademicPensumGraph.FulfillmentRule.matchesAttempt(
		attempt: AcademicPensumSnapshot.Attempt
	): Boolean {
		if (ruleType == "GENERIC_ELECTIVE") return false
		if (minSubjects != null && minSubjects > 1) return false
		if (minCredits != null && attempt.credits < minCredits) return false

		val subjectCode = attempt.normalizedSubjectCode()
		val acceptedCodes = subjectCodes.mapNotNull { code -> code.normalizedSubjectCodeOrNull() }.toSet()
		val acceptedPrefixes = subjectCodePrefixes.mapNotNull { prefix -> prefix.normalizedSubjectCodeOrNull() }
		if (acceptedCodes.isEmpty() && acceptedPrefixes.isEmpty()) return false

		return subjectCode in acceptedCodes || acceptedPrefixes.any(subjectCode::startsWith)
	}

	private fun List<AcademicPensumSnapshot.Attempt>.sortedChronologically(): List<AcademicPensumSnapshot.Attempt> {
		return sortedWith(
			compareBy<AcademicPensumSnapshot.Attempt> { attempt -> attempt.termOrder }
				.thenBy { attempt -> attempt.positionInTerm }
				.thenBy { attempt -> attempt.normalizedSubjectCode() }
				.thenBy { attempt -> attempt.id }
		)
	}
}

private fun statusesOf(
	pensum: AcademicPensumGraph,
	approvedNodeIds: Set<String>,
	currentNodeIds: Set<String>
): Map<String, AcademicPensumNodeStatus> {
	return pensum.nodes.associate { node ->
		val status = when {
			node.id in approvedNodeIds -> AcademicPensumNodeStatus.APPROVED
			node.id in currentNodeIds -> AcademicPensumNodeStatus.CURRENT
			node.isAvailable(pensum.edges, approvedNodeIds, currentNodeIds) -> AcademicPensumNodeStatus.AVAILABLE
			else -> AcademicPensumNodeStatus.BLOCKED
		}

		node.id to status
	}
}

private fun AcademicPensumGraph.Node.isAvailable(
	edges: List<AcademicPensumGraph.Edge>,
	approvedNodeIds: Set<String>,
	currentNodeIds: Set<String>
): Boolean {
	val requirements = edges.filter { edge -> edge.toNodeId == id }
	return requirements.all { edge ->
		when (edge.relationshipType) {
			AcademicPensumGraph.RelationshipType.REQUIREMENT -> edge.fromNodeId in approvedNodeIds
			AcademicPensumGraph.RelationshipType.COREQUISITE ->
				edge.fromNodeId in approvedNodeIds || edge.fromNodeId in currentNodeIds
		}
	}
}

// Only nodes matched via their EQUIVALENCE rule (not their own code) carry a fulfillment — the
// same "cursada como" affordance a SLOT gets, and the reason downstream stats/navigation know
// which code the student actually sat under.
private fun equivalenceFulfillmentsFor(
	courseNodes: List<AcademicPensumGraph.Node>,
	matchedNodeIds: Set<String>,
	ownSubjectCodes: Set<String>,
	attempts: List<AcademicPensumSnapshot.Attempt>
): Map<String, AcademicPensumProgress.NodeFulfillment> {
	return courseNodes
		.filter { node ->
			node.id in matchedNodeIds && node.subjectCode.normalizedSubjectCodeOrNull() !in ownSubjectCodes
		}
		.courseEquivalenceFulfillments(attempts)
}

// Strictly the EQUIVALENCE rule type — a COURSE node's other rule kinds (if any) describe
// something else and must never let an unrelated attempt approve this node.
private fun AcademicPensumGraph.Node.equivalenceCodes(): Set<String> {
	return fulfillmentRules
		.filter { rule -> rule.ruleType == "EQUIVALENCE" }
		.flatMap { rule -> rule.subjectCodes }
		.mapNotNull { code -> code.normalizedSubjectCodeOrNull() }
		.toSet()
}

// Mirrors assignFulfillments' shape for SLOTs, but scoped to COURSE nodes already known to be
// approved/current via equivalence (never via their own code) — each node's equivalence codes
// are specific to that one canonical subject, so unlike slots there is no shared pool of
// attempts to race for and no need to track which attempts are already spent.
private fun List<AcademicPensumGraph.Node>.courseEquivalenceFulfillments(
	attempts: List<AcademicPensumSnapshot.Attempt>
): Map<String, AcademicPensumProgress.NodeFulfillment> {
	return mapNotNull { node ->
		val equivalenceCodes = node.equivalenceCodes()
		val attempt = attempts.firstOrNull { attempt -> attempt.normalizedSubjectCode() in equivalenceCodes }
			?: return@mapNotNull null

		node.id to AcademicPensumProgress.NodeFulfillment(
			subjectCode = attempt.normalizedSubjectCode(),
			subjectName = attempt.subjectName
		)
	}.toMap()
}

private fun fixedCourseSubjectCodesOf(courseNodes: List<AcademicPensumGraph.Node>): Set<String> {
	return courseNodes
		.flatMap { node ->
			listOfNotNull(node.subjectCode.normalizedSubjectCodeOrNull()) +
				node.fulfillmentRules.flatMap { rule ->
					rule.subjectCodes.mapNotNull { code -> code.normalizedSubjectCodeOrNull() }
				}
		}
		.toSet()
}

private fun AcademicPensumSnapshot.Attempt.normalizedSubjectCode(): String {
	return subjectCode.normalizedSubjectCodeOrNull() ?: subjectCode.trim().uppercase()
}

private fun String?.normalizedSubjectCodeOrNull(): String? {
	return this?.trim()?.uppercase()?.takeIf(String::isNotBlank)
}
