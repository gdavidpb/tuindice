package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun PensumProgressRing(progress: Float) {
	val graphColors = pensumGraphColors()
	Canvas(modifier = Modifier.size(24.dp)) {
		drawCircle(color = graphColors.panelBorder, style = Stroke(width = 4.dp.toPx()))
		drawArc(
			color = graphColors.approved,
			startAngle = -90f,
			sweepAngle = 360f * progress.coerceIn(0f, 1f),
			useCenter = false,
			style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
		)
	}
}
