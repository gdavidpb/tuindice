package com.gdavidpb.tuindice.pensum.ui.model

import com.gdavidpb.tuindice.pensum.presentation.model.PensumEdgeItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumEdgeRelationshipType
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusType
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel

internal data class PensumCanvasFocusState(
	val selectedRequirementEdgeIds: Set<String>,
	val selectedRequirementNodeIds: Set<String>,
	val selectedUnlockEdgeIds: Set<String>,
	val selectedAvailableUnlockEdgeIds: Set<String>,
	val selectedUnlockNodeIds: Set<String>,
	val selectedAvailableUnlockNodeIds: Set<String>,
	val selectedFocusNodeIds: Set<String>
) {
	val isActive: Boolean get() = selectedFocusNodeIds.isNotEmpty()
}

internal fun PensumScreenModel.focusStateFor(selectedNodeId: String?): PensumCanvasFocusState {
	if (selectedNodeId == null || nodes.none { node -> node.id == selectedNodeId }) {
		return emptyPensumCanvasFocusState()
	}

	val selectedRequirementEdgeIds = requirementEdgeIdsTo(selectedNodeId)
	val selectedRequirementNodeIds = requirementNodeIdsIn(
		selectedNodeId = selectedNodeId,
		selectedRequirementEdgeIds = selectedRequirementEdgeIds
	)
	val selectedUnlockEdgeIds = unlockEdgeIdsFrom(selectedNodeId)
	val selectedAvailableUnlockEdgeIds = availableUnlockEdgeIdsIn(selectedUnlockEdgeIds)
	val selectedUnlockNodeIds = unlockNodeIdsIn(
		selectedNodeId = selectedNodeId,
		selectedUnlockEdgeIds = selectedUnlockEdgeIds
	)
	val selectedAvailableUnlockNodeIds = availableUnlockNodeIdsIn(selectedAvailableUnlockEdgeIds)

	return PensumCanvasFocusState(
		selectedRequirementEdgeIds = selectedRequirementEdgeIds,
		selectedRequirementNodeIds = selectedRequirementNodeIds,
		selectedUnlockEdgeIds = selectedUnlockEdgeIds,
		selectedAvailableUnlockEdgeIds = selectedAvailableUnlockEdgeIds,
		selectedUnlockNodeIds = selectedUnlockNodeIds,
		selectedAvailableUnlockNodeIds = selectedAvailableUnlockNodeIds,
		selectedFocusNodeIds = selectedRequirementNodeIds + selectedUnlockNodeIds
	)
}

private fun emptyPensumCanvasFocusState(): PensumCanvasFocusState {
	return PensumCanvasFocusState(
		selectedRequirementEdgeIds = emptySet(),
		selectedRequirementNodeIds = emptySet(),
		selectedUnlockEdgeIds = emptySet(),
		selectedAvailableUnlockEdgeIds = emptySet(),
		selectedUnlockNodeIds = emptySet(),
		selectedAvailableUnlockNodeIds = emptySet(),
		selectedFocusNodeIds = emptySet()
	)
}

private fun PensumScreenModel.requirementEdgeIdsTo(nodeId: String?): Set<String> {
	if (nodeId == null) return emptySet()

	val incomingRequirementEdges = edges
		.filter { edge -> edge.relationshipType == PensumEdgeRelationshipType.REQUIREMENT }
		.groupBy { edge -> edge.toNodeId }
	val selectedEdgeIds = mutableSetOf<String>()
	val visitedNodeIds = mutableSetOf<String>()

	fun collectRequirements(targetNodeId: String) {
		if (!visitedNodeIds.add(targetNodeId)) return

		incomingRequirementEdges[targetNodeId].orEmpty().forEach { edge ->
			selectedEdgeIds += edge.id
			collectRequirements(edge.fromNodeId)
		}
	}

	collectRequirements(nodeId)
	return selectedEdgeIds
}

private fun PensumScreenModel.requirementNodeIdsIn(
	selectedNodeId: String?,
	selectedRequirementEdgeIds: Set<String>
): Set<String> {
	if (selectedNodeId == null) return emptySet()

	return buildSet {
		add(selectedNodeId)
		edges.forEach { edge ->
			if (edge.id in selectedRequirementEdgeIds) {
				add(edge.fromNodeId)
				add(edge.toNodeId)
			}
		}
	}
}

private fun PensumScreenModel.unlockEdgeIdsFrom(nodeId: String?): Set<String> {
	if (nodeId == null) return emptySet()

	val outgoingRequirementEdges = edges
		.filter { edge -> edge.relationshipType == PensumEdgeRelationshipType.REQUIREMENT }
		.groupBy { edge -> edge.fromNodeId }
	val selectedEdgeIds = mutableSetOf<String>()
	val visitedNodeIds = mutableSetOf<String>()

	fun collectUnlocks(sourceNodeId: String) {
		if (!visitedNodeIds.add(sourceNodeId)) return

		outgoingRequirementEdges[sourceNodeId].orEmpty().forEach { edge ->
			selectedEdgeIds += edge.id
			collectUnlocks(edge.toNodeId)
		}
	}

	collectUnlocks(nodeId)
	return selectedEdgeIds
}

private fun PensumScreenModel.unlockNodeIdsIn(
	selectedNodeId: String?,
	selectedUnlockEdgeIds: Set<String>
): Set<String> {
	if (selectedNodeId == null) return emptySet()

	return buildSet {
		add(selectedNodeId)
		edges.forEach { edge ->
			if (edge.id in selectedUnlockEdgeIds) {
				add(edge.fromNodeId)
				add(edge.toNodeId)
			}
		}
	}
}

private fun PensumScreenModel.availableUnlockEdgeIdsIn(selectedUnlockEdgeIds: Set<String>): Set<String> {
	val nodesById = nodes.associateBy(PensumNodeItem::id)
	return edges
		.filter { edge -> edge.id in selectedUnlockEdgeIds }
		.filter { edge -> nodesById[edge.toNodeId]?.isAvailableUnseen() == true }
		.map(PensumEdgeItem::id)
		.toSet()
}

private fun PensumScreenModel.availableUnlockNodeIdsIn(selectedAvailableUnlockEdgeIds: Set<String>): Set<String> {
	return edges
		.filter { edge -> edge.id in selectedAvailableUnlockEdgeIds }
		.map(PensumEdgeItem::toNodeId)
		.toSet()
}

private fun PensumNodeItem.isAvailableUnseen(): Boolean {
	return status.type == PensumNodeStatusType.AVAILABLE
}
