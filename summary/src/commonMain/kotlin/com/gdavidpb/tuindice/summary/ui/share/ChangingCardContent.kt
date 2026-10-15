package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled

// A part of the card that changes with the term or the background. With the animations on it changes by its transition;
// with them off it is drawn at once, with no transition wrapper at all, so the picture never depends on how the part
// got its content. Its size changes at once in both cases: a size that animates would still be moving when a picture is
// taken.
@Composable
internal fun <T> ChangingCardContent(
	targetState: T,
	modifier: Modifier,
	label: String,
	transitionSpec: AnimatedContentTransitionScope<T>.() -> ContentTransform,
	content: @Composable (T) -> Unit
) {
	if (LocalTuIndiceAnimationsEnabled.current) {
		AnimatedContent(
			targetState = targetState,
			modifier = modifier,
			transitionSpec = { transitionSpec().using(sizeTransform = null) },
			label = label
		) { shownState ->
			content(shownState)
		}
	} else {
		Box(modifier = modifier) {
			content(targetState)
		}
	}
}
