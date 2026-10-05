package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.ui.RecordUiTags

/**
 * What one subject has on one day: the range of blocks of each meeting ("1-2", or "3" for a single
 * block), one per line. A meeting that overlaps another subject's takes the alert tone; no meeting
 * leaves the cell empty. Today's cell carries a soft tint of the accent, the same one that runs
 * down today's column of the week grid.
 */
@Composable
fun ScheduleTableDayCellView(
	modifier: Modifier = Modifier,
	attemptId: String,
	day: ScheduleDay,
	meetings: List<ScheduleCellItem>,
	isToday: Boolean = false
) {
	val tint = if (isToday) {
		MaterialTheme.colorScheme.primary.copy(alpha = TuIndiceAlpha.SurfaceTint)
	} else {
		Color.Transparent
	}

	Box(
		modifier = modifier
			.testTag(RecordUiTags.scheduleTableCell(attemptId, day.code))
			.background(color = tint, shape = RoundedCornerShape(TuIndiceRadius.ExtraSmall)),
		contentAlignment = Alignment.Center
	) {
		Column(
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Two),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			meetings.forEach { meeting ->
				ScheduleTableMeetingView(
					attemptId = attemptId,
					day = day,
					meeting = meeting
				)
			}
		}
	}
}
