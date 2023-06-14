package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TopAppBarAnimatedTitleView(title: String) {
	AnimatedContent(
		targetState = title,
		transitionSpec = {
			val enter = slideInHorizontally { x -> -x }
			val exit = slideOutHorizontally { x -> 2 * x }

			enter with exit
		}
	) { targetTitle ->
		Text(text = targetTitle)
	}
}