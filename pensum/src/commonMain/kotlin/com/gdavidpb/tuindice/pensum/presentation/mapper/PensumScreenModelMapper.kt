package com.gdavidpb.tuindice.pensum.presentation.mapper

import com.gdavidpb.tuindice.pensum.domain.model.ObservedPensum
import com.gdavidpb.tuindice.pensum.domain.model.PensumGraph
import com.gdavidpb.tuindice.pensum.domain.model.PensumModality
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeStatus
import com.gdavidpb.tuindice.pensum.domain.model.PensumRelationshipType
import com.gdavidpb.tuindice.pensum.presentation.model.PensumCanvasItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumDisplayLayoutDefaults
import com.gdavidpb.tuindice.pensum.presentation.model.PensumEdgeItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumEdgeRelationshipType
import com.gdavidpb.tuindice.pensum.presentation.model.PensumFulfilledSubjectItem
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
import com.gdavidpb.tuindice.pensum.presentation.model.PensumTermItem
import kotlin.math.roundToInt

fun ObservedPensum.toScreenModel(): PensumScreenModel {
	val contentTopShift = pensum.nodes
		.minOfOrNull { node -> node.y }
		?.let { minY -> (minY - PensumDisplayLayoutDefaults.FirstNodeTop).coerceAtLeast(0.0) }
		.orZero()
	val displayTerms = pensum.terms.mapIndexed { index, term ->
		PensumTermItem(
			id = term.id,
			label = term.label,
			x = index * PensumDisplayLayoutDefaults.TermWidth,
			width = PensumDisplayLayoutDefaults.TermWidth
		)
	}
	val displayTermsById = displayTerms.associateBy(PensumTermItem::id)
	val detailTermLabelById = displayTerms
		.mapIndexed { index, term ->
			term.id to pensumTermOrdinalLabel(
				number = index + 1,
				shouldIncludeText = true
			)
		}
		.toMap()
	val displayNodes = pensum.nodes
		.map { node ->
			val term = displayTermsById[node.termId]
			val x = term?.let { displayTerm ->
				displayTerm.x + (displayTerm.width - node.width) / 2.0
			} ?: node.x
			val status = nodeStatuses[node.id] ?: PensumNodeStatus.BLOCKED
			val fulfillment = nodeFulfillments[node.id]
			val subjectStatsCode = fulfillment?.subjectCode ?: node.subjectCode
			val displayName = fulfillment?.subjectName ?: node.name
			val creditsText = "${node.credits} UC"
			val visualStyle = status.toVisualStyle()
			val statusDisplay = status.toStatusDisplay(visualStyle = visualStyle)
			val fulfilledSubject = fulfillment?.let { fulfilled ->
				PensumFulfilledSubjectItem(
					code = fulfilled.subjectCode,
					name = fulfilled.subjectName
				)
			}
			val statsCode = subjectStatsCode.takeIf { code ->
				code.hasSubjectStatsAction(displayCode = subjectStatsCode.orEmpty())
			}
			val detailTermLabel = detailTermLabelById[node.termId]
				?: term?.label?.takeIf(String::isNotBlank)
			val minimumNodeHeight = maxOf(
				displayName.minimumDisplayHeight(),
				if (fulfillment != null) PensumDisplayLayoutDefaults.NodeMultiLineMinHeight else 0.0
			)

			PensumNodeItem(
				id = node.id,
				displayCode = node.displayCode,
				subjectCode = node.subjectCode,
				name = node.name,
				displayName = displayName,
				credits = node.credits,
				creditsText = creditsText,
				termId = node.termId,
				x = x,
				y = (node.y - contentTopShift).coerceAtLeast(0.0),
				width = node.width,
				height = maxOf(node.height, minimumNodeHeight),
				visualStyle = visualStyle,
				status = statusDisplay,
				subjectStatsCode = statsCode,
				fulfilledSubject = fulfilledSubject,
				detail = PensumSubjectDetailItem(
					code = node.displayCode,
					name = node.name,
					status = statusDisplay,
					termLabel = detailTermLabel,
					creditsText = creditsText,
					statsCode = statsCode,
					fulfilledSubject = fulfilledSubject
				)
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
	val modalityItems = availableModalities.toModalityItems()
	val pensumOptions = availablePensums
		.map { option ->
			PensumOptionItem(
				id = option.id,
				year = option.year,
				modalityOptions = modalityItems,
				text = option.year.toString()
			)
		}
		.distinctBy(PensumOptionItem::year)
		.sortedBy(PensumOptionItem::year)
	val selectedOption = pensumOptions.firstOrNull { option ->
		option.year == selection.year
	}

	return PensumScreenModel(
		careerName = careerName,
		selection = PensumScreenSelection(
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
		isCurrentFocusVisible = displayNodes.any(PensumNodeItem::isCurrent),
		canvas = PensumCanvasItem(
			width = displayCanvasWidth,
			height = displayCanvasHeight
		),
		terms = displayTerms,
		nodes = displayNodes,
		edges = pensum.edges.map { edge ->
			val targetStatus = nodeStatuses[edge.toNodeId] ?: PensumNodeStatus.BLOCKED

			PensumEdgeItem(
				id = edge.id,
				fromNodeId = edge.fromNodeId,
				toNodeId = edge.toNodeId,
				relationshipType = edge.relationshipType.toScreenRelationshipType(),
				isDisconnected = targetStatus == PensumNodeStatus.BLOCKED,
				points = edge.points.map { point ->
					PensumPointItem(
						x = point.x,
						y = (point.y - contentTopShift).coerceAtLeast(0.0)
					)
				}
			)
		}
	)
}

private fun List<PensumModality>.toModalityItems(): List<PensumModalityItem> {
	return map { modality ->
		PensumModalityItem(
			id = modality.id,
			name = modality.name,
			isDefault = modality.isDefault,
			text = modality.name
		)
	}
		.distinctBy(PensumModalityItem::id)
}

private fun Double?.orZero(): Double = this ?: 0.0

private fun String.minimumDisplayHeight(): Double {
	return if (length > PensumDisplayLayoutDefaults.NodeSingleLineNameLimit) {
		PensumDisplayLayoutDefaults.NodeMultiLineMinHeight
	} else {
		PensumDisplayLayoutDefaults.NodeSingleLineMinHeight
	}
}

private fun pensumTermOrdinalLabel(
	number: Int,
	shouldIncludeText: Boolean
): String {
	val ordinal = "$number°"
	return if (shouldIncludeText) {
		"$ordinal trimestre"
	} else {
		ordinal
	}
}

private fun List<PensumNodeItem>.withMinimumVerticalSpacing(): List<PensumNodeItem> {
	val spacedNodeYById = groupBy(PensumNodeItem::termId)
		.values
		.flatMap { termNodes ->
			var nextAvailableY = 0.0
			termNodes
				.sortedWith(compareBy<PensumNodeItem> { node -> node.y }.thenBy { node -> node.x })
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

private fun String?.hasSubjectStatsAction(displayCode: String): Boolean {
	val normalizedSubjectCode = this?.trim()?.uppercase() ?: return false
	val normalizedDisplayCode = displayCode.trim().uppercase()

	return normalizedSubjectCode.isNotBlank() &&
		!WildcardSubjectCodeRegex.matches(normalizedSubjectCode) &&
		!WildcardSubjectCodeRegex.matches(normalizedDisplayCode)
}

private val WildcardSubjectCodeRegex = Regex("^[A-Z]{2}\\d{1,2}$")

private fun PensumRelationshipType.toScreenRelationshipType(): PensumEdgeRelationshipType {
	return when (this) {
		PensumRelationshipType.REQUIREMENT -> PensumEdgeRelationshipType.REQUIREMENT
		PensumRelationshipType.COREQUISITE -> PensumEdgeRelationshipType.COREQUISITE
	}
}

private fun PensumNodeStatus.toVisualStyle(): PensumNodeVisualStyle {
	return when (this) {
		PensumNodeStatus.APPROVED -> PensumNodeVisualStyle(
			containerArgb = 0xFF171819,
			borderArgb = 0xFF8FE38C,
			chipArgb = 0xFFB8F4A8,
			chipTextArgb = 0xFF1D5B25,
			textArgb = 0xFFF7F7F7,
			secondaryTextArgb = 0xFF9C9EA3
		)
		PensumNodeStatus.CURRENT -> PensumNodeVisualStyle(
			containerArgb = 0xFF171819,
			borderArgb = 0xFFFFC400,
			chipArgb = 0xFFF7E6A6,
			chipTextArgb = 0xFF5A4A00,
			textArgb = 0xFFF7F7F7,
			secondaryTextArgb = 0xFF9C9EA3
		)
		PensumNodeStatus.AVAILABLE -> PensumNodeVisualStyle(
			containerArgb = 0xFF171819,
			borderArgb = 0xFF8A8F94,
			chipArgb = 0xFFEBDDA3,
			chipTextArgb = 0xFF534500,
			textArgb = 0xFFF7F7F7,
			secondaryTextArgb = 0xFF9C9EA3
		)
		PensumNodeStatus.BLOCKED -> PensumNodeVisualStyle(
			containerArgb = 0xFF171819,
			borderArgb = 0xFF686B70,
			chipArgb = 0xFFB7B8BA,
			chipTextArgb = 0xFF383A3D,
			textArgb = 0xFFF7F7F7,
			secondaryTextArgb = 0xFF9C9EA3
		)
	}
}

private fun PensumNodeStatus.toStatusDisplay(visualStyle: PensumNodeVisualStyle): PensumNodeStatusDisplay {
	return when (this) {
		PensumNodeStatus.APPROVED -> PensumNodeStatusDisplay(
			type = PensumNodeStatusType.APPROVED,
			icon = PensumNodeStatusIcon.CHECK,
			colorArgb = visualStyle.borderArgb
		)
		PensumNodeStatus.CURRENT -> PensumNodeStatusDisplay(
			type = PensumNodeStatusType.CURRENT,
			icon = PensumNodeStatusIcon.CURRENT_ROUTE,
			colorArgb = visualStyle.borderArgb
		)
		PensumNodeStatus.AVAILABLE -> PensumNodeStatusDisplay(
			type = PensumNodeStatusType.AVAILABLE,
			icon = PensumNodeStatusIcon.ADD,
			colorArgb = visualStyle.borderArgb
		)
		PensumNodeStatus.BLOCKED -> PensumNodeStatusDisplay(
			type = PensumNodeStatusType.BLOCKED,
			icon = PensumNodeStatusIcon.LOCK,
			colorArgb = visualStyle.borderArgb
		)
	}
}
