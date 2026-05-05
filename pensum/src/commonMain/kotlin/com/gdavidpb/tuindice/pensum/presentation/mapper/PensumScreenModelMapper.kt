package com.gdavidpb.tuindice.pensum.presentation.mapper

import com.gdavidpb.tuindice.pensum.domain.model.ObservedPensum
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeStatus
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import kotlin.math.roundToInt

private const val DisplayTermWidth = 240.0
private const val DisplayCanvasRightPadding = 32.0

fun ObservedPensum.toScreenModel(): PensumScreenModel {
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

		PensumScreenModel.Node(
			id = node.id,
			displayCode = node.displayCode,
			name = node.name,
			credits = node.credits,
			termId = node.termId,
			x = x,
			y = node.y,
			width = node.width,
			height = node.height,
			status = nodeStatuses[node.id] ?: PensumNodeStatus.BLOCKED
		)
	}
	val displayCanvasWidth = maxOf(
		pensum.canvas.width,
		displayTerms.maxOfOrNull { term -> term.x + term.width }.orZero() + DisplayCanvasRightPadding
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
			height = pensum.canvas.height
		),
		terms = displayTerms,
		nodes = displayNodes,
		edges = pensum.edges.map { edge ->
			PensumScreenModel.Edge(
				id = edge.id,
				fromNodeId = edge.fromNodeId,
				toNodeId = edge.toNodeId,
				relationshipType = edge.relationshipType,
				points = edge.points.map { point -> PensumScreenModel.Point(x = point.x, y = point.y) }
			)
		}
	)
}

private fun Double?.orZero(): Double = this ?: 0.0
