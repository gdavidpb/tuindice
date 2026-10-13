package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.ui.RecordUiTags

/**
 * What a meeting of the table says inside its cell: the range of blocks (in bold while the class is
 * being taught) and, under it, the evaluation icon when an evaluation of this week falls on the
 * meeting. Both take [textColor], which the cell picks from its state, so the icon stays readable on
 * the fill of "in progress" and in the alert tone of a clash.
 */
@Composable
fun ScheduleTableMeetingBlocksView(
	modifier: Modifier = Modifier,
	attemptId: String,
	day: ScheduleDay,
	meeting: ScheduleCellItem,
	textColor: Color
) {
	val isInProgress = meeting.isInProgress
	val textModifier = if (isInProgress) {
		Modifier.testTag(RecordUiTags.scheduleTableInProgress(attemptId, day.code))
	} else {
		Modifier
	}

	Column(
		modifier = modifier.padding(vertical = TuIndiceSpacing.XSmall),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Two),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			modifier = textModifier,
			text = meeting.blocksText,
			style = MaterialTheme.typography.labelMedium,
			fontWeight = if (isInProgress) FontWeight.Bold else null,
			color = textColor,
			textAlign = TextAlign.Center,
			maxLines = 1
		)

		if (meeting.hasEvaluation) {
			// Decorative: the row already says "con evaluación ese día".
			Icon(
				modifier = Modifier
					.testTag(RecordUiTags.scheduleTableEvaluation(attemptId, day.code))
					.size(TuIndiceComponentSizes.IconSmall),
				imageVector = Icons.Outlined.Assignment,
				contentDescription = null,
				tint = textColor
			)
		}
	}
}
