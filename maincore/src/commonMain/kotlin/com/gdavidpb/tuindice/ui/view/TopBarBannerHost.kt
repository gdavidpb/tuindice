package com.gdavidpb.tuindice.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.presentation.model.TopBarBannerBehavior
import kotlinx.coroutines.delay

@Composable
fun TopBarBannerHost(
	isContentAvailable: Boolean,
	requestKey: Int,
	behavior: TopBarBannerBehavior?,
	modifier: Modifier = Modifier,
	content: @Composable () -> Unit
) {
	val isVisible = remember { mutableStateOf(false) }

	LaunchedEffect(isContentAvailable) {
		if (!isContentAvailable) {
			isVisible.value = false
		}
	}

	LaunchedEffect(requestKey) {
		if (!isContentAvailable) return@LaunchedEffect

		when (val currentBehavior = behavior) {
			null -> Unit
			TopBarBannerBehavior.Persistent -> {
				isVisible.value = true
			}

			is TopBarBannerBehavior.AutoDismiss -> {
				isVisible.value = true
				delay(currentBehavior.millis)
				isVisible.value = false
			}
		}
	}

	AnimatedVisibility(
		modifier = modifier,
		visible = isContentAvailable && isVisible.value,
		enter = expandVertically(
			expandFrom = Alignment.Top,
			animationSpec = tween(
				durationMillis = TOP_BAR_BANNER_ANIMATION_MILLIS,
				easing = FastOutSlowInEasing
			)
		) + slideInVertically(
			animationSpec = tween(
				durationMillis = TOP_BAR_BANNER_ANIMATION_MILLIS,
				easing = FastOutSlowInEasing
			),
			initialOffsetY = { -it / 2 }
		) + fadeIn(
			animationSpec = tween(
				durationMillis = TOP_BAR_BANNER_ANIMATION_MILLIS,
				easing = FastOutSlowInEasing
			)
		),
		exit = shrinkVertically(
			shrinkTowards = Alignment.Top,
			animationSpec = tween(
				durationMillis = TOP_BAR_BANNER_ANIMATION_MILLIS,
				easing = FastOutSlowInEasing
			)
		) + slideOutVertically(
			animationSpec = tween(
				durationMillis = TOP_BAR_BANNER_ANIMATION_MILLIS,
				easing = FastOutSlowInEasing
			),
			targetOffsetY = { -it / 2 }
		) + fadeOut(
			animationSpec = tween(
				durationMillis = TOP_BAR_BANNER_ANIMATION_MILLIS,
				easing = FastOutSlowInEasing
			)
		)
	) {
		content()
	}
}

private const val TOP_BAR_BANNER_ANIMATION_MILLIS = 350
