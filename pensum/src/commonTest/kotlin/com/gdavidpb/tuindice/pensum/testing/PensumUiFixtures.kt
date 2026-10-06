package com.gdavidpb.tuindice.pensum.testing

import com.gdavidpb.tuindice.pensum.presentation.model.PensumCanvasItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumEdgeItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumEdgeRelationshipType
import com.gdavidpb.tuindice.pensum.presentation.model.PensumModalityItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusDisplay
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusIcon
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusType
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeVisualStyle
import com.gdavidpb.tuindice.pensum.presentation.model.PensumOptionItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumPointItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenSelection
import com.gdavidpb.tuindice.pensum.presentation.model.PensumSubjectDetailItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumSubjectRelationItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumTermItem

const val SAMPLE_PENSUM_CAREER_NAME = "Ingenieria de Computacion"
const val SAMPLE_PENSUM_SELECTION_KEY = "2019-degree_project"
const val SAMPLE_CURRENT_NODE_ID = "ci4325"
const val SAMPLE_APPROVED_NODE_ID = "ee1111"
const val SAMPLE_AVAILABLE_NODE_ID = "ea1"
const val SAMPLE_BLOCKED_NODE_ID = "ci9999"

fun samplePensumStatusDisplay(type: PensumNodeStatusType): PensumNodeStatusDisplay {
	return when (type) {
		PensumNodeStatusType.APPROVED -> PensumNodeStatusDisplay(
			type = type,
			icon = PensumNodeStatusIcon.CHECK,
			colorArgb = 0xFF8FE38C
		)
		PensumNodeStatusType.CURRENT -> PensumNodeStatusDisplay(
			type = type,
			icon = PensumNodeStatusIcon.CURRENT_ROUTE,
			colorArgb = 0xFFFFC400
		)
		PensumNodeStatusType.AVAILABLE -> PensumNodeStatusDisplay(
			type = type,
			icon = PensumNodeStatusIcon.ADD,
			colorArgb = 0xFF8A8F94
		)
		PensumNodeStatusType.BLOCKED -> PensumNodeStatusDisplay(
			type = type,
			icon = PensumNodeStatusIcon.LOCK,
			colorArgb = 0xFF686B70
		)
	}
}

fun samplePensumVisualStyle(type: PensumNodeStatusType): PensumNodeVisualStyle {
	val baseStyle = PensumNodeVisualStyle(
		containerArgb = 0xFF171819,
		borderArgb = 0xFF8A8F94,
		chipArgb = 0xFFEBDDA3,
		chipTextArgb = 0xFF534500,
		textArgb = 0xFFF7F7F7,
		secondaryTextArgb = 0xFF9C9EA3
	)

	return when (type) {
		PensumNodeStatusType.APPROVED -> baseStyle.copy(
			borderArgb = 0xFF8FE38C,
			chipArgb = 0xFFB8F4A8,
			chipTextArgb = 0xFF1D5B25
		)
		PensumNodeStatusType.CURRENT -> baseStyle.copy(
			borderArgb = 0xFFFFC400,
			chipArgb = 0xFFF7E6A6,
			chipTextArgb = 0xFF5A4A00
		)
		PensumNodeStatusType.AVAILABLE -> baseStyle
		PensumNodeStatusType.BLOCKED -> baseStyle.copy(
			borderArgb = 0xFF686B70,
			chipArgb = 0xFFB7B8BA,
			chipTextArgb = 0xFF383A3D
		)
	}
}

/**
 * A real subject on the first term: its code is also its stats code, so the
 * detail sheet offers the stats action. Use [withoutSubjectStats] for a slot.
 */
fun samplePensumNodeItem(
	id: String,
	code: String,
	name: String,
	type: PensumNodeStatusType = PensumNodeStatusType.AVAILABLE
): PensumNodeItem {
	val status = samplePensumStatusDisplay(type)
	val creditsText = "4 UC"

	return PensumNodeItem(
		id = id,
		displayCode = code,
		subjectCode = code,
		name = name,
		displayName = name,
		credits = 4,
		creditsText = creditsText,
		termId = "T1",
		x = 24.0,
		y = 72.0,
		width = 190.0,
		height = 144.0,
		visualStyle = samplePensumVisualStyle(type),
		status = status,
		subjectStatsCode = code,
		fulfilledSubject = null,
		detail = PensumSubjectDetailItem(
			code = code,
			name = name,
			status = status,
			termLabel = "1° trimestre",
			creditsText = creditsText,
			statsCode = code,
			fulfilledSubject = null
		)
	)
}

fun PensumNodeItem.placedAt(x: Double, y: Double, termId: String = "T1"): PensumNodeItem {
	return copy(x = x, y = y, termId = termId)
}

fun PensumNodeItem.withoutSubjectStats(): PensumNodeItem {
	return copy(
		subjectCode = null,
		subjectStatsCode = null,
		detail = detail.copy(statsCode = null)
	)
}

fun PensumNodeItem.toSampleRelationItem(
	relationshipType: PensumEdgeRelationshipType = PensumEdgeRelationshipType.REQUIREMENT
): PensumSubjectRelationItem {
	return PensumSubjectRelationItem(
		nodeId = id,
		code = displayCode,
		name = displayName,
		status = status,
		visualStyle = visualStyle,
		relationshipType = relationshipType
	)
}

fun sampleCurrentPensumNode(): PensumNodeItem {
	return samplePensumNodeItem(
		id = SAMPLE_CURRENT_NODE_ID,
		code = "CI4325",
		name = "Interfaces con el Usuario",
		type = PensumNodeStatusType.CURRENT
	)
}

fun sampleApprovedPensumNode(): PensumNodeItem {
	return samplePensumNodeItem(
		id = SAMPLE_APPROVED_NODE_ID,
		code = "EE1111",
		name = "Electiva General",
		type = PensumNodeStatusType.APPROVED
	).placedAt(x = 270.0, y = 72.0, termId = "T2")
}

/** A wildcard slot: no subject behind it, so no stats action. */
fun sampleAvailablePensumNode(): PensumNodeItem {
	return samplePensumNodeItem(
		id = SAMPLE_AVAILABLE_NODE_ID,
		code = "EA1",
		name = "Electiva de Área I"
	)
		.placedAt(x = 24.0, y = 240.0)
		.withoutSubjectStats()
}

fun sampleBlockedPensumNode(): PensumNodeItem {
	return samplePensumNodeItem(
		id = SAMPLE_BLOCKED_NODE_ID,
		code = "CI9999",
		name = "Proyecto Integrador",
		type = PensumNodeStatusType.BLOCKED
	).placedAt(x = 270.0, y = 240.0, termId = "T2")
}

fun samplePensumEdgeItem(
	fromNodeId: String,
	toNodeId: String,
	relationshipType: PensumEdgeRelationshipType = PensumEdgeRelationshipType.REQUIREMENT
): PensumEdgeItem {
	return PensumEdgeItem(
		id = "${fromNodeId}_to_$toNodeId",
		fromNodeId = fromNodeId,
		toNodeId = toNodeId,
		relationshipType = relationshipType,
		isDisconnected = false,
		points = listOf(
			PensumPointItem(x = 0.0, y = 0.0),
			PensumPointItem(x = 1.0, y = 1.0)
		)
	)
}

fun samplePensumModalityItems(): List<PensumModalityItem> {
	return listOf(
		PensumModalityItem(
			id = "degree_project",
			name = "Proyecto de Grado",
			isDefault = true,
			text = "Proyecto de Grado"
		),
		PensumModalityItem(
			id = "long_internship",
			name = "Pasantía Larga",
			isDefault = false,
			text = "Pasantía Larga"
		)
	)
}

/** Pensum 2019 / degree_project with one node per status and no edges. */
fun samplePensumScreenModel(): PensumScreenModel {
	return PensumScreenModel(
		careerName = SAMPLE_PENSUM_CAREER_NAME,
		selection = PensumScreenSelection(
			year = 2019,
			modalityId = "degree_project"
		),
		pensumOptions = listOf(
			PensumOptionItem(
				id = "2019",
				year = 2019,
				modalityOptions = emptyList(),
				text = "2019"
			)
		),
		modalityOptions = emptyList(),
		progressPercent = 0,
		approvedCredits = 0,
		totalCredits = 16,
		isCurrentFocusVisible = true,
		canvas = PensumCanvasItem(width = 520.0, height = 700.0),
		terms = listOf(
			PensumTermItem(id = "T1", label = "Primer trimestre", x = 0.0, width = 240.0),
			PensumTermItem(id = "T2", label = "Segundo trimestre", x = 240.0, width = 240.0)
		),
		nodes = listOf(
			sampleCurrentPensumNode(),
			sampleApprovedPensumNode(),
			sampleAvailablePensumNode(),
			sampleBlockedPensumNode()
		),
		edges = emptyList()
	)
}

/** 2018 offers only the degree project; 2019 (selected) also the long internship. */
fun samplePensumScreenModelWithSelectableYears(): PensumScreenModel {
	val modalities = samplePensumModalityItems()

	return samplePensumScreenModel().copy(
		pensumOptions = listOf(
			PensumOptionItem(
				id = "2018",
				year = 2018,
				modalityOptions = modalities.take(1),
				text = "2018"
			),
			PensumOptionItem(
				id = "2019",
				year = 2019,
				modalityOptions = modalities,
				text = "2019"
			)
		),
		modalityOptions = modalities
	)
}

/**
 * The current node gains one requirement (approved), one corequisite (the slot)
 * and one unlock (blocked), with the edges that back them.
 */
fun samplePensumScreenModelWithRelations(): PensumScreenModel {
	val baseModel = samplePensumScreenModel()
	val current = sampleCurrentPensumNodeWithRelations()

	return baseModel.copy(
		nodes = baseModel.nodes.map { node -> if (node.id == current.id) current else node },
		edges = listOf(
			samplePensumEdgeItem(fromNodeId = SAMPLE_APPROVED_NODE_ID, toNodeId = current.id),
			samplePensumEdgeItem(
				fromNodeId = SAMPLE_AVAILABLE_NODE_ID,
				toNodeId = current.id,
				relationshipType = PensumEdgeRelationshipType.COREQUISITE
			),
			samplePensumEdgeItem(fromNodeId = current.id, toNodeId = SAMPLE_BLOCKED_NODE_ID)
		)
	)
}

fun sampleCurrentPensumNodeWithRelations(): PensumNodeItem {
	val current = sampleCurrentPensumNode()

	return current.copy(
		detail = current.detail.copy(
			requirements = listOf(sampleApprovedPensumNode().toSampleRelationItem()),
			corequisites = listOf(
				sampleAvailablePensumNode().toSampleRelationItem(PensumEdgeRelationshipType.COREQUISITE)
			),
			unlocks = listOf(sampleBlockedPensumNode().toSampleRelationItem())
		)
	)
}
