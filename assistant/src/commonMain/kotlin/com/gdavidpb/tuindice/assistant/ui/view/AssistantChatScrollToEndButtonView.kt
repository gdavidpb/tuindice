package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.style.AssistantChatMotion
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.a11y_assistant_scroll_to_end

// The round button that floats over the foot of the messages while the list is not at its end: it fades and grows in
// when the person scrolls up, takes the list down to the end of its last item when tapped, and fades and shrinks away
// once that end is in view. Whether the list can go further down is a derived state, so a scroll recomposes the button
// only when that changes. With animations off it is there or not, at once.
@Composable
fun AssistantChatScrollToEndButtonView(
	listState: LazyListState,
	modifier: Modifier = Modifier
) {
	val scope = rememberCoroutineScope()
	// The list opens at its top and then follows its last message down: the button waits until the list has reached its
	// end once, so it does not flash while the sheet opens.
	var hasReachedEnd by remember(listState) { mutableStateOf(false) }
	val isAwayFromEnd by remember(listState) { derivedStateOf { hasReachedEnd && listState.canScrollForward } }

	LaunchedEffect(listState) {
		snapshotFlow { listState.canScrollForward }.first { canScroll -> !canScroll }
		hasReachedEnd = true
	}
	val animationsEnabled = LocalTuIndiceAnimationsEnabled.current

	AnimatedVisibility(
		visible = isAwayFromEnd,
		modifier = modifier,
		enter = if (animationsEnabled) {
			fadeIn(tween(AssistantChatMotion.SmallEnterMillis)) +
				scaleIn(tween(AssistantChatMotion.SmallEnterMillis), initialScale = HIDDEN_SCALE)
		} else {
			EnterTransition.None
		},
		exit = if (animationsEnabled) {
			fadeOut(tween(AssistantChatMotion.ExitMillis)) +
				scaleOut(tween(AssistantChatMotion.ExitMillis), targetScale = HIDDEN_SCALE)
		} else {
			ExitTransition.None
		}
	) {
		Surface(
			onClick = {
				scope.launch {
					val lastIndex = listState.layoutInfo.totalItemsCount - 1
					// The offset is clamped to what the list can scroll, so this ends at the foot of the last item even
					// when that item is taller than the list.
					if (lastIndex >= 0) listState.animateScrollToItem(lastIndex, scrollOffset = Int.MAX_VALUE)
				}
			},
			modifier = Modifier.size(TuIndiceComponentSizes.IconButton).testTag(AssistantUiTags.ScrollToEndButton),
			shape = CircleShape,
			color = MaterialTheme.colorScheme.surfaceContainerHigh,
			contentColor = MaterialTheme.colorScheme.onSurface,
			border = BorderStroke(BORDER_WIDTH, MaterialTheme.colorScheme.outlineVariant),
			shadowElevation = SHADOW_ELEVATION
		) {
			Box(contentAlignment = Alignment.Center) {
				Icon(
					imageVector = Icons.Rounded.ArrowDownward,
					contentDescription = stringResource(Res.string.a11y_assistant_scroll_to_end),
					modifier = Modifier.size(TuIndiceComponentSizes.IconMedium)
				)
			}
		}
	}
}

private const val HIDDEN_SCALE = 0.6f
private val BORDER_WIDTH = 1.dp
private val SHADOW_ELEVATION = 4.dp
