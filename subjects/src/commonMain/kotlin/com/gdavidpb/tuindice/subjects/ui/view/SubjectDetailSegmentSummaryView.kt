package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.subjects_segment_attempts_content_description
import tuindice.subjects.generated.resources.subjects_segment_attempts_tooltip_line_1
import tuindice.subjects.generated.resources.subjects_segment_attempts_tooltip_line_2
import tuindice.subjects.generated.resources.subjects_segment_students_content_description
import tuindice.subjects.generated.resources.subjects_segment_students_tooltip_line_1
import tuindice.subjects.generated.resources.subjects_segment_students_tooltip_line_2

@Composable
fun SubjectDetailSegmentSummaryView(
	studentsText: String,
	attemptsText: String,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier,
		horizontalArrangement = Arrangement.spacedBy(16.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		SubjectDetailSummaryMetric(
			testTag = SubjectsUiTags.SegmentStudentsMetric,
			valueText = studentsText,
			contentDescription = stringResource(
				Res.string.subjects_segment_students_content_description,
				studentsText
			),
			tooltipLine1 = Res.string.subjects_segment_students_tooltip_line_1,
			tooltipLine2 = Res.string.subjects_segment_students_tooltip_line_2
		) {
			Icon(
				modifier = Modifier.size(22.dp),
				imageVector = Icons.Outlined.Groups,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}

		SubjectDetailSummaryMetric(
			testTag = SubjectsUiTags.SegmentAttemptsMetric,
			valueText = attemptsText,
			contentDescription = stringResource(
				Res.string.subjects_segment_attempts_content_description,
				attemptsText
			),
			tooltipLine1 = Res.string.subjects_segment_attempts_tooltip_line_1,
			tooltipLine2 = Res.string.subjects_segment_attempts_tooltip_line_2
		) {
			Icon(
				modifier = Modifier.size(22.dp),
				imageVector = Icons.Outlined.Repeat,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}
