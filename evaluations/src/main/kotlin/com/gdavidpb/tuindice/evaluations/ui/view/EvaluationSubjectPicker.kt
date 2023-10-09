package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.evaluations.R

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EvaluationSubjectPicker(
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	subjects: List<Subject>,
	selectedSubject: Subject? = subjects.firstOrNull(),
	onSubjectChange: (subject: Subject) -> Unit
) {
	val selectedSubjectState = remember { mutableStateOf(selectedSubject) }

	FlowRow(
		modifier = modifier
			.padding(top = dimensionResource(id = R.dimen.dp_8))
			.fillMaxWidth(),
		horizontalArrangement = Arrangement
			.spacedBy(
				space = dimensionResource(id = R.dimen.dp_8)
			)
	) {
		subjects
			.forEach { subject ->
				FilterChip(
					selected = (subject == selectedSubjectState.value),
					enabled = enabled,
					onClick = {
						selectedSubjectState.value =  subject
						onSubjectChange(subject)
					},
					label = {
						Text(
							text = subject.code,
							style = MaterialTheme.typography.bodyLarge,
							maxLines = 1
						)
					}
				)
			}
	}
}