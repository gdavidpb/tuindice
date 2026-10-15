package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAnimation
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArtwork

// The figures of a term slide up by a part of their own height, as they fade in.
private const val FIGURES_SLIDE_DIVISOR = 8

// The new artwork fades in over the one before it, which stays under it until the fade is done, so the card never shows
// the bone between two artworks. It changes at once when the card had no artwork yet.
internal fun AnimatedContentTransitionScope<ShareTermArtwork?>.artTransform(): ContentTransform {
	if (initialState == null) {
		return EnterTransition.None togetherWith ExitTransition.None
	}

	val enter = fadeIn(animationSpec = tween(durationMillis = TuIndiceAnimation.StandardMillis))

	return (enter togetherWith ExitTransition.None).apply { targetContentZIndex = 1f }
}

// The credit is text, so it dissolves into the new one; it changes at once when the card had no artwork yet.
internal fun AnimatedContentTransitionScope<ShareTermArtwork?>.creditTransform(): ContentTransform {
	if (initialState == null) {
		return EnterTransition.None togetherWith ExitTransition.None
	}

	return dissolveTransform()
}

// The figures of a term slide up as they fade in, and the old ones fade out in place, all in the short transition.
internal fun figuresTransform(): ContentTransform {
	val enter = fadeIn(animationSpec = tween(durationMillis = TuIndiceAnimation.ShortMillis)) +
		slideInVertically(animationSpec = tween(durationMillis = TuIndiceAnimation.ShortMillis)) { fullHeight ->
			fullHeight / FIGURES_SLIDE_DIVISOR
		}
	val exit = fadeOut(animationSpec = tween(durationMillis = TuIndiceAnimation.ShortMillis))

	return enter togetherWith exit
}

// Text dissolves into the next text, in the short transition.
internal fun dissolveTransform(): ContentTransform {
	return fadeIn(animationSpec = tween(durationMillis = TuIndiceAnimation.ShortMillis)) togetherWith
		fadeOut(animationSpec = tween(durationMillis = TuIndiceAnimation.ShortMillis))
}
