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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import java.util.Date

@Composable
fun EvaluationContentView(
	state: Evaluation.State.Content,
	onDoneClick: () -> Unit
) {
	val evaluation = remember {
		mutableStateOf(state)
	}

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
					.padding(vertical = dimensionResource(id = R.dimen.dp_6))
					.fillMaxWidth(),
				name = evaluation.value.name,
				onNameChanged = { name ->
					evaluation.value = evaluation.value
						.copy(name = name)
				}
			)

			Text(
				text = stringResource(id = R.string.label_evaluation_subject).uppercase(),
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.outline
			)

			EvaluationSubjectTextField(
				modifier = Modifier
					.padding(top = dimensionResource(id = R.dimen.dp_6))
					.fillMaxWidth(),
				subjects = evaluation.value.availableSubjects,
				selectedSubject = evaluation.value.subject,
				onSubjectChanged = { subject ->
					evaluation.value = evaluation.value
						.copy(subject = subject)
				}
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
				selectedDate = evaluation.value.date?.let(::Date),
				onDateChanged = { date ->
					evaluation.value = evaluation.value
						.copy(date = date.time)
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
				grade = evaluation.value.grade,
				maxGrade = evaluation.value.maxGrade,
				onGradeChanged = { grade ->
					evaluation.value = evaluation.value
						.copy(grade = grade)
				}
			)

			Text(
				text = stringResource(id = R.string.label_evaluation_type).uppercase(),
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.outline
			)

			EvaluationTypePicker(
				selectedType = evaluation.value.type,
				onTypeChanged = { type ->
					evaluation.value = evaluation.value
						.copy(type = type)
				}
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