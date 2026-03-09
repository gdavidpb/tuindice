package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
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
		horizontalArrangement = Arrangement
			.spacedBy(
				space = 8.dp
			)
	) {
		subjects
			.forEach { subject ->
				FilterChip(
					modifier = Modifier.testTag(
						EvaluationsUiTags.evaluationSubjectChip(subject.id)
					),
					selected = (subject == selectedSubject),
					enabled = enabled,
					onClick = {
						onSubjectChange(subject)
					},
					label = {
						Text(
							text = subject.code,
							maxLines = 1
						)
					}
				)
			}
	}
}
