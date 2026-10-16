package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceDarkTheme
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.ScheduleGridDefaults
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_now_line

/**
 * The "now" line of the week grid: a dot where it starts and a line across today's column, at the
 * height of the current hour. Each stretch takes the ink of what lies behind it: the sheet's own
 * over the sheet and, over the meetings in [cellSpans] (measured from the left edge), the dark one,
 * because a subject's colour is light on both themes. So on the dark theme the line is light over
 * the sheet and dark over a meeting, and on the light theme it is dark all along.
 */
@Composable
fun ScheduleNowLineView(
	modifier: Modifier = Modifier,
	cellSpans: List<ClosedRange<Dp>> = emptyList()
) {
	val onSheet = MaterialTheme.colorScheme.onSurface
	val onCell = if (TuIndiceDarkTheme.isDark()) MaterialTheme.colorScheme.inverseOnSurface else onSheet
	val description = stringResource(Res.string.schedule_now_line)

	Box(
		modifier = modifier
			.testTag(RecordUiTags.ScheduleNowLine)
			.height(ScheduleGridDefaults.NowDotSize)
			.semantics { contentDescription = description }
			.drawBehind {
				drawNowLine(color = onSheet)

				cellSpans.forEach { span ->
					clipRect(left = span.start.toPx(), right = span.endInclusive.toPx()) {
						drawNowLine(color = onCell)
					}
				}
			}
	)
}

private fun DrawScope.drawNowLine(color: Color) {
	val dotRadius = size.height / 2

	drawCircle(color = color, radius = dotRadius, center = Offset(x = dotRadius, y = center.y))
	drawLine(
		color = color,
		start = Offset(x = dotRadius, y = center.y),
		end = Offset(x = size.width, y = center.y),
		strokeWidth = ScheduleGridDefaults.NowLineThickness.toPx()
	)
}
