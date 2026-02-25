package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.presentation.model.SummaryEntry

@Composable
fun StatusCardItemView(
	header: String,
	entries: List<SummaryEntry>,
	lineWidth: Dp = 8.dp
) {
	val lineWidthPx = with(LocalDensity.current) { lineWidth.toPx() }

	ElevatedCard(
		modifier = Modifier
			.fillMaxWidth()
			.padding(
				horizontal = 16.dp,
				vertical = 12.dp
			)
	) {
		Text(
			modifier = Modifier
				.padding(
					vertical = 8.dp,
					horizontal = 16.dp
				),
			text = header
		)

		Row(
			modifier = Modifier
				.padding(
					vertical = 8.dp,
					horizontal = 16.dp
				)
				.fillMaxWidth()
		) {
			entries.forEach { (_, value, color) ->
				if (value > 0)
					DistributionView(
						label = "$value",
						weight = value.toFloat(),
						color = color
					)
			}
		}

		Row(
			modifier = Modifier
				.padding(
					vertical = 8.dp,
					horizontal = 16.dp
				)
				.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceAround
		) {
			entries.forEach { (label, _, color) ->
				Text(
					modifier = Modifier
						.drawWithContent {
							drawOval(
								topLeft = Offset(0f, (size.height / 2f) - (lineWidthPx / 2f)),
								size = Size(lineWidthPx, lineWidthPx),
								color = color
							)

							translate(left = lineWidthPx * 1.5f) {
								this@drawWithContent.drawContent()
							}
						},
					text = label,
					style = MaterialTheme.typography.labelLarge
				)
			}
		}
	}
}

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
