package com.gdavidpb.tuindice.pensum.presentation.mapper

import com.gdavidpb.tuindice.pensum.domain.model.ObservedPensum
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeStatus
import com.gdavidpb.tuindice.pensum.domain.model.PensumRelationshipType
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import kotlin.math.roundToInt

private const val DisplayTermWidth = 240.0
private const val DisplayCanvasRightPadding = 32.0
private const val DisplayCanvasBottomPadding = 48.0
private const val DisplayFirstNodeTop = 84.0
private const val DisplayNodeSingleLineMinHeight = 120.0
private const val DisplayNodeMultiLineMinHeight = 144.0
private const val DisplayNodeSingleLineNameLimit = 18

fun ObservedPensum.toScreenModel(): PensumScreenModel {
	val contentTopShift = pensum.nodes
		.minOfOrNull { node -> node.y }
		?.let { minY -> (minY - DisplayFirstNodeTop).coerceAtLeast(0.0) }
		.orZero()
	val displayTerms = pensum.terms.mapIndexed { index, term ->
		PensumScreenModel.Term(
			id = term.id,
			label = term.label,
			x = index * DisplayTermWidth,
			width = DisplayTermWidth
		)
	}
	val displayTermsById = displayTerms.associateBy(PensumScreenModel.Term::id)
	val displayNodes = pensum.nodes.map { node ->
		val term = displayTermsById[node.termId]
		val x = term?.let { displayTerm ->
			displayTerm.x + (displayTerm.width - node.width) / 2.0
		} ?: node.x
		val status = nodeStatuses[node.id] ?: PensumNodeStatus.BLOCKED

		PensumScreenModel.Node(
			id = node.id,
			displayCode = node.displayCode,
			subjectCode = node.subjectCode,
			name = node.name,
			credits = node.credits,
			termId = node.termId,
			x = x,
			y = (node.y - contentTopShift).coerceAtLeast(0.0),
			width = node.width,
			height = maxOf(node.height, node.name.minimumDisplayHeight()),
			visualStyle = status.toVisualStyle(),
			isCurrent = status == PensumNodeStatus.CURRENT,
			isApproved = status == PensumNodeStatus.APPROVED,
			hasSubjectStatsAction = node.subjectCode.hasSubjectStatsAction(displayCode = node.displayCode)
		)
	}
	val displayCanvasWidth = maxOf(
		pensum.canvas.width,
		displayTerms.maxOfOrNull { term -> term.x + term.width }.orZero() + DisplayCanvasRightPadding
	)
	val displayCanvasHeight = maxOf(
		pensum.canvas.height - contentTopShift,
		displayNodes.maxOfOrNull { node -> node.y + node.height }.orZero() + DisplayCanvasBottomPadding
	)

	return PensumScreenModel(
		selection = PensumScreenModel.Selection(
			careerCode = selection.careerCode,
			year = selection.year,
			modalityId = selection.modalityId
		),
		pensumOptions = availablePensums.map { option ->
			PensumScreenModel.PensumOptionItem(
				id = option.id,
				careerCode = option.careerCode,
				careerName = option.careerName,
				year = option.year,
				text = "${option.year} - ${option.careerName}"
			)
		},
		modalityOptions = availableModalities.map { modality ->
			PensumScreenModel.ModalityItem(
				id = modality.id,
				name = modality.name,
				isDefault = modality.isDefault,
				text = modality.name
			)
		},
		progressPercent = if (pensum.totalCredits == 0)
			0
		else
			((approvedCredits.toDouble() / pensum.totalCredits.toDouble()) * 100).roundToInt().coerceIn(0, 100),
		approvedCredits = approvedCredits,
		totalCredits = pensum.totalCredits,
		canvas = PensumScreenModel.Canvas(
			width = displayCanvasWidth,
			height = displayCanvasHeight
		),
		terms = displayTerms,
		nodes = displayNodes,
		edges = pensum.edges.map { edge ->
			PensumScreenModel.Edge(
				id = edge.id,
				fromNodeId = edge.fromNodeId,
				toNodeId = edge.toNodeId,
				relationshipType = edge.relationshipType.toScreenRelationshipType(),
				points = edge.points.map { point ->
					PensumScreenModel.Point(
						x = point.x,
						y = (point.y - contentTopShift).coerceAtLeast(0.0)
					)
				}
			)
		}
	)
}

private fun Double?.orZero(): Double = this ?: 0.0

private fun String.minimumDisplayHeight(): Double {
	return if (length > DisplayNodeSingleLineNameLimit) {
		DisplayNodeMultiLineMinHeight
	} else {
		DisplayNodeSingleLineMinHeight
	}
}

private fun String?.hasSubjectStatsAction(displayCode: String): Boolean {
	val normalizedSubjectCode = this?.trim()?.uppercase() ?: return false
	val normalizedDisplayCode = displayCode.trim().uppercase()

	return normalizedSubjectCode.isNotBlank() &&
		!WildcardSubjectCodeRegex.matches(normalizedSubjectCode) &&
		!WildcardSubjectCodeRegex.matches(normalizedDisplayCode)
}

private val WildcardSubjectCodeRegex = Regex("^[A-Z]{2}\\d{1,2}$")

private fun PensumRelationshipType.toScreenRelationshipType(): PensumScreenModel.RelationshipType {
	return when (this) {
		PensumRelationshipType.REQUIREMENT -> PensumScreenModel.RelationshipType.REQUIREMENT
		PensumRelationshipType.COREQUISITE -> PensumScreenModel.RelationshipType.COREQUISITE
	}
}

private fun PensumNodeStatus.toVisualStyle(): PensumScreenModel.NodeVisualStyle {
	return when (this) {
		PensumNodeStatus.APPROVED -> PensumScreenModel.NodeVisualStyle(
			containerArgb = 0xFF171819,
			borderArgb = 0xFF8FE38C,
			chipArgb = 0xFFB8F4A8,
			chipTextArgb = 0xFF1D5B25,
			textArgb = 0xFFF7F7F7,
			secondaryTextArgb = 0xFF9C9EA3
		)
		PensumNodeStatus.CURRENT -> PensumScreenModel.NodeVisualStyle(
			containerArgb = 0xFF171819,
			borderArgb = 0xFFFFC400,
			chipArgb = 0xFFF7E6A6,
			chipTextArgb = 0xFF5A4A00,
			textArgb = 0xFFF7F7F7,
			secondaryTextArgb = 0xFF9C9EA3
		)
		PensumNodeStatus.AVAILABLE -> PensumScreenModel.NodeVisualStyle(
			containerArgb = 0xFF171819,
			borderArgb = 0xFF8A8F94,
			chipArgb = 0xFFEBDDA3,
			chipTextArgb = 0xFF534500,
			textArgb = 0xFFF7F7F7,
			secondaryTextArgb = 0xFF9C9EA3
		)
		PensumNodeStatus.BLOCKED -> PensumScreenModel.NodeVisualStyle(
			containerArgb = 0xFF242628,
			borderArgb = 0xFF686B70,
			chipArgb = 0xFFB7B8BA,
			chipTextArgb = 0xFF383A3D,
			textArgb = 0xFFC7C8CA,
			secondaryTextArgb = 0xFF8A8C90
		)
	}
}
