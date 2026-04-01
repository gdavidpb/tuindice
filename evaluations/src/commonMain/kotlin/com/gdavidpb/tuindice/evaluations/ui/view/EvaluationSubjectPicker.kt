package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.ui.style.SubjectColorGenerator
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags

@Composable
fun EvaluationSubjectPicker(
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	subjects: List<Subject>,
	selectedSubject: Subject? = subjects.firstOrNull(),
	onSubjectChange: (subject: Subject) -> Unit
) {
	FlowRow(
		modifier = modifier
			.testTag(EvaluationsUiTags.EvaluationSubjectPickerRow)
			.padding(top = 8.dp)
			.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(6.dp)
	) {
		subjects
			.forEach { subject ->
				val subjectColors = remember(subject.code) {
					SubjectColorGenerator.fromCode(subject.code)
				}

				FilterChip(
					modifier = Modifier.testTag(
						EvaluationsUiTags.evaluationSubjectChip(subject.id)
					),
					selected = (subject == selectedSubject),
					enabled = enabled,
					colors = FilterChipDefaults.filterChipColors(
						containerColor = subjectColors.containerColor.copy(alpha = 0.42f),
						labelColor = subjectColors.color,
						disabledContainerColor = subjectColors.containerColor.copy(alpha = 0.22f),
						disabledLabelColor = subjectColors.color.copy(alpha = 0.38f),
						selectedContainerColor = subjectColors.containerColor,
						selectedLabelColor = subjectColors.color
					),
					onClick = {
						onSubjectChange(subject)
					},
					label = {
						Text(
							text = subject.code,
							fontWeight = if (subject == selectedSubject) {
								FontWeight.SemiBold
							} else {
								FontWeight.Medium
							},
							maxLines = 1
						)
					}
				)
			}
	}
}
