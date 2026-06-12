package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.subjects.ui.model.SubjectChartDefaults
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent

@Composable
fun rememberSubjectDetailBarChartColumnProvider(): ColumnCartesianLayer.ColumnProvider {
	val baseColumn = remember {
		LineComponent(
			fill = Fill(SubjectChartDefaults.BarColor),
			thickness = 24.dp,
			shape = RoundedCornerShape(TuIndiceRadius.Medium)
		)
	}
	return remember(baseColumn) {
		ColumnCartesianLayer.ColumnProvider.series(baseColumn)
	}
}
