package com.gdavidpb.tuindice.pensum.data.mapper

import com.gdavidpb.tuindice.pensum.data.model.GetPensumResponse
import com.gdavidpb.tuindice.pensum.domain.model.PensumGraph
import com.gdavidpb.tuindice.pensum.domain.model.PensumModality
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeType
import com.gdavidpb.tuindice.pensum.domain.model.PensumOption
import com.gdavidpb.tuindice.pensum.domain.model.PensumRelationshipType
import com.gdavidpb.tuindice.pensum.domain.model.PensumSelection

fun GetPensumResponse.cacheKey(): String {
	return "${selection.careerCode}-${selection.year}-${selection.modalityId}"
}

fun GetPensumResponse.toSelection(): PensumSelection {
	return PensumSelection(
		pensumId = selection.pensumId,
		careerCode = selection.careerCode,
		careerName = selection.careerName,
		year = selection.year,
		modalityId = selection.modalityId,
		modalityName = selection.modalityName,
		inferred = selection.inferred
	)
}

fun GetPensumResponse.toAvailablePensums(): List<PensumOption> {
	return availablePensums.map { option ->
		PensumOption(
			id = option.id,
			careerCode = option.careerCode,
			careerName = option.careerName,
			year = option.year
		)
	}
}

fun GetPensumResponse.toAvailableModalities(): List<PensumModality> {
	return availableModalities.map { modality ->
		PensumModality(
			id = modality.id,
			name = modality.name,
			isDefault = modality.isDefault
		)
	}
}

fun GetPensumResponse.toGraph(): PensumGraph {
	return PensumGraph(
		id = pensum.id,
		careerCode = pensum.careerCode,
		careerName = pensum.careerName,
		year = pensum.year,
		modalityId = pensum.modalityId,
		modalityName = pensum.modalityName,
		totalCredits = pensum.totalCredits,
		canvas = PensumGraph.Canvas(
			width = pensum.canvas.width,
			height = pensum.canvas.height
		),
		terms = pensum.terms.map { term ->
			PensumGraph.Term(
				id = term.id,
				label = term.label,
				x = term.x,
				width = term.width
			)
		},
		nodes = pensum.nodes.map { node ->
			PensumGraph.Node(
				id = node.id,
				nodeType = node.nodeType.toNodeType(),
				displayCode = node.displayCode,
				subjectCode = node.subjectCode,
				name = node.name,
				credits = node.credits,
				category = node.category,
				termId = node.termId,
				x = node.x,
				y = node.y,
				width = node.width,
				height = node.height,
				fulfillmentRules = node.fulfillmentRules.map { rule ->
					PensumGraph.FulfillmentRule(
						id = rule.id,
						ruleType = rule.ruleType,
						subjectCodes = rule.subjectCodes,
						subjectCodePrefixes = rule.subjectCodePrefixes,
						minCredits = rule.minCredits,
						minSubjects = rule.minSubjects
					)
				}
			)
		},
		edges = pensum.edges.map { edge ->
			PensumGraph.Edge(
				id = edge.id,
				fromNodeId = edge.fromNodeId,
				toNodeId = edge.toNodeId,
				relationshipType = edge.relationshipType.toRelationshipType(),
				points = edge.points.map { point -> PensumGraph.Point(x = point.x, y = point.y) }
			)
		}
	)
}

private fun String.toNodeType(): PensumNodeType {
	return when (uppercase()) {
		"COURSE" -> PensumNodeType.COURSE
		else -> PensumNodeType.SLOT
	}
}

private fun String.toRelationshipType(): PensumRelationshipType {
	return when (uppercase()) {
		"COREQUISITE" -> PensumRelationshipType.COREQUISITE
		else -> PensumRelationshipType.REQUIREMENT
	}
}
