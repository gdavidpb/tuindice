package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun RowScope.DistributionView(
	label: String,
	weight: Float,
	color: Color
) {
	val heightPx = with(LocalDensity.current) { 6.dp.toPx() }
	val radiusPx = with(LocalDensity.current) { 8.dp.toPx() }

	Text(
		modifier = Modifier
			.weight(weight)
			.drawWithContent {
				drawRoundRect(
					color = color,
					size = Size(size.width, heightPx),
					cornerRadius = CornerRadius(radiusPx)
				)

				translate(top = heightPx * 1.5f) {
					this@drawWithContent.drawContent()
				}
			},
		text = label,
		textAlign = TextAlign.Center
	)
}
