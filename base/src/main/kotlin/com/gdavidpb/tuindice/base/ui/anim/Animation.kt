package com.gdavidpb.tuindice.base.ui.anim

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.math.sin

@Composable
fun rememberAnimatableShake() = remember {
	Animatable(0f)
}

suspend fun Animatable<Float, AnimationVector1D>.animateShake() {
	animateTo(
		targetValue = 5f,
		animationSpec = tween(
			durationMillis = 500,
			easing = { x -> sin(2f * 3f * Math.PI * x).toFloat() }
		)
	)

	snapTo(0f)
}