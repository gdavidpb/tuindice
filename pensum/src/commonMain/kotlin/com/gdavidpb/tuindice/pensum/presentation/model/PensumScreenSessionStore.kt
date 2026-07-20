package com.gdavidpb.tuindice.pensum.presentation.model

import androidx.compose.runtime.saveable.Saver

/**
 * Per-entry canvas/session state of the pensum screen, retained across tab
 * parking through the navigation saveable holder via [Companion.saver];
 * cleared on real removal (sign-out root swap) together with the entry.
 */
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

	companion object {
		fun saver(): Saver<PensumScreenSessionStore, Any> = Saver(
			save = { store -> store.toSavedList() },
			restore = { saved -> fromSavedList(saved) }
		)

		private fun PensumScreenSessionStore.toSavedList(): ArrayList<Any?> {
			val selections = HashMap<String, ArrayList<Any?>>()

			statesBySelection.forEach { (selectionKey, state) ->
				selections[selectionKey] = arrayListOf(
					state.focusedNodeId,
					state.detailNodeId,
					state.shouldOpenDetailExpanded,
					state.detailNavigationOriginNodeId,
					state.detailNavigationDirectionName,
					state.focusRequestSerial,
					state.canvasScale,
					state.canvasOffsetX,
					state.canvasOffsetY,
					state.isMinimapToggleVisible,
					state.isMinimapVisible,
					ArrayList(state.activeStatusFilterNames)
				)
			}

			return arrayListOf(isSelectionSheetVisible, selections)
		}

		@Suppress("UNCHECKED_CAST")
		private fun fromSavedList(saved: Any): PensumScreenSessionStore {
			val fields = (saved as List<Any?>).iterator()
			val store = PensumScreenSessionStore()

			store.isSelectionSheetVisible = fields.next() as Boolean

			val selections = fields.next() as Map<String, List<Any?>>
			selections.forEach { (selectionKey, savedValues) ->
				val values = savedValues.iterator()

				store.stateFor(selectionKey).apply {
					focusedNodeId = values.next() as String?
					detailNodeId = values.next() as String?
					shouldOpenDetailExpanded = values.next() as Boolean
					detailNavigationOriginNodeId = values.next() as String?
					detailNavigationDirectionName = values.next() as String?
					focusRequestSerial = values.next() as Int
					canvasScale = values.next() as Float?
					canvasOffsetX = values.next() as Float?
					canvasOffsetY = values.next() as Float?
					isMinimapToggleVisible = values.next() as Boolean
					isMinimapVisible = values.next() as Boolean
					activeStatusFilterNames = (values.next() as List<String>).toSet()
				}
			}

			return store
		}
	}
}
