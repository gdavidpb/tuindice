package com.gdavidpb.tuindice.pensum.data.mapper

import com.gdavidpb.tuindice.pensum.data.model.GetPensumResponse
import com.gdavidpb.tuindice.pensum.domain.model.PensumGraph
import com.gdavidpb.tuindice.pensum.domain.model.PensumModality
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeType
import com.gdavidpb.tuindice.pensum.domain.model.PensumOption
import com.gdavidpb.tuindice.pensum.domain.model.PensumRelationshipType
import com.gdavidpb.tuindice.pensum.domain.model.PensumSelection

fun GetPensumResponse.cacheKey(): String {
	val pensum = selectedPensum()
	return "${pensum.year}-${pensum.modalityId}"
}

fun GetPensumResponse.toSelection(): PensumSelection {
	val pensum = selectedPensum()
	return PensumSelection(
		pensumId = pensum.id,
		year = pensum.year,
		modalityId = pensum.modalityId,
		modalityName = pensum.modalityName,
		inferred = inferred
	)
}

fun GetPensumResponse.toAvailablePensums(): List<PensumOption> {
	return pensums
		.groupBy { pensum -> pensum.year }
		.values
		.map { group ->
			val pensum = group.first()
			PensumOption(
				id = pensum.year.toString(),
				year = pensum.year
			)
		}
		.sortedBy(PensumOption::year)
}

fun GetPensumResponse.toAvailableModalities(): List<PensumModality> {
	val selectedPensum = selectedPensum()
	return pensums
		.filter { pensum ->
			pensum.year == selectedPensum.year
		}
		.map { pensum ->
			PensumModality(
				id = pensum.modalityId,
				name = pensum.modalityName,
				isDefault = pensum.id == selectedPensum.id
			)
		}
		.distinctBy(PensumModality::id)
}

fun GetPensumResponse.toGraph(): PensumGraph {
	return selectedPensum().toGraph()
}

fun GetPensumResponse.toGraphs(): List<PensumGraph> {
	return pensums.map { pensum -> pensum.toGraph() }
}

private fun GetPensumResponse.Pensum.toGraph(): PensumGraph {
	return PensumGraph(
		id = id,
		year = year,
		modalityId = modalityId,
		modalityName = modalityName,
		totalCredits = totalCredits,
		canvas = PensumGraph.Canvas(
			width = canvas.width,
			height = canvas.height
		),
		terms = terms.map { term ->
			PensumGraph.Term(
				id = term.id,
				label = term.label,
				x = term.x,
				width = term.width
			)
		},
		nodes = nodes.map { node ->
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
						slotEligibilityKind = rule.slotEligibilityKind,
						minCredits = rule.minCredits,
						minSubjects = rule.minSubjects
					)
				}
			)
		},
		edges = edges.map { edge ->
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

private fun GetPensumResponse.selectedPensum(): GetPensumResponse.Pensum {
	return pensums.firstOrNull { pensum -> pensum.id == selectedPensumId }
		?: pensums.firstOrNull()
		?: error("Pensum response contains no pensums.")
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
