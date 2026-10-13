package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.ui.RecordUiTags

/**
 * What a meeting of the table says inside its cell: the range of blocks (in bold while the class is
 * being taught) and, under it, the evaluation icon when an evaluation of this week falls on the
 * meeting. Both take [textColor], which the cell picks from its state, so the icon stays readable on
 * the fill of "in progress" and in the alert tone of a clash. A clash is also tagged here, on the
 * content, so the tag survives the cell being a button.
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
	// The clash mark lives here, inside the cell: the cell itself is a button when an evaluation falls
	// on it, and the button's own tag is the evaluation one.
	val clashModifier = if (meeting.isClash) {
		Modifier.testTag(RecordUiTags.scheduleTableClash(attemptId, day.code))
	} else {
		Modifier
	}
	val textModifier = if (isInProgress) {
		Modifier.testTag(RecordUiTags.scheduleTableInProgress(attemptId, day.code))
	} else {
		Modifier
	}

	Column(
		modifier = modifier.then(clashModifier).padding(vertical = TuIndiceSpacing.XSmall),
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
			ScheduleEvaluationMarkIcon(
				modifier = Modifier.testTag(RecordUiTags.scheduleTableEvaluationIcon(attemptId, day.code)),
				tint = textColor
			)
		}
	}
}
