package com.gdavidpb.tuindice.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene

/* Navigation 2 iOS (uikit) default: Cupertino horizontal slide, 200 ms linear,
   with the outgoing screen trailing at 0.3x parallax. Push slides toward the
   leading edge; pop slides back toward the trailing edge. */
private const val SLIDE_DURATION_MILLIS = 200
private const val PARALLAX_FACTOR = 0.3f

private fun AnimatedContentTransitionScope<Scene<NavKey>>.forwardSlide(): ContentTransform =
	slideIntoContainer(
		towards = SlideDirection.Start,
		animationSpec = tween(durationMillis = SLIDE_DURATION_MILLIS, easing = LinearEasing)
	) togetherWith slideOutOfContainer(
		towards = SlideDirection.Start,
		animationSpec = tween(durationMillis = SLIDE_DURATION_MILLIS, easing = LinearEasing),
		targetOffset = { fullOffset -> (fullOffset * PARALLAX_FACTOR).toInt() }
	)

private fun AnimatedContentTransitionScope<Scene<NavKey>>.popSlide(): ContentTransform =
	slideIntoContainer(
		towards = SlideDirection.End,
		animationSpec = tween(durationMillis = SLIDE_DURATION_MILLIS, easing = LinearEasing),
		initialOffset = { fullOffset -> (fullOffset * PARALLAX_FACTOR).toInt() }
	) togetherWith slideOutOfContainer(
		towards = SlideDirection.End,
		animationSpec = tween(durationMillis = SLIDE_DURATION_MILLIS, easing = LinearEasing)
	)

internal actual fun tuIndiceNavForwardTransitionSpec(): NavTransitionSpec =
	{ forwardSlide() }

internal actual fun tuIndiceNavPopTransitionSpec(): NavTransitionSpec =
	{ popSlide() }

internal actual fun tuIndiceNavPredictivePopTransitionSpec(): NavPredictivePopTransitionSpec =
	{ _ -> popSlide() }
