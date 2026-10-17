package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.dynamic.PropertyProvider
import io.github.alexzhirkevich.compottie.dynamic.rememberLottieDynamicProperties
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter

/**
 * Plays the Lottie file read by [readBytes]. [fillOverrides] recolors fills by name: a key that names a layer recolors
 * every fill inside that layer, and a key that names a fill recolors that fill in any layer.
 */
@Composable
fun LottieResourceAnimationView(
	readBytes: suspend () -> ByteArray,
	modifier: Modifier = Modifier,
	testTag: String? = null,
	iterations: Int = Compottie.IterateForever,
	fillOverrides: Map<String, Color> = emptyMap()
) {
	val playback = lottiePlayback(
		animationsEnabled = LocalTuIndiceAnimationsEnabled.current,
		iterations = iterations
	)
	val composition by rememberLottieComposition {
		LottieCompositionSpec.JsonString(readBytes().decodeToString())
	}
	val dynamicProperties = rememberLottieDynamicProperties(fillOverrides) {
		fillOverrides.forEach { (name, fillColor) ->
			val provider = PropertyProvider<Color> { _ -> fillColor }

			// A layer with that name: every fill inside it.
			shapeLayer(name) {
				fill("**") {
					color(provider)
				}
			}

			// A fill with that name, in any layer.
			shapeLayer("**") {
				fill("**", name) {
					color(provider)
				}
			}
		}
	}

	Image(
		modifier = if (testTag != null) modifier.testTag(testTag) else modifier,
		painter = rememberLottiePainter(
			composition = composition,
			isPlaying = playback.isPlaying,
			iterations = playback.iterations,
			dynamicProperties = dynamicProperties
		),
		contentDescription = null
	)
}
