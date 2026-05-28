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
fun ErrorStateAnimationView() {
	LottieResourceAnimationView(
		readBytes = { Res.readBytes(ERROR_STATE_ANIMATION_PATH) },
		modifier = Modifier.size(256.dp),
		testTag = BaseUiTags.ErrorStateAnimation
	)
}

private const val ERROR_STATE_ANIMATION_PATH = "files/an_error.json"
