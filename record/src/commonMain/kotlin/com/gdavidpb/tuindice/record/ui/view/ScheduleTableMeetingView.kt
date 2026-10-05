package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.ScheduleClashDefaults

/**
 * One meeting in the schedule table, as the range of blocks it covers. A meeting that overlaps
 * another subject's that day takes the alert tone, the same clash the weekly grid borders. The
 * class being taught right now is filled with the accent, in bold over it. A meeting can be both:
 * the fill says "in progress" and the alert border still says "clash".
 */
@Composable
fun ScheduleTableMeetingView(
	modifier: Modifier = Modifier,
	attemptId: String,
	day: ScheduleDay,
	meeting: ScheduleCellItem
) {
	val warning = AcademicStatusColors.warning()
	val isClash = meeting.isClash
	val isInProgress = meeting.isInProgress
	val textColor = when {
		isInProgress -> MaterialTheme.colorScheme.onPrimary
		isClash -> warning
		else -> MaterialTheme.colorScheme.onSurface
	}
	val fillColor = when {
		isInProgress -> MaterialTheme.colorScheme.primary
		isClash -> warning.copy(alpha = TuIndiceAlpha.SurfaceTint)
		else -> Color.Transparent
	}
	// The clash tag sits on the surface; the in-progress one on the text, so a meeting that is both
	// answers to each.
	val textModifier = if (isInProgress) {
		Modifier.testTag(RecordUiTags.scheduleTableInProgress(attemptId, day.code))
	} else {
		Modifier
	}
	val surfaceModifier = if (isClash) {
		modifier.testTag(RecordUiTags.scheduleTableClash(attemptId, day.code))
	} else {
		modifier
	}

	Surface(
		modifier = surfaceModifier.fillMaxWidth(),
		shape = RoundedCornerShape(TuIndiceRadius.ExtraSmall),
		color = fillColor,
		border = if (isClash) {
			BorderStroke(width = ScheduleClashDefaults.BorderWidth, color = warning.copy(alpha = TuIndiceAlpha.BorderStrong))
		} else {
			null
		}
	) {
		Text(
			modifier = textModifier.padding(vertical = TuIndiceSpacing.XSmall),
			text = meeting.blocksText,
			style = MaterialTheme.typography.labelMedium,
			fontWeight = if (isInProgress) FontWeight.Bold else null,
			color = textColor,
			textAlign = TextAlign.Center,
			maxLines = 1
		)
	}
}
