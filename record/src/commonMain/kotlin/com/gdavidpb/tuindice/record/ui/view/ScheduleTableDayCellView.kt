package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
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

@Composable
private fun ScheduleTableMeetingView(
	attemptId: String,
	day: ScheduleDay,
	meeting: ScheduleCellItem
) {
	val warning = AcademicStatusColors.warning()
	val isClash = meeting.isClash
	val text = if (meeting.blockSpan > 1) "${meeting.startBlock}-${meeting.endBlock}" else "${meeting.startBlock}"
	val textColor = if (isClash) warning else MaterialTheme.colorScheme.onSurface
	val surfaceModifier = if (isClash) {
		Modifier.testTag(RecordUiTags.scheduleTableClash(attemptId, day.code))
	} else {
		Modifier
	}

	Surface(
		modifier = surfaceModifier.fillMaxWidth(),
		shape = RoundedCornerShape(TuIndiceRadius.ExtraSmall),
		color = if (isClash) warning.copy(alpha = TuIndiceAlpha.SurfaceTint) else Color.Transparent,
		border = if (isClash) {
			BorderStroke(width = TuIndiceSpacing.Hairline, color = warning.copy(alpha = TuIndiceAlpha.BorderStrong))
		} else {
			null
		}
	) {
		Text(
			modifier = Modifier.padding(vertical = TuIndiceSpacing.XSmall),
			text = text,
			style = MaterialTheme.typography.labelMedium,
			color = textColor,
			textAlign = TextAlign.Center,
			maxLines = 1
		)
	}
}
