package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.ui.RecordUiTags

/**
 * What one subject has on one day: the range of blocks of each meeting ("1-2", or "3" for a single
 * block), one per line. A meeting that overlaps another subject's takes the alert tone; no meeting
 * leaves the cell empty.
 */
@Composable
fun ScheduleTableDayCellView(
	modifier: Modifier = Modifier,
	attemptId: String,
	day: ScheduleDay,
	meetings: List<ScheduleCellItem>
) {
	Box(
		modifier = modifier.testTag(RecordUiTags.scheduleTableCell(attemptId, day.code)),
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
