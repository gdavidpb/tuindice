package com.gdavidpb.tuindice.login.ui.view

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource
import tuindice.login.generated.resources.Res
import tuindice.login.generated.resources.background

@Composable
fun AnimatedPatternBackground(
	background: DrawableResource = Res.drawable.background,
	tileScale: Float = 1f,
	alpha: Float = 0.5f,
	durationMillis: Int = 30_000
) {
	val density = LocalDensity.current.density.coerceAtLeast(1f)
	val backgroundBitmap = imageResource(background)

	// Compose resources can expose density-normalized image sizes on iOS.
	// Multiplying by current density keeps visual tile size aligned with Android.
	val tileWidth = remember(backgroundBitmap, density, tileScale) {
		(backgroundBitmap.width.toFloat() * density * tileScale).coerceAtLeast(1f)
	}
	val tileHeight = remember(backgroundBitmap, density, tileScale) {
		(backgroundBitmap.height.toFloat() * density * tileScale).coerceAtLeast(1f)
	}

	val transition = rememberInfiniteTransition(label = "AnimatedPatternBackground")
	val animatedProgress by transition.animateFloat(
		label = "AnimatedPatternBackground",
		initialValue = 0f,
		targetValue = 1f,
		animationSpec = infiniteRepeatable(
			animation = tween(durationMillis = durationMillis, easing = LinearEasing)
		)
	)

	Canvas(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
			.alpha(alpha)
	) {
		val viewportWidth = size.width
		val viewportHeight = size.height
		val animatedX = animatedProgress * tileWidth

		val tileWidthInt = tileWidth.toInt().coerceAtLeast(1)
		val tileHeightInt = tileHeight.toInt().coerceAtLeast(1)

		var drawX = animatedX - tileWidth
		while (drawX < viewportWidth + tileWidth) {
			var drawY = -tileHeight
			while (drawY < viewportHeight + tileHeight) {
				drawImage(
					image = backgroundBitmap,
					dstOffset = IntOffset(drawX.toInt(), drawY.toInt()),
					dstSize = IntSize(width = tileWidthInt, height = tileHeightInt)
				)
				drawY += tileHeight
			}
			drawX += tileWidth
		}
	}
}
