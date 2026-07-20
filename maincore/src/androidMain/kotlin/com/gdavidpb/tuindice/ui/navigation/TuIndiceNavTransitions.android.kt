package com.gdavidpb.tuindice.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith

/* Navigation 2 Android default: StandardDefaultNavTransitions cross-fade, same
   spec for push and pop. */
private const val FADE_DURATION_MILLIS = 700

private fun fadeContentTransform(): ContentTransform =
	fadeIn(animationSpec = tween(FADE_DURATION_MILLIS)) togetherWith
		fadeOut(animationSpec = tween(FADE_DURATION_MILLIS))

internal actual fun tuIndiceNavForwardTransitionSpec(): NavTransitionSpec =
	{ fadeContentTransform() }

internal actual fun tuIndiceNavPopTransitionSpec(): NavTransitionSpec =
	{ fadeContentTransform() }

internal actual fun tuIndiceNavPredictivePopTransitionSpec(): NavPredictivePopTransitionSpec =
	{ _ -> fadeContentTransform() }
