package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent

@Composable
fun rememberSubjectDetailBarChartColumnProvider(): ColumnCartesianLayer.ColumnProvider {
	val baseColumn = remember {
		LineComponent(
			fill = Fill(Color(0xFF4A8DFF)),
			thickness = 24.dp,
			shape = RoundedCornerShape(10.dp)
		)
	}
	return remember(baseColumn) {
		ColumnCartesianLayer.ColumnProvider.series(baseColumn)
	}
}
