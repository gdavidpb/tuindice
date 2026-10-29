package com.gdavidpb.tuindice.base.ui.dialog

import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntOffset

// The speed of a drag on the sheet, read on the window. The sheet moves with the finger, so a drag read on the sheet
// itself gives the finger's speed relative to the sheet: when the finger slows down before it lets go, as fingers and
// XCUITest's swipes do, that speed turns upward, and a sheet dragged down past the middle went back up. The positions
// are read where the sheet was laid out, plus the offset it was placed at, so they are the finger's on the window. The
// same holds for a drag on the sheet's scrolling content, which moves with it too.
internal class TuIndiceSheetDragVelocity {
	private val tracker = VelocityTracker()

	// The offset the sheet was last placed at, the one its pointer positions are relative to.
	var placedOffsetPx: Int = 0

	// True once the gesture on the sheet has moved it, by a drag on it or by what its content left of one.
	var sheetMoved: Boolean = false

	// Reads every pointer of a gesture on the sheet before the sheet or its content handles it, and consumes nothing.
	suspend fun PointerInputScope.trackDrags() {
		awaitEachGesture {
			val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)

			tracker.resetTracking()
			sheetMoved = false
			add(down)

			do {
				val change = awaitPointerEvent(PointerEventPass.Initial).changes
					.firstOrNull { change -> change.id == down.id }
					?.also { change -> add(change) }
			} while (change?.pressed == true)
		}
	}

	// The vertical speed the finger let go of the sheet at, in pixels per second on the window.
	fun releaseVelocityPx(): Float = tracker.calculateVelocity().y

	private fun add(change: PointerInputChange) {
		tracker.addPosition(change.uptimeMillis, Offset(change.position.x, change.position.y + placedOffsetPx))
	}
}

// Places the sheet at the offset its state is at. It comes first in the sheet's modifiers, so everything after it,
// the caller's tags and the pane a screen reader meets included, is where the sheet is drawn.
internal fun Modifier.sheetPlacement(sheetState: TuIndiceSheetState): Modifier =
	offset {
		val offsetPx = sheetState.offsetPx()
		sheetState.dragVelocity.placedOffsetPx = offsetPx
		IntOffset(x = 0, y = offsetPx)
	}

// Lets a drag on the sheet move it. The drag is read here and settled by the sheet's own rule, the same one the nested
// scroll uses, so a quick flick down closes the sheet: the anchored-draggable default settled a short fast flick back
// where it was. It settles at the finger's speed on the window, not at the drag's on the sheet, which moves with the
// finger.
internal fun Modifier.draggableSheet(sheetState: TuIndiceSheetState, draggableState: DraggableState): Modifier =
	pointerInput(sheetState) {
		with(sheetState.dragVelocity) { trackDrags() }
	}.draggable(
		state = draggableState,
		orientation = Orientation.Vertical,
		onDragStopped = { sheetState.settle(sheetState.dragVelocity.releaseVelocityPx()) }
	)
