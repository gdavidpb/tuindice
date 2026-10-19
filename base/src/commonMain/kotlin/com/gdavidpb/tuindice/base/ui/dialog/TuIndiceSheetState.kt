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
import kotlinx.coroutines.flow.first
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

// The state of a TuIndiceModalBottomSheet: where it rests, and how a drag, a fling or a call moves it between its
// anchors. A sheet that is not hideable cannot go to Hidden by the person (a drag or a tap on the scrim settles at its
// open state), but keeps the Hidden anchor, so it still slides in from the bottom when it opens.
@Stable
class TuIndiceSheetState internal constructor(
	val skipPartiallyExpanded: Boolean,
	val hideable: Boolean,
	initialValue: TuIndiceSheetValue,
	private val velocityThresholdPx: Float
) {
	internal val draggableState = AnchoredDraggableState(initialValue)

	// Set by the sheet from LocalTuIndiceAnimationsEnabled: without animations every move snaps to its place.
	internal var animationsEnabled = true

	// Set when the person asks for the sheet to go (a drag, a tap on the scrim, the back gesture). The sheet calls
	// onDismissRequest once it rests hidden. A hide that the caller asks for does not set it: the caller already knows.
	private var dismissPending = false

	val currentValue: TuIndiceSheetValue
		get() = draggableState.currentValue

	val targetValue: TuIndiceSheetValue
		get() = draggableState.targetValue

	val hasPartiallyExpandedState: Boolean
		get() = draggableState.anchors.hasPositionFor(TuIndiceSheetValue.PartiallyExpanded)

	val hasExpandedState: Boolean
		get() = draggableState.anchors.hasPositionFor(TuIndiceSheetValue.Expanded)

	// Opens the sheet at its half state when it has one, or at its content's height.
	suspend fun show() {
		awaitAnchors()
		dismissPending = false
		animateTo(if (hasPartiallyExpandedState) TuIndiceSheetValue.PartiallyExpanded else TuIndiceSheetValue.Expanded)
	}

	suspend fun hide() {
		dismissPending = false
		if (hideable) animateTo(TuIndiceSheetValue.Hidden)
	}

	suspend fun expand() {
		dismissPending = false
		if (hasExpandedState) animateTo(TuIndiceSheetValue.Expanded)
	}

	suspend fun partialExpand() {
		dismissPending = false
		if (hasPartiallyExpandedState) animateTo(TuIndiceSheetValue.PartiallyExpanded)
	}

	internal suspend fun requestDismiss() {
		if (!hideable || currentValue == TuIndiceSheetValue.Hidden) return

		dismissPending = true
		animateTo(TuIndiceSheetValue.Hidden)
	}

	// True once, when the sheet rests hidden after the person asked for it to go.
	internal fun consumeDismissal(): Boolean {
		if (!dismissPending || currentValue != TuIndiceSheetValue.Hidden) return false

		dismissPending = false
		return true
	}

	// The anchors follow the window and the sheet's measured height. The half anchor exists only when the content is
	// taller than half the window; the expanded anchor never goes above the top inset.
	internal fun updateAnchors(containerHeightPx: Float, sheetHeightPx: Float, topLimitPx: Float) {
		val halfOffsetPx = containerHeightPx / 2f
		val hasPartiallyExpanded = !skipPartiallyExpanded && sheetHeightPx > halfOffsetPx

		val anchors = DraggableAnchors<TuIndiceSheetValue> {
			TuIndiceSheetValue.Hidden at containerHeightPx

			if (hasPartiallyExpanded) TuIndiceSheetValue.PartiallyExpanded at halfOffsetPx

			TuIndiceSheetValue.Expanded at max(topLimitPx, containerHeightPx - sheetHeightPx)
		}

		val current = currentValue
		val target = if (anchors.hasPositionFor(current)) current else TuIndiceSheetValue.Expanded

		draggableState.updateAnchors(anchors, target)
	}

	// The top edge of the sheet in pixels from the top of the window. Before the first layout the sheet is out of sight.
	internal fun offsetPx(): Int {
		val offset = draggableState.offset

		return if (offset.isNaN()) UnpositionedOffsetPx else offset.roundToInt()
	}

	// The content takes what it can of a drag first. What it leaves moves the sheet, and an upward drag opens the sheet
	// before the content scrolls: the sheet behaves as one more scrollable of the same drag, as it does on iOS.
	internal val nestedScrollConnection: NestedScrollConnection = object : NestedScrollConnection {
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
	}

	private val isPositioned: Boolean
		get() = !draggableState.offset.isNaN()

	private fun isOpenable(): Boolean =
		isPositioned && draggableState.offset > draggableState.anchors.positionOf(TuIndiceSheetValue.Expanded)

	private fun dragBy(delta: Float): Float =
		if (isPositioned) draggableState.dispatchRawDelta(delta) else 0f

	// Settles where the sheet was let go: with a fling it goes on in the direction of the fling to the next anchor, and
	// without one, to the closest. A fling towards the bottom that ends at Hidden is a dismissal.
	private suspend fun settle(velocity: Float) {
		if (!isPositioned) return

		val target = targetFor(draggableState.requireOffset(), velocity)
		dismissPending = target == TuIndiceSheetValue.Hidden

		if (animationsEnabled) {
			draggableState.animateToWithDecay(target, velocity, SheetSpring, SheetDecay)
		} else {
			draggableState.snapTo(target)
		}
	}

	private fun targetFor(offsetPx: Float, velocityPx: Float): TuIndiceSheetValue {
		val anchors = draggableState.anchors
		val candidates = TuIndiceSheetValue.entries.filter { value ->
			anchors.hasPositionFor(value) && (hideable || value != TuIndiceSheetValue.Hidden)
		}
		val closest = candidates.minBy { value -> abs(anchors.positionOf(value) - offsetPx) }

		return when {
			velocityPx > velocityThresholdPx -> candidates
				.filter { value -> anchors.positionOf(value) > offsetPx }
				.minByOrNull { value -> anchors.positionOf(value) } ?: closest

			velocityPx < -velocityThresholdPx -> candidates
				.filter { value -> anchors.positionOf(value) < offsetPx }
				.maxByOrNull { value -> anchors.positionOf(value) } ?: closest

			else -> closest
		}
	}

	private suspend fun animateTo(target: TuIndiceSheetValue) {
		if (animationsEnabled) {
			draggableState.animateTo(target, SheetSpring)
		} else {
			draggableState.snapTo(target)
		}
	}

	private suspend fun awaitAnchors() {
		snapshotFlow { draggableState.anchors.size }.first { size -> size > 0 }
	}

	private companion object {
		// Mirrors Material's sheet: a spring to rest, a fling that decays, and a fling of 125 dp/s or more counts as one.
		val SheetSpring: AnimationSpec<Float> = spring<Float>(stiffness = Spring.StiffnessMediumLow)
		val SheetDecay: DecayAnimationSpec<Float> = exponentialDecay<Float>()

		const val UnpositionedOffsetPx = Int.MAX_VALUE / 2
	}
}

// Remembers the state of a TuIndiceModalBottomSheet across recompositions and configuration changes.
@Composable
fun rememberTuIndiceSheetState(
	skipPartiallyExpanded: Boolean = false,
	hideable: Boolean = true,
	initialValue: TuIndiceSheetValue = TuIndiceSheetValue.Hidden
): TuIndiceSheetState {
	val velocityThresholdPx = with(LocalDensity.current) { SheetVelocityThresholdDp.toPx() }
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

// A fling of this speed or more counts as a fling in its own direction, whatever the position of the sheet.
private val SheetVelocityThresholdDp = 125.dp
