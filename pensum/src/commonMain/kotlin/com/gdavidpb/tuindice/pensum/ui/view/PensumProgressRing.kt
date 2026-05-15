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
	Canvas(modifier = Modifier.size(24.dp)) {
		drawCircle(color = PanelBorder, style = Stroke(width = 4.dp.toPx()))
		drawArc(
			color = Approved,
			startAngle = -90f,
			sweepAngle = 360f * progress.coerceIn(0f, 1f),
			useCenter = false,
			style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
		)
	}
}
