package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.view.LottieResourceAnimationView
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import org.jetbrains.compose.resources.ExperimentalResourceApi
import tuindice.pensum.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
@Composable
fun PensumLoadingAnimationView() {
	LottieResourceAnimationView(
		readBytes = { Res.readBytes(PENSUM_LOADING_ANIMATION_PATH) },
		modifier = Modifier.size(240.dp),
		testTag = PensumUiTags.LoadingAnimation
	)
}

private const val PENSUM_LOADING_ANIMATION_PATH = "files/an_pensum_loading.json"
