package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import com.gdavidpb.tuindice.pensum.ui.view.pensumGraphColors

@Composable
fun PensumRouteConnector(
	modifier: Modifier = Modifier
) {
	val graphColors = pensumGraphColors()
	Box(
		modifier = modifier.padding(top = SubjectDetailRouteConnectorTopPadding),
		contentAlignment = Alignment.Center
	) {
		Canvas(
			modifier = Modifier
				.fillMaxWidth()
				.height(SubjectDetailRouteConnectorStrokeWidth)
		) {
			val centerY = size.height / 2f
			drawLine(
				color = graphColors.selected,
				start = Offset(0f, centerY),
				end = Offset(size.width, centerY),
				strokeWidth = SubjectDetailRouteConnectorStrokeWidth.toPx(),
				cap = StrokeCap.Round
			)
		}
	}
}
