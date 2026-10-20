package com.gdavidpb.tuindice.base.ui.dialog

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAnimation
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

// The bottom sheet of the app: every sheet is one, and no other code draws Material's ModalBottomSheet. It holds its
// own Dialog, which covers the whole window, so the sheet's surface reaches the bottom edge of the screen, behind the
// keyboard, and the keyboard's rounded corners show the sheet's colour. The content is padded at the bottom by the
// keyboard or the navigation bar, whichever is taller, so inputs sit right above the keyboard. The padding is part of
// the sheet's height: when the keyboard opens, the sheet rises by the keyboard's height above it, and a sheet that
// scrolls shrinks above it. Back dismisses a sheet that the person can hide; one that cannot be hidden leaves back to
// the screen, as it always did.
@Composable
fun TuIndiceModalBottomSheet(
	onDismissRequest: () -> Unit,
	modifier: Modifier = Modifier,
	sheetState: TuIndiceSheetState = rememberTuIndiceSheetState(),
	contentWindowInsets: @Composable () -> WindowInsets = {
		WindowInsets.ime.union(WindowInsets.navigationBars).only(WindowInsetsSides.Bottom)
	},
	content: @Composable ColumnScope.() -> Unit
) {
	val scope = rememberCoroutineScope()
	val measures = remember { TuIndiceSheetMeasures() }
	val topLimitPx = WindowInsets.safeDrawing.getTop(LocalDensity.current).toFloat()

	TuIndiceSheetEffects(
		sheetState = sheetState,
		onDismissRequest = onDismissRequest,
		measures = measures,
		topLimitPx = topLimitPx
	)

	Dialog(
		onDismissRequest = { scope.launch { sheetState.onBack() } },
		properties = tuIndiceSheetDialogProperties(dismissOnBackPress = sheetState.hideable)
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.onSizeChanged { size -> measures.containerHeightPx = size.height.toFloat() }
		) {
			TuIndiceSheetScrim(
				visible = sheetState.targetValue != TuIndiceSheetValue.Hidden,
				onTap = { scope.launch { sheetState.requestDismiss() } }
			)

			TuIndiceSheetSurface(
				modifier = modifier,
				sheetState = sheetState,
				contentWindowInsets = contentWindowInsets(),
				onHeightChanged = { heightPx -> measures.sheetHeightPx = heightPx },
				content = content
			)
		}
	}
}

// The sizes the anchors are computed from. Writing them lays the sheet out again, and the effects keep the anchors
// up to date with them.
private class TuIndiceSheetMeasures {
	var containerHeightPx by mutableFloatStateOf(0f)
	var sheetHeightPx by mutableFloatStateOf(0f)
}

// Keeps the sheet's state in step with the composition: it opens when it is composed, reports the dismissals the
// person makes, and keeps its anchors up to date with the window and the sheet's height.
@Composable
private fun TuIndiceSheetEffects(
	sheetState: TuIndiceSheetState,
	onDismissRequest: () -> Unit,
	measures: TuIndiceSheetMeasures,
	topLimitPx: Float
) {
	val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)
	val animationsEnabled = LocalTuIndiceAnimationsEnabled.current
	val containerHeightPx = measures.containerHeightPx
	val sheetHeightPx = measures.sheetHeightPx

	SideEffect {
		sheetState.animationsEnabled = animationsEnabled
	}

	LaunchedEffect(sheetState) {
		sheetState.show()
	}

	// A dismissal by the person reaches the caller once the sheet rests hidden, whatever moved it there. The first
	// value is the one the sheet starts from, not a change.
	LaunchedEffect(sheetState) {
		snapshotFlow { sheetState.currentValue }.drop(1).collect {
			if (sheetState.consumeUserDismissal()) currentOnDismissRequest()
		}
	}

	LaunchedEffect(sheetState, containerHeightPx, sheetHeightPx, topLimitPx) {
		if (containerHeightPx > 0f && sheetHeightPx > 0f) {
			sheetState.updateAnchors(containerHeightPx, sheetHeightPx, topLimitPx)
		}
	}
}

// The scrim of the sheet: the theme's scrim at Material's alpha, faded with the sheet. A tap on it closes the sheet.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TuIndiceSheetScrim(
	visible: Boolean,
	onTap: () -> Unit
) {
	val animationsEnabled = LocalTuIndiceAnimationsEnabled.current
	val currentOnTap by rememberUpdatedState(onTap)
	val scrimAlpha by animateFloatAsState(
		targetValue = if (visible) 1f else 0f,
		animationSpec = if (animationsEnabled) {
			tween<Float>(durationMillis = TuIndiceAnimation.StandardMillis)
		} else {
			snap()
		}
	)

	Box(
		modifier = Modifier
			.fillMaxSize()
			.alpha(scrimAlpha)
			.background(BottomSheetDefaults.ScrimColor)
			.pointerInput(Unit) {
				detectTapGestures { currentOnTap() }
			}
			.semantics {
				onClick(label = null, action = {
					currentOnTap()
					true
				})
			}
			.testTag(BaseUiTags.ModalBottomSheetScrim)
	)
}

// The sheet itself, at the anchor its state is at, with its drag handle and its content. Its surface is as tall as
// its content and padded by the window insets, and it reaches the bottom of the window.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoxScope.TuIndiceSheetSurface(
	modifier: Modifier,
	sheetState: TuIndiceSheetState,
	contentWindowInsets: WindowInsets,
	onHeightChanged: (Float) -> Unit,
	content: @Composable ColumnScope.() -> Unit
) {
	Surface(
		modifier = modifier
			.align(Alignment.TopCenter)
			.widthIn(max = BottomSheetDefaults.SheetMaxWidth)
			.fillMaxWidth()
			.onSizeChanged { size -> onHeightChanged(size.height.toFloat()) }
			.offset { IntOffset(x = 0, y = sheetState.offsetPx()) }
			// The drag is read here and settled by the sheet's own rule, the same one the nested scroll uses, so a quick
			// flick down closes the sheet: the anchored-draggable default settled a short fast flick back where it was.
			.draggable(
				state = rememberDraggableState { delta -> sheetState.dragBy(delta) },
				orientation = Orientation.Vertical,
				onDragStopped = { velocity -> sheetState.settle(velocity) }
			),
		shape = BottomSheetDefaults.ExpandedShape,
		// No tonal elevation: in this theme it tints the sheet with the accent (amber in dark), and the sheets were never
		// tinted. The colour is the sheet container's, flat.
		color = BottomSheetDefaults.ContainerColor,
		tonalElevation = 0.dp
	) {
		Column(modifier = Modifier.fillMaxWidth()) {
			Box(
				modifier = Modifier.fillMaxWidth(),
				contentAlignment = Alignment.Center
			) {
				BottomSheetDefaults.DragHandle()
			}

			Column(
				modifier = Modifier
					.fillMaxWidth()
					.nestedScroll(sheetState.nestedScrollConnection)
					.windowInsetsPadding(contentWindowInsets),
				content = content
			)
		}
	}
}

// Back closes an expanded sheet that rests halfway to that half first, as the system sheets do; otherwise it dismisses.
private suspend fun TuIndiceSheetState.onBack() {
	if (currentValue == TuIndiceSheetValue.Expanded && hasPartiallyExpandedState) {
		partialExpand()
	} else {
		requestDismiss()
	}
}
