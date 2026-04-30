package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter

@Composable
fun LottieResourceAnimationView(
	readBytes: suspend () -> ByteArray,
	modifier: Modifier = Modifier,
	testTag: String? = null,
	iterations: Int = Compottie.IterateForever
) {
	val composition by rememberLottieComposition {
		LottieCompositionSpec.JsonString(readBytes().decodeToString())
	}

	Image(
		modifier = if (testTag != null) modifier.testTag(testTag) else modifier,
		painter = rememberLottiePainter(
			composition = composition,
			iterations = iterations
		),
		contentDescription = null
	)
}
