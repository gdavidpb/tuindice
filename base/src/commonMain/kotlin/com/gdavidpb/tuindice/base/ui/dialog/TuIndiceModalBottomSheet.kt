package com.gdavidpb.tuindice.base.ui.dialog

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.window.Dialog
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAnimation
import kotlinx.coroutines.launch

// The bottom sheet of the app: every sheet is one, and no other code draws Material's ModalBottomSheet. It holds its
// own Dialog, which covers the whole window, so the sheet's surface reaches the bottom edge of the screen, behind the
// keyboard, and the keyboard's rounded corners show the sheet's colour. The content is padded at the bottom by the
// keyboard or the navigation bar, whichever is taller, so inputs sit right above the keyboard. The padding is part of
// the sheet's height: when the keyboard opens, the sheet rises by the keyboard's height above it, and a sheet that
// scrolls shrinks above it.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuIndiceModalBottomSheet(
	onDismissRequest: () -> Unit,
	modifier: Modifier = Modifier,
	sheetState: TuIndiceSheetState = rememberTuIndiceSheetState(),
	dismissOnBackPress: Boolean = true,
	dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
	contentWindowInsets: @Composable () -> WindowInsets = {
		WindowInsets.ime.union(WindowInsets.navigationBars).only(WindowInsetsSides.Bottom)
	},
	content: @Composable ColumnScope.() -> Unit
) {
	val scope = rememberCoroutineScope()
	val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)
	val animationsEnabled = LocalTuIndiceAnimationsEnabled.current
	val density = LocalDensity.current
	val topLimitPx = WindowInsets.safeDrawing.getTop(density).toFloat()
	var containerHeightPx by remember { mutableFloatStateOf(0f) }
	var sheetHeightPx by remember { mutableFloatStateOf(0f) }

	val scrimAlpha by animateFloatAsState(
		targetValue = if (sheetState.targetValue == TuIndiceSheetValue.Hidden) 0f else 1f,
		animationSpec = if (animationsEnabled) {
			tween<Float>(durationMillis = TuIndiceAnimation.StandardMillis)
		} else {
			snap()
		}
	)

	SideEffect {
		sheetState.animationsEnabled = animationsEnabled
	}

	LaunchedEffect(sheetState) {
		sheetState.show()
	}

	// Dismissals the person asked for reach the caller once the sheet rests hidden; a hide the caller asked for does not.
	LaunchedEffect(sheetState) {
		snapshotFlow { sheetState.currentValue }.collect {
			if (sheetState.consumeDismissal()) currentOnDismissRequest()
		}
	}

	LaunchedEffect(sheetState, containerHeightPx, sheetHeightPx, topLimitPx) {
		if (containerHeightPx > 0f && sheetHeightPx > 0f) {
			sheetState.updateAnchors(containerHeightPx, sheetHeightPx, topLimitPx)
		}
	}

	Dialog(
		onDismissRequest = {
			scope.launch {
				// Back on an expanded sheet that rests halfway closes to that half first, as the system sheets do.
				if (sheetState.currentValue == TuIndiceSheetValue.Expanded && sheetState.hasPartiallyExpandedState) {
					sheetState.partialExpand()
				} else {
					sheetState.requestDismiss()
				}
			}
		},
		properties = tuIndiceSheetDialogProperties(dismissOnBackPress = dismissOnBackPress)
	) {
		val contentInsets = contentWindowInsets()

		Box(
			modifier = Modifier
				.fillMaxSize()
				.onSizeChanged { size -> containerHeightPx = size.height.toFloat() }
		) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.alpha(scrimAlpha)
					.background(BottomSheetDefaults.ScrimColor)
					.pointerInput(sheetState) {
						detectTapGestures {
							scope.launch { sheetState.requestDismiss() }
						}
					}
					.semantics {
						onClick(label = null, action = {
							scope.launch { sheetState.requestDismiss() }
							true
						})
					}
					.testTag(BaseUiTags.ModalBottomSheetScrim)
			)

			Surface(
				modifier = modifier
					.align(Alignment.TopCenter)
					.widthIn(max = BottomSheetDefaults.SheetMaxWidth)
					.fillMaxWidth()
					.onSizeChanged { size -> sheetHeightPx = size.height.toFloat() }
					.offset { IntOffset(x = 0, y = sheetState.offsetPx()) }
					.anchoredDraggable(
						state = sheetState.draggableState,
						orientation = Orientation.Vertical
					),
				shape = BottomSheetDefaults.ExpandedShape,
				color = BottomSheetDefaults.ContainerColor,
				tonalElevation = BottomSheetDefaults.Elevation
			) {
				Column(modifier = Modifier.fillMaxWidth()) {
					dragHandle?.let { handle ->
						Box(
							modifier = Modifier.fillMaxWidth(),
							contentAlignment = Alignment.Center
						) {
							handle()
						}
					}

					Column(
						modifier = Modifier
							.fillMaxWidth()
							.nestedScroll(sheetState.nestedScrollConnection)
							.windowInsetsPadding(contentInsets),
						content = content
					)
				}
			}
		}
	}
}
