package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.jetbrains.compose.resources.ExperimentalResourceApi
import tuindice.base.generated.resources.Res

@Composable
fun EmptyStateAnimationView() {
	SharedLottieAnimation(
		filePath = EMPTY_STATE_ANIMATION_PATH,
		modifier = Modifier.size(256.dp)
	)
}

@Composable
fun ErrorStateAnimationView() {
	SharedLottieAnimation(
		filePath = ERROR_STATE_ANIMATION_PATH,
		modifier = Modifier.size(256.dp)
	)
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun SharedLottieAnimation(
	filePath: String,
	modifier: Modifier
) {
	val composition by rememberLottieComposition {
		LottieCompositionSpec.JsonString(
			Res.readBytes(filePath).decodeToString()
		)
	}

	Image(
		modifier = modifier,
		painter = rememberLottiePainter(
			composition = composition,
			iterations = Compottie.IterateForever
		),
		contentDescription = null
	)
}

private const val EMPTY_STATE_ANIMATION_PATH = "files/an_empty.json"
private const val ERROR_STATE_ANIMATION_PATH = "files/an_error.json"
