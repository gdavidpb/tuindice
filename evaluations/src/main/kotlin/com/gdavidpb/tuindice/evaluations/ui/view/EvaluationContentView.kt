package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import java.util.Date

@Composable
fun EvaluationContentView(
	state: Evaluation.State.Content
) {
	val subjectItems = state
		.availableSubjects
		.map { subject ->
			SubjectDropdownMenuItem(
				subjectId = subject.id,
				text = "${subject.code} — ${subject.name}"
			)
		}

	val selectedSubject = subjectItems.find { subject ->
		subject.subjectId == state.subject?.id
	}

	Column(
		modifier = Modifier
			.padding(
				horizontal = dimensionResource(id = R.dimen.dp_24)
			)
			.verticalScroll(
				state = rememberScrollState()
			)
			.fillMaxSize()
	) {
		Text(
			text = stringResource(id = R.string.label_evaluation_name).uppercase(),
			style = MaterialTheme.typography.labelMedium,
			color = MaterialTheme.colorScheme.outline
		)

		EvaluationNameTextField(
			modifier = Modifier
				.padding(vertical = dimensionResource(id = R.dimen.dp_6))
				.fillMaxWidth(),
			name = state.name,
			onNameChanged = { name ->
			}
		)

		Text(
			text = stringResource(id = R.string.label_evaluation_subject).uppercase(),
			style = MaterialTheme.typography.labelMedium,
			color = MaterialTheme.colorScheme.outline
		)

		EvaluationSubjectTextField(
			modifier = Modifier
				.padding(vertical = dimensionResource(id = R.dimen.dp_6))
				.fillMaxWidth(),
			subjects = subjectItems,
			selectedSubject = selectedSubject,
			onSubjectChanged = { subject ->
			}
		)

		Text(
			text = stringResource(id = R.string.label_evaluation_date).uppercase(),
			style = MaterialTheme.typography.labelMedium,
			color = MaterialTheme.colorScheme.outline
		)

		EvaluationDatePicker(
			modifier = Modifier
				.padding(vertical = dimensionResource(id = R.dimen.dp_6))
				.fillMaxWidth(),
			selectedDate = Date(state.date ?: 0L),
			onDateChanged = { date ->
			}
		)

		Text(
			text = stringResource(id = R.string.label_evaluation_grade).uppercase(),
			style = MaterialTheme.typography.labelMedium,
			color = MaterialTheme.colorScheme.outline
		)

		EvaluationGradeTextField(
			modifier = Modifier
				.padding(vertical = dimensionResource(id = R.dimen.dp_6))
				.fillMaxWidth(),
			grade = state.grade ?: 0.0,
			maxGrade = state.maxGrade ?: 0.0,
			onGradeChanged = { grade ->
			}
		)

		Text(
			text = stringResource(id = R.string.label_evaluation_type).uppercase(),
			style = MaterialTheme.typography.labelMedium,
			color = MaterialTheme.colorScheme.outline
		)

		EvaluationTypePicker(
			selectedType = state.type,
			onTypeChanged = { type ->
			}
		)
	}
}