package com.gdavidpb.tuindice.login.ui.view

import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext

@Composable
fun AnimatedBackgroundView(
	@DrawableRes background: Int,
	content: @Composable () -> Unit
) {
	val resources = LocalContext.current.resources

	val backgroundBitmap = remember(resources) {
		BitmapFactory.decodeResource(resources, background).asImageBitmap()
	}

	val backgroundWidth = remember(backgroundBitmap) {
		backgroundBitmap.width.toFloat()
	}

	val backgroundBrush = remember(backgroundBitmap) {
		ShaderBrush(
			ImageShader(
				image = backgroundBitmap,
				tileModeX = TileMode.Repeated,
				tileModeY = TileMode.Repeated
			)
		)
	}

	val infiniteTransition = rememberInfiniteTransition()

	val animatedX by infiniteTransition.animateFloat(
		initialValue = 0f,
		targetValue = backgroundWidth,
		animationSpec = infiniteRepeatable(
			animation = tween(durationMillis = 30000, easing = LinearEasing),
			repeatMode = RepeatMode.Restart
		)
	)

	Box(
		modifier = Modifier
			.fillMaxSize()
	) {
		Canvas(
			modifier = Modifier
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.background)
				.alpha(0.5f),
			onDraw = {
				translate(left = animatedX, top = 0f) {
					drawRect(
						brush = backgroundBrush,
						size = Size(width = backgroundWidth, height = size.height)
					)
				}

				translate(left = animatedX - backgroundWidth, top = 0f) {
					drawRect(
						brush = backgroundBrush,
						size = Size(width = backgroundWidth, height = size.height)
					)
				}
			}
		)

		content()
	}
}