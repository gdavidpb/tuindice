package com.gdavidpb.tuindice.pensum.presentation.model

class PensumScreenSessionStore {
	var isSelectionSheetVisible: Boolean = false

	private val statesBySelection = mutableMapOf<String, SelectionState>()

	fun stateFor(selectionKey: String): SelectionState {
		return statesBySelection.getOrPut(selectionKey) { SelectionState() }
	}

	class SelectionState {
		var focusedNodeId: String? = null
		var detailNodeId: String? = null
		var shouldOpenDetailExpanded: Boolean = false
		var detailNavigationOriginNodeId: String? = null
		var detailNavigationDirectionName: String? = null
		var focusRequestSerial: Int = 0
		var canvasScale: Float? = null
		var canvasOffsetX: Float? = null
		var canvasOffsetY: Float? = null
		var isMinimapToggleVisible: Boolean = false
		var isMinimapVisible: Boolean = false
		var activeStatusFilterNames: Set<String> = emptySet()
	}
}
