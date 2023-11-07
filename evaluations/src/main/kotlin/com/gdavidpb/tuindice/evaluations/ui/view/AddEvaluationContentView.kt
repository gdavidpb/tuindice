package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEvaluationContentView(
	modifier: Modifier = Modifier,
	state: AddEvaluation.State.Content,
	onSubjectChange: (subject: Subject) -> Unit,
	onTypeChange: (type: EvaluationType) -> Unit,
	onDateChange: (date: Long?) -> Unit,
	onGradeClick: (grade: Double?, maxGrade: Double?) -> Unit,
	onMaxGradeClick: (grade: Double?) -> Unit,
	onDoneClick: () -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
	) {
		Column(
			modifier = modifier
				.padding(horizontal = dimensionResource(id = R.dimen.dp_16))
		) {
			Text(
				modifier = Modifier
					.fillMaxWidth(),
				text = stringResource(
					id = R.string.label_add_evaluation_subject
				),
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.Medium
			)

			EvaluationSubjectPicker(
				subjects = state.availableSubjects,
				selectedSubject = state.subject,
				onSubjectChange = onSubjectChange
			)

			Text(
				modifier = Modifier
					.fillMaxWidth()
					.padding(
						top = dimensionResource(id = R.dimen.dp_8)
					),
				text = stringResource(
					id = R.string.label_add_evaluation_type
				),
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.Medium
			)

			EvaluationTypePicker(
				selectedType = state.type,
				onTypeChange = onTypeChange
			)

			Text(
				modifier = Modifier
					.fillMaxWidth()
					.padding(
						vertical = dimensionResource(id = R.dimen.dp_12)
					),
				text = stringResource(
					id = R.string.label_add_evaluation_date
				),
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.Medium
			)

			EvaluationDatePicker(
				modifier = Modifier
					.fillMaxWidth(),
				selectedDate = state.date,
				onDateChange = onDateChange
			)

			Text(
				modifier = Modifier
					.fillMaxWidth()
					.padding(
						vertical = dimensionResource(id = R.dimen.dp_12)
					),
				text = stringResource(
					id = if (!state.isOverdue)
						R.string.label_add_evaluation_max_grade
					else
						R.string.label_add_evaluation_grades
				),
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.Medium
			)

			AnimatedVisibility(visible = !state.isOverdue) {
				InputChip(
					selected = false,
					onClick = {
						onMaxGradeClick(state.maxGrade)
					},
					label = {
						Text(
							text = (state.maxGrade ?: 0.0).formatGrade(decimals = 2),
							style = MaterialTheme.typography.titleMedium
						)
					}
				)
			}

			AnimatedVisibility(visible = state.isOverdue) {
				Row(
					verticalAlignment = Alignment.CenterVertically
				) {
					InputChip(
						selected = false,
						onClick = {
							onGradeClick(state.grade, state.maxGrade)
						},
						label = {
							Text(
								text = (state.grade ?: 0.0).formatGrade(decimals = 2),
								style = MaterialTheme.typography.titleMedium
							)
						}
					)

					Text(
						modifier = Modifier
							.padding(all = dimensionResource(id = R.dimen.dp_8)),
						text = "/",
						style = MaterialTheme.typography.titleLarge
					)

					InputChip(
						selected = false,
						onClick = {
							onMaxGradeClick(state.maxGrade)
						},
						label = {
							Text(
								text = (state.maxGrade ?: 0.0).formatGrade(decimals = 2),
								style = MaterialTheme.typography.titleMedium
							)
						}
					)
				}
			}
		}

		FloatingActionButton(
			modifier = Modifier
				.align(Alignment.BottomEnd)
				.padding(dimensionResource(id = R.dimen.dp_24)),
			containerColor = MaterialTheme.colorScheme.primary,
			onClick = onDoneClick
		) {
			Icon(
				imageVector = Icons.Outlined.Done,
				contentDescription = null
			)
		}
	}
}