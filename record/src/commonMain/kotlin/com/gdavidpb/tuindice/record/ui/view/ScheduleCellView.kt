package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.ui.RecordUiTags

/**
 * One meeting of a subject, in the colours of the subject's chip. A meeting that overlaps another
 * subject's that day gains the alert border, the same rule the table follows.
 */
@Composable
fun ScheduleCellView(
	modifier: Modifier = Modifier,
	day: ScheduleDay,
	cell: ScheduleCellItem
) {
	val warning = AcademicStatusColors.warning()
	val description = cell.description.asString()

	Surface(
		modifier = modifier
			.testTag(RecordUiTags.scheduleCell(cell.attemptId, day.code, cell.startBlock))
			.semantics(mergeDescendants = true) { contentDescription = description },
		shape = RoundedCornerShape(TuIndiceRadius.Small),
		color = cell.codeContainerColor,
		contentColor = cell.codeColor,
		border = if (cell.isClash) {
			BorderStroke(width = TuIndiceSpacing.Two, color = warning.copy(alpha = TuIndiceAlpha.BorderStrong))
		} else {
			null
		}
	) {
		Column(
			modifier = Modifier.padding(
				horizontal = TuIndiceSpacing.Small,
				vertical = TuIndiceSpacing.XSmall
			)
		) {
			Text(
				text = cell.codeText,
				style = MaterialTheme.typography.labelMedium,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)

			if (cell.classroomText != null) {
				Text(
					text = cell.classroomText,
					style = MaterialTheme.typography.labelSmall,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
		}
	}
}
