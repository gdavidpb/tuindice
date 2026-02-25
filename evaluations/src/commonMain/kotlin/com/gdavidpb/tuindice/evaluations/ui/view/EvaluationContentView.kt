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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.formatAsShortDayOfWeekAndDate
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.evaluation_attendance
import tuindice.evaluations.generated.resources.evaluation_essay
import tuindice.evaluations.generated.resources.evaluation_interventions
import tuindice.evaluations.generated.resources.evaluation_laboratory
import tuindice.evaluations.generated.resources.evaluation_model
import tuindice.evaluations.generated.resources.evaluation_other
import tuindice.evaluations.generated.resources.evaluation_presentation
import tuindice.evaluations.generated.resources.evaluation_project
import tuindice.evaluations.generated.resources.evaluation_quiz
import tuindice.evaluations.generated.resources.evaluation_report
import tuindice.evaluations.generated.resources.evaluation_test
import tuindice.evaluations.generated.resources.evaluation_workshop
import tuindice.evaluations.generated.resources.evaluation_written_work
import tuindice.evaluations.generated.resources.label_add_evaluation_date
import tuindice.evaluations.generated.resources.label_add_evaluation_grades
import tuindice.evaluations.generated.resources.label_add_evaluation_max_grade
import tuindice.evaluations.generated.resources.label_add_evaluation_subject
import tuindice.evaluations.generated.resources.label_add_evaluation_type
import tuindice.evaluations.generated.resources.label_evaluation_assign_today
import tuindice.evaluations.generated.resources.label_evaluation_no_date_short

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
		date: Long?,
		grade: Double?,
		maxGrade: Double?
	) -> Unit
) {
	Box(
		modifier = Modifier
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

			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp)
			) {
				EvaluationType.entries.forEach { type ->
					InputChip(
						modifier = Modifier
							.padding(end = 8.dp),
						selected = state.type == type,
						onClick = {
							onTypeChange(type)
						},
						label = {
							Text(evaluationTypeLabel(type = type))
						}
					)
				}
			}

			Text(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 12.dp),
				text = stringResource(Res.string.label_add_evaluation_date),
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.Medium
			)

			Row(
				modifier = Modifier
					.fillMaxWidth()
			) {
				InputChip(
					selected = false,
					onClick = {
						onDateChange(currentTimeMillis())
					},
					label = {
						Text(
							if (state.date == null) {
								stringResource(Res.string.label_evaluation_assign_today)
							} else {
								state.date.formatAsShortDayOfWeekAndDate()
							}
						)
					}
				)

				InputChip(
					modifier = Modifier
						.padding(start = 8.dp),
					selected = state.date == null,
					onClick = {
						onDateChange(null)
					},
					label = {
						Text(stringResource(Res.string.label_evaluation_no_date_short))
					}
				)
			}

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
							.padding(all = 8.dp),
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
				.padding(24.dp),
			containerColor = MaterialTheme.colorScheme.primary,
			onClick = {
				onDoneClick(
					state.selectedSubject,
					state.type,
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

@Composable
private fun evaluationTypeLabel(type: EvaluationType) = when (type) {
	EvaluationType.TEST -> stringResource(Res.string.evaluation_test)
	EvaluationType.ESSAY -> stringResource(Res.string.evaluation_essay)
	EvaluationType.ATTENDANCE -> stringResource(Res.string.evaluation_attendance)
	EvaluationType.INTERVENTIONS -> stringResource(Res.string.evaluation_interventions)
	EvaluationType.LABORATORY -> stringResource(Res.string.evaluation_laboratory)
	EvaluationType.MODEL -> stringResource(Res.string.evaluation_model)
	EvaluationType.PRESENTATION -> stringResource(Res.string.evaluation_presentation)
	EvaluationType.PROJECT -> stringResource(Res.string.evaluation_project)
	EvaluationType.QUIZ -> stringResource(Res.string.evaluation_quiz)
	EvaluationType.REPORT -> stringResource(Res.string.evaluation_report)
	EvaluationType.WORKSHOP -> stringResource(Res.string.evaluation_workshop)
	EvaluationType.WRITTEN_WORK -> stringResource(Res.string.evaluation_written_work)
	EvaluationType.OTHER -> stringResource(Res.string.evaluation_other)
}
