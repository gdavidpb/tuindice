package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.BaseUiTags

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
		Text(
			modifier = Modifier.testTag(BaseUiTags.TopAppBarTitle),
			text = targetTitle,
			style = MaterialTheme.typography.titleMedium
		)
	}
}
