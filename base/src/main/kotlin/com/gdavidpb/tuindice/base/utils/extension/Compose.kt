package com.gdavidpb.tuindice.base.utils.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize

@Composable
fun MeasureUnconstrainedViewSize(
	viewToMeasure: @Composable () -> Unit,
	content: @Composable (measuredSize: DpSize) -> Unit,
) {
	SubcomposeLayout { constraints ->
		val measured = subcompose("viewToMeasure", viewToMeasure)[0]
			.measure(Constraints())

		val contentPlaceable = subcompose("content") {
			content(
				DpSize(
					width = measured.width.toDp(),
					height = measured.height.toDp()
				)
			)
		}[0].measure(constraints)
		layout(contentPlaceable.width, contentPlaceable.height) {
			contentPlaceable.place(0, 0)
		}
	}
}