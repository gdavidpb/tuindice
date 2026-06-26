package com.gdavidpb.tuindice.wizard.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.wizard.presentation.contract.CoachmarkOverlay
import com.gdavidpb.tuindice.wizard.presentation.model.Coachmark
import com.gdavidpb.tuindice.wizard.ui.CoachmarkUiTags
import com.gdavidpb.tuindice.wizard.ui.anchor.CoachmarkAnchorRegistry
import kotlin.math.roundToInt

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CoachmarkOverlayHost(
	state: CoachmarkOverlay.State,
	anchorRegistry: CoachmarkAnchorRegistry,
	onPreviousActionClick: () -> Unit,
	onPrimaryActionClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	BoxWithConstraints(
		modifier = modifier
			.fillMaxSize()
			.testTag(CoachmarkUiTags.Host)
	) {
		val density = LocalDensity.current
		val bubbleFrame = coachmarkBubbleFrame(
			state = state,
			anchorRegistry = anchorRegistry,
			maxWidth = maxWidth,
			maxHeight = maxHeight,
			bottomSafeInsetPx = WindowInsets.safeDrawing.getBottom(density),
			density = density
		)

		CoachmarkAnimatedBubble(
			frame = bubbleFrame,
			onPreviousActionClick = onPreviousActionClick,
			onPrimaryActionClick = onPrimaryActionClick
		)
	}
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun CoachmarkAnimatedBubble(
	frame: CoachmarkBubbleFrame?,
	onPreviousActionClick: () -> Unit,
	onPrimaryActionClick: () -> Unit
) {
	val latestVisibleFrame = remember {
		mutableStateOf<CoachmarkBubbleFrame?>(null)
	}

	LaunchedEffect(frame) {
		if (frame != null) {
			latestVisibleFrame.value = frame
		}
	}

	val layoutFrame = frame ?: latestVisibleFrame.value
		?: return

	Box(
		modifier = Modifier
			.width(layoutFrame.width)
			.offset { layoutFrame.offset }
	) {
		AnimatedVisibility(
			visible = frame != null,
			enter = coachmarkBubbleEnterTransition(),
			exit = coachmarkBubbleExitTransition()
		) {
			AnimatedContent(
				targetState = frame ?: layoutFrame,
				transitionSpec = { coachmarkStepTransition() },
				label = "coachmark_step"
			) { visibleFrame ->
				CoachmarkBubble(
					coachmark = visibleFrame.coachmark,
					hasPreviousCoachmark = visibleFrame.hasPreviousCoachmark,
					hasNextCoachmark = visibleFrame.hasNextCoachmark,
					onPreviousActionClick = onPreviousActionClick,
					onPrimaryActionClick = onPrimaryActionClick,
					modifier = Modifier.fillMaxWidth()
				)
			}
		}
	}
}

private fun coachmarkBubbleFrame(
	state: CoachmarkOverlay.State,
	anchorRegistry: CoachmarkAnchorRegistry,
	maxWidth: Dp,
	maxHeight: Dp,
	bottomSafeInsetPx: Int,
	density: Density
): CoachmarkBubbleFrame? {
	val coachmark = state.activeCoachmark ?: return null
	val anchorBounds = anchorRegistry.boundsFor(coachmark.id) ?: return null
	val margin = 16.dp
	val estimatedBubbleHeight = 168.dp
	val maxWidthPx = with(density) { maxWidth.toPx() }
	val maxHeightPx = with(density) { maxHeight.toPx() }
	val marginPx = with(density) { margin.toPx() }
	val estimatedBubbleHeightPx = with(density) { estimatedBubbleHeight.toPx() }
	val bubbleWidth = minOf(320.dp, (maxWidth - margin * 2).coerceAtLeast(220.dp))
	val bubbleWidthPx = with(density) { bubbleWidth.toPx() }
	val bubbleX = (anchorBounds.center.x - bubbleWidthPx / 2)
		.coerceIn(
			minimumValue = marginPx,
			maximumValue = (maxWidthPx - bubbleWidthPx - marginPx).coerceAtLeast(marginPx)
		)
	val bubbleY = (maxHeightPx - estimatedBubbleHeightPx - bottomSafeInsetPx.toFloat() - marginPx)
		.coerceAtLeast(marginPx)

	return CoachmarkBubbleFrame(
		coachmark = coachmark,
		hasPreviousCoachmark = state.hasPreviousCoachmark,
		hasNextCoachmark = state.hasNextCoachmark,
		stepIndex = state.previousCoachmarks.size,
		width = bubbleWidth,
		offset = IntOffset(
			x = bubbleX.roundToInt(),
			y = bubbleY.roundToInt()
		)
	)
}

@OptIn(ExperimentalAnimationApi::class)
private fun AnimatedContentTransitionScope<CoachmarkBubbleFrame>
	.coachmarkStepTransition(): ContentTransform {
	val initialIndex = initialState.stepIndex
	val targetIndex = targetState.stepIndex

	val transform = when {
		initialIndex == targetIndex -> {
			fadeIn(animationSpec = tween(durationMillis = 180)) +
				scaleIn(animationSpec = tween(durationMillis = 220), initialScale = 0.96f) togetherWith
				fadeOut(animationSpec = tween(durationMillis = 140)) +
				scaleOut(animationSpec = tween(durationMillis = 160), targetScale = 0.98f)
		}

		targetIndex > initialIndex -> {
			fadeIn(animationSpec = tween(durationMillis = 180)) +
				slideInHorizontally(animationSpec = tween(durationMillis = 280)) { width -> width / 2 } togetherWith
				fadeOut(animationSpec = tween(durationMillis = 160)) +
				slideOutHorizontally(animationSpec = tween(durationMillis = 240)) { width -> -width / 3 }
		}

		else -> {
			fadeIn(animationSpec = tween(durationMillis = 180)) +
				slideInHorizontally(animationSpec = tween(durationMillis = 280)) { width -> -width / 2 } togetherWith
				fadeOut(animationSpec = tween(durationMillis = 160)) +
				slideOutHorizontally(animationSpec = tween(durationMillis = 240)) { width -> width / 3 }
		}
	}

	return transform.using(SizeTransform(clip = false))
}

private fun coachmarkBubbleEnterTransition() =
	fadeIn(animationSpec = tween(durationMillis = 200)) +
		slideInVertically(animationSpec = tween(durationMillis = 300)) { height -> height / 3 } +
		scaleIn(animationSpec = tween(durationMillis = 300), initialScale = 0.92f)

private fun coachmarkBubbleExitTransition() =
	fadeOut(animationSpec = tween(durationMillis = 160)) +
		slideOutVertically(animationSpec = tween(durationMillis = 240)) { height -> height / 3 } +
		scaleOut(animationSpec = tween(durationMillis = 240), targetScale = 0.94f)

private data class CoachmarkBubbleFrame(
	val coachmark: Coachmark,
	val hasPreviousCoachmark: Boolean,
	val hasNextCoachmark: Boolean,
	val stepIndex: Int,
	val width: Dp,
	val offset: IntOffset
)
