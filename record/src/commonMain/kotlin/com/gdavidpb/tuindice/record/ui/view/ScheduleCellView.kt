package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.label
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_cell_description
import tuindice.record.generated.resources.schedule_cell_description_classroom
import tuindice.record.generated.resources.schedule_cell_description_error
import tuindice.record.generated.resources.schedule_cell_description_span

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
	val description = scheduleCellDescription(day = day, cell = cell)
	// The colours of the subject's chip, so a subject looks the same here as everywhere else.
	val subjectColors = remember(cell.codeText) { CourseCodeColorGenerator.fromCode(cell.codeText) }

	Surface(
		modifier = modifier
			.testTag(RecordUiTags.scheduleCell(cell.attemptId, day.code, cell.startBlock))
			.semantics(mergeDescendants = true) { contentDescription = description },
		shape = RoundedCornerShape(TuIndiceRadius.Small),
		color = subjectColors.containerColor,
		contentColor = subjectColors.color,
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

			if (cell.blockSpan > 1 && cell.classroomText != null) {
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

@Composable
private fun scheduleCellDescription(
	day: ScheduleDay,
	cell: ScheduleCellItem
): String {
	val dayName = day.label(isShort = false)
	val timing = if (cell.blockSpan > 1) {
		stringResource(Res.string.schedule_cell_description_span, cell.codeText, dayName, cell.startBlock, cell.endBlock)
	} else {
		stringResource(Res.string.schedule_cell_description, cell.codeText, dayName, cell.startBlock)
	}
	val withClassroom = cell.classroomText?.let { classroom ->
		stringResource(Res.string.schedule_cell_description_classroom, timing, classroom)
	} ?: timing

	return cell.errorText?.let { error ->
		stringResource(Res.string.schedule_cell_description_error, withClassroom, error)
	} ?: withClassroom
}
