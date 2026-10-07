package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
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
	ObservedLottieResourceAnimationView(
		readBytes = readBytes,
		modifier = modifier,
		testTag = testTag,
		iterations = iterations,
		onPlayback = {}
	)
}

// The public view with a seam: [onPlayback] receives what was decided from
// `LocalTuIndiceAnimationsEnabled`, which is what a UI test can observe of the painter.
@Composable
internal fun ObservedLottieResourceAnimationView(
	readBytes: suspend () -> ByteArray,
	modifier: Modifier,
	testTag: String?,
	iterations: Int,
	onPlayback: (LottiePlayback) -> Unit
) {
	val playback = lottiePlayback(
		animationsEnabled = LocalTuIndiceAnimationsEnabled.current,
		iterations = iterations
	)
	val composition by rememberLottieComposition {
		LottieCompositionSpec.JsonString(readBytes().decodeToString())
	}

	SideEffect { onPlayback(playback) }

	Image(
		modifier = if (testTag != null) modifier.testTag(testTag) else modifier,
		painter = rememberLottiePainter(
			composition = composition,
			isPlaying = playback.isPlaying,
			iterations = playback.iterations
		),
		contentDescription = null
	)
}
