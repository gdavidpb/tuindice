package com.gdavidpb.tuindice.base.ui.keyboard

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.abs

// The keyboard rule of every surface with a text field (docs/design-system.md, «Teclado»). Compose's insets
// (imePadding) are the only thing that moves a surface above the keyboard; these modifiers make the rest of it
// behave the same way.

// Tapping the background of a form hides the keyboard. A tap that a child takes (a field, a button, a link) is not a
// tap on the background: the child keeps its focus, and the background does not clear it.
fun Modifier.dismissKeyboardOnTap(focusManager: FocusManager): Modifier {
	return pointerInput(focusManager) {
		awaitEachGesture {
			awaitFirstDown(requireUnconsumed = true)
			val up = waitForUpOrCancellation()

			if (up != null && !up.isConsumed) focusManager.clearFocus(force = true)
		}
	}
}

// Dragging a scroll container hides the keyboard, as iOS does on an interactive dismiss. Only the person's drag counts,
// and it is read from the pointer: the scrolls the framework makes on its own (bringing a field or the button into
// view) reach the scroll as drags too, so a scroll signal would take back the focus they were made for. The movement
// is read in the initial pass, before the scroll takes it, and nothing is consumed, so the scroll still happens. A
// movement that is mostly sideways (the selection of a field's text) does not hide the keyboard.
fun Modifier.dismissKeyboardOnScroll(focusManager: FocusManager): Modifier {
	return pointerInput(focusManager) {
		awaitEachGesture {
			val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
			val touchSlop = viewConfiguration.touchSlop
			var movement = Offset.Zero
			var isPressed = true

			while (isPressed && movement.getDistance() <= touchSlop) {
				val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == down.id }

				isPressed = change?.pressed == true

				if (change != null) movement += change.position - change.previousPosition
			}

			if (isPressed && abs(movement.y) > abs(movement.x)) focusManager.clearFocus(force = true)
		}
	}
}

// The region this modifier wraps holds a field and the primary action after it. When a field in it gains focus, the
// whole region is brought into view, and again whenever the keyboard's insets change while the region has focus: the
// keyboard shrinks the viewport after the focus, so one request at the focus is not enough. The region must sit inside
// the scroll container that moves.
@Composable
fun Modifier.bringRegionIntoViewOnFocus(requester: BringIntoViewRequester): Modifier {
	val hasFocus = remember { mutableStateOf(false) }
	val density = LocalDensity.current
	val ime = WindowInsets.ime

	LaunchedEffect(requester, density, ime) {
		snapshotFlow { hasFocus.value to ime.getBottom(density) }
			.distinctUntilChanged()
			.collectLatest { (focused, _) ->
				if (focused) requester.bringIntoView()
			}
	}

	return this
		.bringIntoViewRequester(requester)
		.onFocusChanged { state -> hasFocus.value = state.hasFocus }
}
