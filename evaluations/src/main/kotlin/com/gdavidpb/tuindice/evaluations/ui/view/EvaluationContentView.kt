package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationError
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation

@Composable
fun EvaluationContentView(
	state: Evaluation.State.Content,
	onNameChanged: (name: String) -> Unit,
	onSubjectChanged: (subject: Subject) -> Unit,
	onDateChanged: (date: Long) -> Unit,
	onGradeChanged: (grade: Double?) -> Unit,
	onTypeChanged: (type: EvaluationType?) -> Unit,
	onDoneClick: () -> Unit
) {
	val scrollState = rememberScrollState()

	Box(
		modifier = Modifier
			.fillMaxSize()
	) {
		Column(
			modifier = Modifier
				.padding(
					horizontal = dimensionResource(id = R.dimen.dp_24)
				)
				.verticalScroll(
					state = scrollState
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
					.padding(
						top = dimensionResource(id = R.dimen.dp_6),
						bottom = if (state.error is EvaluationError.EmptyName)
							dimensionResource(id = R.dimen.dp_12)
						else
							dimensionResource(id = R.dimen.dp_6)
					)
					.fillMaxWidth(),
				name = state.name,
				onNameChanged = onNameChanged,
				error = if (state.error is EvaluationError.EmptyName)
					stringResource(id = R.string.error_evaluation_name_empty)
				else
					null
			)

			Text(
				text = stringResource(id = R.string.label_evaluation_subject).uppercase(),
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.outline
			)

			EvaluationSubjectTextField(
				modifier = Modifier
					.padding(
						top = dimensionResource(id = R.dimen.dp_6),
						bottom = if (state.error is EvaluationError.SubjectMissed)
							dimensionResource(id = R.dimen.dp_12)
						else
							dimensionResource(id = R.dimen.dp_6)
					)
					.fillMaxWidth(),
				subjects = state.availableSubjects,
				selectedSubject = state.subject,
				onSubjectChanged = onSubjectChanged,
				error = if (state.error is EvaluationError.SubjectMissed)
					stringResource(id = R.string.error_evaluation_subject_missed)
				else
					null
			)

			Text(
				text = stringResource(id = R.string.label_evaluation_date).uppercase(),
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.outline
			)

			EvaluationDatePicker(
				modifier = Modifier
					.padding(vertical = dimensionResource(id = R.dimen.dp_12))
					.fillMaxWidth(),
				selectedDate = state.date,
				onDateChanged = onDateChanged
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
				grade = state.grade,
				maxGrade = state.maxGrade,
				onGradeChanged = onGradeChanged,
				error = if (state.error is EvaluationError.GradeMissed)
					stringResource(id = R.string.error_evaluation_grade_missed)
				else
					null
			)

			Text(
				text = stringResource(id = R.string.label_evaluation_type).uppercase(),
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.outline
			)

			EvaluationTypePicker(
				selectedType = state.type,
				onTypeChanged = onTypeChanged
			)
		}

		AnimatedVisibility(
			modifier = Modifier
				.align(Alignment.BottomEnd)
				.padding(dimensionResource(id = R.dimen.dp_24)),
			visible = !scrollState.isScrollInProgress,
			enter = fadeIn(),
			exit = fadeOut()
		) {
			FloatingActionButton(
				onClick = onDoneClick
			) {
				Icon(
					imageVector = Icons.Outlined.Done,
					contentDescription = null
				)
			}
		}
	}
}