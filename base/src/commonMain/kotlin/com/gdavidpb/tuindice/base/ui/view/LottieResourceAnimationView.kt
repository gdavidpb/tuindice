package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieAnimationState
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
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
	val animation = rememberLottieAnimationState(
		composition = composition,
		iterations = iterations
	)

	Image(
		modifier = if (testTag != null) modifier.testTag(testTag) else modifier,
		painter = rememberLottiePainter(
			composition = composition,
			progress = { animation.progress }
		),
		contentDescription = null
	)
}

/**
 * The playback of the animation: it plays only while [LocalTuIndiceAnimationsEnabled] is true
 * and otherwise stays at its first frame. Apart from the view so a test can read its progress.
 */
@Composable
internal fun rememberLottieAnimationState(
	composition: LottieComposition?,
	iterations: Int
): LottieAnimationState {
	return animateLottieCompositionAsState(
		composition = composition,
		isPlaying = LocalTuIndiceAnimationsEnabled.current,
		iterations = iterations
	)
}
