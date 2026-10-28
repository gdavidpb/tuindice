package com.gdavidpb.tuindice.base.ui.dialog

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.animateToWithDecay
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlin.math.abs
import kotlin.math.roundToInt

// The state of a TuIndiceModalBottomSheet: where it rests, and how a drag, a fling or a call moves it between its
// anchors. A sheet that is not hideable cannot go to Hidden by the person (a drag or a tap on the scrim settles at its
// open state), but keeps the Hidden anchor until it has opened, so it still slides in from the bottom.
@Stable
class TuIndiceSheetState internal constructor(
	val skipPartiallyExpanded: Boolean,
	val hideable: Boolean,
	initialValue: TuIndiceSheetValue,
	internal val velocityThresholdPx: Float
) {
	internal val draggableState = AnchoredDraggableState(initialValue)

	// Set by the sheet from LocalTuIndiceAnimationsEnabled: without animations every move snaps to its place.
	internal var animationsEnabled = true

	// Set while the caller asks for the sheet to hide: the caller already knows, so no dismissal is reported for it.
	private var hideRequestedByCaller = false

	// A sheet that is not hideable keeps its Hidden anchor until it has opened, so it slides in from the bottom, and
	// after that no drag can drop it to Hidden.
	private var hasOpened = false

	// True while the opening animation runs: a hide asked for then ends the opening and slides the sheet out.
	private var opening = false

	// An anchor asked for before the sheet had any anchors: its opening takes it as the target.
	private var requestedValue: TuIndiceSheetValue? = null

	// The measurements the anchors were last computed from.
	private var containerHeightPx = 0f
	private var sheetHeightPx = 0f

	val currentValue: TuIndiceSheetValue
		get() = draggableState.currentValue

	val targetValue: TuIndiceSheetValue
		get() = draggableState.targetValue

	// The anchor the sheet rests at. Unlike currentValue, the anchor closest to where the sheet is, it does not move
	// while the sheet moves or its anchors change, so a sheet that grows is not taken for one that went down.
	val settledValue: TuIndiceSheetValue
		get() = draggableState.settledValue

	val hasPartiallyExpandedState: Boolean
		get() = draggableState.anchors.hasPositionFor(TuIndiceSheetValue.PartiallyExpanded)

	val hasExpandedState: Boolean
		get() = draggableState.anchors.hasPositionFor(TuIndiceSheetValue.Expanded)

	// Before the first layout the sheet has no anchors at all.
	private val hasAnchors: Boolean
		get() = draggableState.anchors.size > 0

	internal val nestedScrollConnection: NestedScrollConnection = SheetNestedScroll()

	// Opens the sheet at the anchor asked for before its first layout, or else at its half state when it has one, or at
	// its content's height.
	suspend fun show() {
		awaitAnchors()
		hideRequestedByCaller = false

		val target = requestedValue?.takeIf { value -> draggableState.anchors.hasPositionFor(value) }
			?: if (hasPartiallyExpandedState) TuIndiceSheetValue.PartiallyExpanded else TuIndiceSheetValue.Expanded
		requestedValue = null

		opening = true
		try {
			animateTo(target)
		} finally {
			opening = false

			// However the opening ends, the sheet has opened: a sheet that is not hideable no longer needs its Hidden anchor.
			if (!hasOpened) {
				hasOpened = true
				applyAnchors()
			}
		}
	}

	// Hides the sheet because the caller asks for it: onDismissRequest is not called for it. A sheet still opening
	// has its opening ended, and slides out.
	suspend fun hide() {
		if (!hideable || (settledValue == TuIndiceSheetValue.Hidden && !opening)) return

		hideRequestedByCaller = true
		try {
			animateTo(TuIndiceSheetValue.Hidden)
		} catch (exception: CancellationException) {
			hideRequestedByCaller = false
			throw exception
		}
		if (settledValue != TuIndiceSheetValue.Hidden) hideRequestedByCaller = false
	}

	// Before the first layout there are no anchors yet, so the request waits for the opening,
	// which takes it as its target.
	suspend fun expand() {
		when {
			hasExpandedState -> animateTo(TuIndiceSheetValue.Expanded)
			!hasAnchors -> requestedValue = TuIndiceSheetValue.Expanded
		}
	}

	suspend fun partialExpand() {
		when {
			hasPartiallyExpandedState -> animateTo(TuIndiceSheetValue.PartiallyExpanded)
			!hasAnchors -> requestedValue = TuIndiceSheetValue.PartiallyExpanded
		}
	}

	// The person asks for the sheet to go: a tap on the scrim, or the back gesture.
	internal suspend fun requestDismiss() {
		if (!hideable || settledValue == TuIndiceSheetValue.Hidden) return

		hideRequestedByCaller = false
		animateTo(TuIndiceSheetValue.Hidden)
	}

	// True when the sheet rests hidden and the caller did not hide it: the person dismissed it, by a tap, a drag, a fling
	// or the back gesture alike. The caller's hide is consumed here, once it rests.
	internal fun consumeUserDismissal(): Boolean {
		val restsHidden = settledValue == TuIndiceSheetValue.Hidden
		val isUserDismissal = restsHidden && !hideRequestedByCaller

		if (restsHidden) hideRequestedByCaller = false
		return isUserDismissal
	}

	// The anchors follow the window and the sheet's measured height, limited to the window below the status bar. The
	// half anchor exists only when the content is taller than half the window, and the expanded anchor rests its bottom
	// edge on the window's bottom, or on the status bar's bottom when the sheet is that tall.
	internal fun updateAnchors(containerHeightPx: Float, sheetHeightPx: Float, topInsetPx: Float) {
		this.containerHeightPx = containerHeightPx
		this.sheetHeightPx = limitedSheetHeightPx(containerHeightPx, sheetHeightPx, topInsetPx)

		applyAnchors()
	}

	private fun applyAnchors() {
		if (containerHeightPx <= 0f || sheetHeightPx <= 0f) return

		val halfOffsetPx = containerHeightPx / 2f
		val hasPartiallyExpanded = !skipPartiallyExpanded && sheetHeightPx > halfOffsetPx
		val hasHidden = hideable || !hasOpened

		val anchors = DraggableAnchors<TuIndiceSheetValue> {
			if (hasHidden) TuIndiceSheetValue.Hidden at containerHeightPx

			if (hasPartiallyExpanded) TuIndiceSheetValue.PartiallyExpanded at halfOffsetPx

			TuIndiceSheetValue.Expanded at expandedAnchorPx(containerHeightPx, sheetHeightPx)
		}

		// The sheet keeps the anchor it moves to, not the one it last rested at. A change of height while it opens must
		// not send it back to the foot of the window, where it waits.
		val current = targetValue
		val target = if (anchors.hasPositionFor(current)) current else TuIndiceSheetValue.Expanded

		draggableState.updateAnchors(anchors, target)
	}

	// The content takes what it can of a drag first. What it leaves moves the sheet, and an upward drag opens the sheet
	// before the content scrolls: the sheet behaves as one more scrollable of the same drag, as it does on iOS.
	private inner class SheetNestedScroll : NestedScrollConnection {
		override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
			if (source != NestedScrollSource.UserInput || available.y >= 0f || !isOpenable()) return Offset.Zero

			return Offset(0f, dragBy(available.y))
		}

		override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
			if (source != NestedScrollSource.UserInput) return Offset.Zero

			return Offset(0f, dragBy(available.y))
		}

		override suspend fun onPreFling(available: Velocity): Velocity {
			if (available.y >= 0f || !isOpenable()) return Velocity.Zero

			settle(available.y)
			return available
		}

		override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
			settle(available.y)
			return available
		}

		private fun isOpenable(): Boolean =
			isPositioned && draggableState.offset > draggableState.anchors.positionOf(TuIndiceSheetValue.Expanded)
	}

	private val isPositioned: Boolean
		get() = !draggableState.offset.isNaN()

	// Settles where the sheet was let go, by a drag on it or by its content's nested scroll: with a fling it goes on in
	// the direction of the fling to the next anchor, so a quick flick down closes it, and without one, to the closest.
	internal suspend fun settle(velocity: Float) {
		if (!isPositioned) return

		val target = targetFor(draggableState.requireOffset(), velocity)

		if (animationsEnabled) {
			draggableState.animateToWithDecay(target, velocity, SheetSpring, SheetDecay)
		} else {
			draggableState.snapTo(target)
		}
	}

	private suspend fun animateTo(target: TuIndiceSheetValue) {
		if (animationsEnabled) {
			draggableState.animateTo(target, SheetSpring)
		} else {
			draggableState.snapTo(target)
		}
	}

	private companion object {
		// Mirrors Material's sheet: a spring to rest, and a fling that decays.
		val SheetSpring: AnimationSpec<Float> = spring<Float>(stiffness = Spring.StiffnessMediumLow)
		val SheetDecay: DecayAnimationSpec<Float> = exponentialDecay<Float>()
	}
}

// The top edge of the sheet in pixels from the top of the window. Before the first layout the sheet is out of sight.
internal fun TuIndiceSheetState.offsetPx(): Int {
	val offset = draggableState.offset

	return if (offset.isNaN()) UNPOSITIONED_OFFSET_PX else offset.roundToInt()
}

// Waits for the first layout: the anchors are the sheet's before it can open.
internal suspend fun TuIndiceSheetState.awaitAnchors() {
	snapshotFlow { draggableState.anchors.size }.first { size -> size > 0 }
}

// The anchor a sheet settles on, given where it is and how fast it was let go: a fling of the velocity threshold or
// more goes on to the next anchor in its direction, and any other release goes to the closest anchor.
private fun TuIndiceSheetState.targetFor(offsetPx: Float, velocityPx: Float): TuIndiceSheetValue {
	val anchors = draggableState.anchors
	val candidates = TuIndiceSheetValue.entries.filter { value ->
		anchors.hasPositionFor(value) && (hideable || value != TuIndiceSheetValue.Hidden)
	}
	val closest = candidates.minBy { value -> abs(anchors.positionOf(value) - offsetPx) }
	val below = candidates.filter { value -> anchors.positionOf(value) > offsetPx }
	val above = candidates.filter { value -> anchors.positionOf(value) < offsetPx }

	return when {
		velocityPx > velocityThresholdPx -> below.minByOrNull { value -> anchors.positionOf(value) } ?: closest
		velocityPx < -velocityThresholdPx -> above.maxByOrNull { value -> anchors.positionOf(value) } ?: closest
		else -> closest
	}
}

// Remembers the state of a TuIndiceModalBottomSheet across recompositions and configuration changes.
@Composable
fun rememberTuIndiceSheetState(
	skipPartiallyExpanded: Boolean = false,
	hideable: Boolean = true,
	initialValue: TuIndiceSheetValue = TuIndiceSheetValue.Hidden
): TuIndiceSheetState {
	val velocityThresholdPx = with(LocalDensity.current) { SheetVelocityThreshold.toPx() }
	val saver = remember(skipPartiallyExpanded, hideable, velocityThresholdPx) {
		Saver<TuIndiceSheetState, String>(
			save = { state -> state.currentValue.name },
			restore = { name ->
				TuIndiceSheetState(
					skipPartiallyExpanded = skipPartiallyExpanded,
					hideable = hideable,
					initialValue = TuIndiceSheetValue.valueOf(name),
					velocityThresholdPx = velocityThresholdPx
				)
			}
		)
	}

	return rememberSaveable(skipPartiallyExpanded, hideable, saver = saver) {
		TuIndiceSheetState(
			skipPartiallyExpanded = skipPartiallyExpanded,
			hideable = hideable,
			initialValue = initialValue,
			velocityThresholdPx = velocityThresholdPx
		)
	}
}

// A fling of this speed or more moves the sheet on in its direction, whatever its position.
private val SheetVelocityThreshold = 125.dp

private const val UNPOSITIONED_OFFSET_PX = Int.MAX_VALUE / 2

// A drag on the sheet itself (its handle, or content that does not scroll) or what its content leaves of one moves it
// as far as the anchors allow. Before the first layout there is nothing to move.
internal fun TuIndiceSheetState.dragBy(delta: Float): Float =
	if (draggableState.offset.isNaN()) 0f else draggableState.dispatchRawDelta(delta)

// The expanded anchor of a sheet: its bottom edge rests on the bottom of the window, so the padding its content takes
// from the keyboard or the navigation bar keeps the content above them, and the sheet rises with the keyboard, up to
// the status bar. The sheet's height is the limited one, so the anchor never goes above the status bar.
internal fun expandedAnchorPx(containerHeightPx: Float, sheetHeightPx: Float): Float =
	containerHeightPx - sheetHeightPx

// The height a sheet takes: the height it measures, but no more than the window leaves below its top inset, the
// status bar's, so a sheet is never drawn under the status bar. A taller sheet is laid out at that height, and its
// content scrolls within it.
internal fun limitedSheetHeightPx(containerHeightPx: Float, sheetHeightPx: Float, topInsetPx: Float): Float =
	minOf(sheetHeightPx, (containerHeightPx - topInsetPx).coerceAtLeast(0f))
