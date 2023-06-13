package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <T : Any> SealedCrossfade(
	targetState: T,
	modifier: Modifier = Modifier,
	animationSpec: FiniteAnimationSpec<Float> = tween(),
	label: String = "SealedCrossfade",
	content: @Composable (T) -> Unit
) {
	val transition = updateTransition(targetState, label)

	transition.Crossfade(
		modifier = modifier,
		animationSpec = animationSpec,
		content = content,
		contentKey = { newState -> newState::class }
	)
}