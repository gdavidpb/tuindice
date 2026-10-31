package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumGraph
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumProgress
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSnapshot

// Alternative groups of AcademicPensumStatusEngine: COURSE nodes that are «one of them» for a
// single requirement (see AcademicPensumGraph.Node.alternativeGroup).

// A group with a member matched by an attempt (its own code or EQUIVALENCE) extends that status
// to every other COURSE member, each carrying the earliest attempt that matched the group: the
// same "cursada como" affordance an equivalence gets. Members already matched keep their own
// resolution, and excluded ids (approved nodes, when resolving current) are never downgraded.
internal fun List<AcademicPensumGraph.Node>.alternativeFulfillments(
	matchedNodeIds: Set<String>,
	excludedNodeIds: Set<String>,
	attempts: List<AcademicPensumSnapshot.Attempt>,
	matches: AcademicPensumGraph.Node.(AcademicPensumSnapshot.Attempt) -> Boolean
): Map<String, AcademicPensumProgress.NodeFulfillment> {
	return filter { node -> node.courseAlternativeGroup() != null }
		.groupBy { node -> node.courseAlternativeGroup() }
		.values
		.flatMap { members ->
			val matchedMembers = members.filter { node -> node.id in matchedNodeIds }
			val attempt = attempts.firstOrNull { attempt ->
				matchedMembers.any { node -> node.matches(attempt) }
			} ?: return@flatMap emptyList()
			val fulfillment = AcademicPensumProgress.NodeFulfillment(
				subjectCode = attempt.subjectCode.trim().uppercase(),
				subjectName = attempt.subjectName
			)

			members
				.filter { node -> node.id !in matchedNodeIds && node.id !in excludedNodeIds }
				.map { node -> node.id to fulfillment }
		}
		.toMap()
}

// Each alternative group counts once: the credits of the member the student actually matched
// (the largest one if several were), never the sum of every member the group approved.
internal fun AcademicPensumGraph.approvedCreditsOf(
	statuses: Map<String, AcademicPensumNodeStatus>,
	directlyApprovedCourseNodeIds: Set<String>
): Int {
	val approvedNodes = nodes.filter { node -> statuses[node.id] == AcademicPensumNodeStatus.APPROVED }
	val ungroupedCredits = approvedNodes
		.filter { node -> node.courseAlternativeGroup() == null }
		.sumOf(AcademicPensumGraph.Node::credits)
	val groupedCredits = approvedNodes
		.filter { node -> node.courseAlternativeGroup() != null }
		.groupBy { node -> node.courseAlternativeGroup() }
		.values
		.sumOf { members ->
			members.filter { node -> node.id in directlyApprovedCourseNodeIds }
				.ifEmpty { members }
				.maxOf(AcademicPensumGraph.Node::credits)
		}

	return ungroupedCredits + groupedCredits
}

// Only COURSE nodes form alternative groups; a blank group is no group.
internal fun AcademicPensumGraph.Node.courseAlternativeGroup(): String? {
	return alternativeGroup
		?.trim()
		?.takeIf { group -> nodeType == AcademicPensumGraph.NodeType.COURSE && group.isNotBlank() }
}
