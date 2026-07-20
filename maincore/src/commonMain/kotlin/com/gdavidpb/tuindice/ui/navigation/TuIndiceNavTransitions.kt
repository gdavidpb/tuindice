package com.gdavidpb.tuindice.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene

internal typealias NavTransitionSpec =
	AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform

internal typealias NavPredictivePopTransitionSpec =
	AnimatedContentTransitionScope<Scene<NavKey>>.(Int) -> ContentTransform

/**
 * Navigation transition specs that reproduce the Navigation 2 defaults the app
 * shipped before the Nav3 migration: a 700 ms cross-fade on Android and the
 * 200 ms Cupertino horizontal slide on iOS. Predictive-back peeks reuse the
 * platform's pop spec so the gesture matches the committed pop animation.
 */
internal expect fun tuIndiceNavForwardTransitionSpec(): NavTransitionSpec

internal expect fun tuIndiceNavPopTransitionSpec(): NavTransitionSpec

internal expect fun tuIndiceNavPredictivePopTransitionSpec(): NavPredictivePopTransitionSpec
