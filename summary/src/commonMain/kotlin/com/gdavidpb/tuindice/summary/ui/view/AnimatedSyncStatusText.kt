package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

@Composable
fun AnimatedSyncStatusText(
	text: String,
	modifier: Modifier = Modifier
) {
	AnimatedContent(
		modifier = modifier,
		targetState = text,
		transitionSpec = {
			val enter = fadeIn(
				animationSpec = tween(
					durationMillis = SYNC_STATUS_TEXT_ANIMATION_DURATION_MILLIS,
					easing = FastOutSlowInEasing
				)
			) + slideInVertically(
				animationSpec = tween(
					durationMillis = SYNC_STATUS_TEXT_ANIMATION_DURATION_MILLIS,
					easing = FastOutSlowInEasing
				),
				initialOffsetY = { height -> height / 3 }
			)
			val exit = fadeOut(
				animationSpec = tween(
					durationMillis = SYNC_STATUS_TEXT_ANIMATION_DURATION_MILLIS,
					easing = FastOutSlowInEasing
				)
			) + slideOutVertically(
				animationSpec = tween(
					durationMillis = SYNC_STATUS_TEXT_ANIMATION_DURATION_MILLIS,
					easing = FastOutSlowInEasing
				),
				targetOffsetY = { height -> -height / 3 }
			)

			enter togetherWith exit
		},
		label = "SummarySyncStatusTextAnimatedContent"
	) { targetText ->
		Text(
			modifier = Modifier.testTag(SummaryUiTags.StatusText),
			text = targetText,
			style = MaterialTheme.typography.bodyMedium,
			textAlign = TextAlign.Center
		)
	}
}
