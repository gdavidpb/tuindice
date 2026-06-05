package com.gdavidpb.tuindice.pensum.presentation.model

import com.gdavidpb.tuindice.base.ui.view.DropdownMenuItem

data class PensumScreenModel(
	val careerName: String,
	val selection: Selection,
	val pensumOptions: List<PensumOptionItem>,
	val modalityOptions: List<ModalityItem>,
	val progressPercent: Int,
	val approvedCredits: Int,
	val totalCredits: Int,
	val canvas: Canvas,
	val terms: List<Term>,
	val nodes: List<Node>,
	val edges: List<Edge>
) {
	data class Selection(
		val year: Int,
		val modalityId: String
	)

	data class PensumOptionItem(
		val id: String,
		val year: Int,
		val modalityOptions: List<ModalityItem>,
		override val text: String
	) : DropdownMenuItem

	data class ModalityItem(
		val id: String,
		val name: String,
		val isDefault: Boolean,
		override val text: String
	) : DropdownMenuItem

	data class Canvas(
		val width: Double,
		val height: Double
	)

	data class Term(
		val id: String,
		val label: String,
		val x: Double,
		val width: Double
	)

	data class Node(
		val id: String,
		val displayCode: String,
		val subjectCode: String?,
		val name: String,
		val credits: Int,
		val termId: String,
		val x: Double,
		val y: Double,
		val width: Double,
		val height: Double,
		val visualStyle: NodeVisualStyle,
		val isCurrent: Boolean,
		val isApproved: Boolean,
		val isBlocked: Boolean = false,
		val hasSubjectStatsAction: Boolean,
		val subjectStatsCode: String?,
		val fulfilledSubject: FulfilledSubject?
	)

	data class FulfilledSubject(
		val code: String,
		val name: String
	)

	data class Edge(
		val id: String,
		val fromNodeId: String,
		val toNodeId: String,
		val relationshipType: RelationshipType,
		val isDisconnected: Boolean = false,
		val points: List<Point>
	)

	data class Point(
		val x: Double,
		val y: Double
	)

	data class NodeVisualStyle(
		val containerArgb: Long,
		val borderArgb: Long,
		val chipArgb: Long,
		val chipTextArgb: Long,
		val textArgb: Long,
		val secondaryTextArgb: Long
	)

	enum class RelationshipType {
		REQUIREMENT,
		COREQUISITE
	}
}
