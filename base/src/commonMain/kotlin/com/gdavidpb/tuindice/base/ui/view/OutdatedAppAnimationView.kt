package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import org.jetbrains.compose.resources.ExperimentalResourceApi
import tuindice.base.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
@Suppress("FunctionName")
@Composable
fun OutdatedAppAnimationView() {
	LottieResourceAnimationView(
		readBytes = { Res.readBytes(OUTDATED_APP_ANIMATION_PATH) },
		modifier = Modifier.size(256.dp),
		testTag = BaseUiTags.OutdatedAppAnimation
	)
}

private const val OUTDATED_APP_ANIMATION_PATH = "files/an_outdated_app.json"
