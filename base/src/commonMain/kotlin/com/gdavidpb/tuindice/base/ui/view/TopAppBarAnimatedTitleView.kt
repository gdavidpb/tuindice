package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun TopAppBarAnimatedTitleView(title: String) {
	AnimatedContent(
		targetState = title,
		transitionSpec = {
			val enter = slideInHorizontally { x -> -x }
			val exit = slideOutHorizontally { x -> 2 * x }

			enter togetherWith exit
		}, label = "TopAppBarAnimatedTitleViewAnimatedContent"
	) { targetTitle ->
		Text(text = targetTitle)
	}
}