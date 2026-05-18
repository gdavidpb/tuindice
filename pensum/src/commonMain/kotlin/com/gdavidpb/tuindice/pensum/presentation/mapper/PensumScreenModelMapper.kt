package com.gdavidpb.tuindice.pensum.presentation.mapper

import com.gdavidpb.tuindice.pensum.domain.model.ObservedPensum
import com.gdavidpb.tuindice.pensum.domain.model.PensumGraph
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeStatus
import com.gdavidpb.tuindice.pensum.domain.model.PensumRelationshipType
import com.gdavidpb.tuindice.pensum.presentation.model.PensumDisplayLayoutDefaults
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import kotlin.math.roundToInt

fun ObservedPensum.toScreenModel(): PensumScreenModel {
	val contentTopShift = pensum.nodes
		.minOfOrNull { node -> node.y }
		?.let { minY -> (minY - PensumDisplayLayoutDefaults.FirstNodeTop).coerceAtLeast(0.0) }
		.orZero()
	val displayTerms = pensum.terms.mapIndexed { index, term ->
		PensumScreenModel.Term(
			id = term.id,
			label = term.label,
			x = index * PensumDisplayLayoutDefaults.TermWidth,
			width = PensumDisplayLayoutDefaults.TermWidth
		)
	}
	val displayTermsById = displayTerms.associateBy(PensumScreenModel.Term::id)
	val displayNodes = pensum.nodes
		.map { node ->
			val term = displayTermsById[node.termId]
			val x = term?.let { displayTerm ->
				displayTerm.x + (displayTerm.width - node.width) / 2.0
			} ?: node.x
			val status = nodeStatuses[node.id] ?: PensumNodeStatus.BLOCKED

			PensumScreenModel.Node(
				id = node.id,
				displayCodes = node.displayCodes,
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
				hasSubjectStatsAction = node.subjectCode.hasSubjectStatsAction(displayCodes = node.displayCodes)
			)
		}
		.withMinimumVerticalSpacing()
	val displayCanvasWidth = maxOf(
		pensum.canvas.width,
		displayTerms.maxOfOrNull { term -> term.x + term.width }.orZero() +
			PensumDisplayLayoutDefaults.CanvasRightPadding
	)
	val displayCanvasHeight = maxOf(
		pensum.canvas.height - contentTopShift,
		displayNodes.maxOfOrNull { node -> node.y + node.height }.orZero() +
			PensumDisplayLayoutDefaults.CanvasBottomPadding
	)
	val pensumOptions = pensums
		.groupBy { graph -> graph.careerCode to graph.year }
		.values
		.map { group ->
			val representative = group.first()
			PensumScreenModel.PensumOptionItem(
				id = "${representative.careerCode}-${representative.year}",
				careerCode = representative.careerCode,
				careerName = representative.careerName,
				year = representative.year,
				modalityOptions = group.toModalityItems(selectedPensumId = selection.pensumId),
				text = "${representative.year} - ${representative.careerName}"
			)
		}
		.sortedWith(compareBy(PensumScreenModel.PensumOptionItem::year, PensumScreenModel.PensumOptionItem::careerName))
	val selectedOption = pensumOptions.firstOrNull { option ->
		option.careerCode == selection.careerCode && option.year == selection.year
	}

	return PensumScreenModel(
		selection = PensumScreenModel.Selection(
			careerCode = selection.careerCode,
			year = selection.year,
			modalityId = selection.modalityId
		),
		pensumOptions = pensumOptions,
		modalityOptions = selectedOption?.modalityOptions.orEmpty(),
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

private fun List<PensumGraph>.toModalityItems(selectedPensumId: String): List<PensumScreenModel.ModalityItem> {
	return map { graph ->
		PensumScreenModel.ModalityItem(
			id = graph.modalityId,
			name = graph.modalityName,
			isDefault = graph.id == selectedPensumId,
			text = graph.modalityName
		)
	}
		.distinctBy(PensumScreenModel.ModalityItem::id)
		.sortedWith(compareByDescending<PensumScreenModel.ModalityItem>(PensumScreenModel.ModalityItem::isDefault).thenBy(PensumScreenModel.ModalityItem::name))
}

private fun Double?.orZero(): Double = this ?: 0.0

private fun String.minimumDisplayHeight(): Double {
	return if (length > PensumDisplayLayoutDefaults.NodeSingleLineNameLimit) {
		PensumDisplayLayoutDefaults.NodeMultiLineMinHeight
	} else {
		PensumDisplayLayoutDefaults.NodeSingleLineMinHeight
	}
}

private fun List<PensumScreenModel.Node>.withMinimumVerticalSpacing(): List<PensumScreenModel.Node> {
	val spacedNodeYById = groupBy(PensumScreenModel.Node::termId)
		.values
		.flatMap { termNodes ->
			var nextAvailableY = 0.0
			termNodes
				.sortedWith(compareBy<PensumScreenModel.Node> { node -> node.y }.thenBy { node -> node.x })
				.map { node ->
					val y = maxOf(node.y, nextAvailableY)
					nextAvailableY = y + node.height + PensumDisplayLayoutDefaults.NodeVerticalGap
					node.id to y
				}
		}
		.toMap()

	return map { node ->
		node.copy(y = spacedNodeYById[node.id] ?: node.y)
	}
}

private fun String?.hasSubjectStatsAction(displayCodes: List<String>): Boolean {
	val normalizedSubjectCode = this?.trim()?.uppercase() ?: return false
	val normalizedDisplayCodes = displayCodes.map { code -> code.trim().uppercase() }

	return normalizedSubjectCode.isNotBlank() &&
		!WildcardSubjectCodeRegex.matches(normalizedSubjectCode) &&
		normalizedDisplayCodes.none(WildcardSubjectCodeRegex::matches)
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
