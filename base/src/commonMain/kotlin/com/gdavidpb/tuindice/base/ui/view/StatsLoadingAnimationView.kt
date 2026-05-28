package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import org.jetbrains.compose.resources.ExperimentalResourceApi
import tuindice.base.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
@Composable
fun StatsLoadingAnimationView() {
	LottieResourceAnimationView(
		readBytes = { Res.readBytes(STATS_LOADING_ANIMATION_PATH) },
		modifier = Modifier.size(220.dp),
		testTag = BaseUiTags.StatsLoadingAnimation
	)
}

private const val STATS_LOADING_ANIMATION_PATH = "files/an_stats_loading.json"
