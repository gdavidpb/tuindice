package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.label_add_evaluation_date
import tuindice.evaluations.generated.resources.label_add_evaluation_grades
import tuindice.evaluations.generated.resources.label_add_evaluation_max_grade
import tuindice.evaluations.generated.resources.label_add_evaluation_subject
import tuindice.evaluations.generated.resources.label_add_evaluation_type

@Composable
fun EvaluationContentView(
	modifier: Modifier = Modifier,
	state: Evaluation.State.Content,
	onSubjectChange: (subject: Subject) -> Unit,
	onTypeChange: (type: EvaluationType) -> Unit,
	onDateChange: (date: Long?) -> Unit,
	onGradeClick: (grade: Double?, maxGrade: Double?) -> Unit,
	onMaxGradeClick: (grade: Double?) -> Unit,
	onDoneClick: (
		subject: Subject?,
		type: EvaluationType?,
		scheduleMode: EvaluationScheduleMode,
		date: Long?,
		grade: Double?,
		maxGrade: Double?
	) -> Unit
) {
	Box(
		modifier = Modifier
			.testTag(EvaluationsUiTags.EvaluationContentContainer)
			.fillMaxSize()
	) {
		Column(
			modifier = modifier
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp)
		) {
			Text(
				modifier = Modifier
					.fillMaxWidth(),
				text = stringResource(Res.string.label_add_evaluation_subject),
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.Medium
			)

			EvaluationSubjectPicker(
				subjects = state.availableSubjects,
				selectedSubject = state.selectedSubject,
				onSubjectChange = onSubjectChange
			)

			Text(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp),
				text = stringResource(Res.string.label_add_evaluation_type),
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
					.padding(vertical = 12.dp),
				text = stringResource(Res.string.label_add_evaluation_date),
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.Medium
			)

			EvaluationDatePicker(
				modifier = Modifier.fillMaxWidth(),
				selectedScheduleMode = state.scheduleMode,
				selectedDate = state.date,
				onDateChange = onDateChange
			)

			Text(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 12.dp),
				text = if (!state.isOverdue) {
					stringResource(Res.string.label_add_evaluation_max_grade)
				} else {
					stringResource(Res.string.label_add_evaluation_grades)
				},
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.Medium
			)

			AnimatedVisibility(visible = !state.isOverdue) {
				InputChip(
					modifier = Modifier.testTag(EvaluationsUiTags.EvaluationMaxGradeChip),
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
						modifier = Modifier.testTag(EvaluationsUiTags.EvaluationGradeChip),
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
							.padding(all = 8.dp),
						text = "/",
						style = MaterialTheme.typography.titleLarge
					)

					InputChip(
						modifier = Modifier.testTag(EvaluationsUiTags.EvaluationMaxGradeChip),
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
				.testTag(EvaluationsUiTags.EvaluationDoneFab)
				.align(Alignment.BottomEnd)
				.padding(24.dp),
			containerColor = MaterialTheme.colorScheme.primary,
			onClick = {
				onDoneClick(
					state.selectedSubject,
					state.type,
					state.scheduleMode,
					state.date,
					state.grade,
					state.maxGrade
				)
			}
		) {
			Icon(
				imageVector = Icons.Outlined.Done,
				contentDescription = null
			)
		}
	}
}
