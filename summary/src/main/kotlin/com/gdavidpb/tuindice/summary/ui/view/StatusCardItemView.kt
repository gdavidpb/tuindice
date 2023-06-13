package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.presentation.model.SummaryEntry

@Composable
fun StatusCardItemView(
	header: String,
	entries: List<SummaryEntry>
) {
	val lineWidth = with(LocalDensity.current) { dimensionResource(id = R.dimen.dp_8).toPx() }

	ElevatedCard(
		modifier = Modifier
			.fillMaxWidth()
			.height(dimensionResource(id = R.dimen.dp_128))
			.padding(
				horizontal = dimensionResource(id = R.dimen.dp_16),
				vertical = dimensionResource(id = R.dimen.dp_8)
			)
	) {
		Text(
			modifier = Modifier
				.padding(
					vertical = dimensionResource(id = R.dimen.dp_8),
					horizontal = dimensionResource(id = R.dimen.dp_16)
				),
			text = header
		)

		Row(
			modifier = Modifier
				.padding(
					vertical = dimensionResource(id = R.dimen.dp_8),
					horizontal = dimensionResource(id = R.dimen.dp_16)
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
					vertical = dimensionResource(id = R.dimen.dp_8),
					horizontal = dimensionResource(id = R.dimen.dp_16)
				)
				.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceAround
		) {
			entries.forEach { (label, _, color) ->
				Text(
					modifier = Modifier
						.drawWithContent {
							drawOval(
								topLeft = Offset(0f, (size.height / 2) - (lineWidth / 2)),
								size = Size(lineWidth, lineWidth),
								color = color
							)

							translate(left = lineWidth * 1.5f) {
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
	val height = with(LocalDensity.current) { dimensionResource(id = R.dimen.dp_6).toPx() }
	val radius = with(LocalDensity.current) { dimensionResource(id = R.dimen.dp_8).toPx() }

	Text(
		modifier = Modifier
			.weight(weight)
			.drawWithContent {
				drawRoundRect(
					color = color,
					size = Size(size.width, height),
					cornerRadius = CornerRadius(radius)
				)

				translate(top = height * 1.5f) {
					this@drawWithContent.drawContent()
				}
			},
		text = label,
		textAlign = TextAlign.Center
	)
}