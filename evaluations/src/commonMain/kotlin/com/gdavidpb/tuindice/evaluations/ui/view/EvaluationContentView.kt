package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationRequiredField
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.button_add_evaluation
import tuindice.evaluations.generated.resources.button_save_evaluation_changes
import tuindice.evaluations.generated.resources.error_evaluation_max_grade_missed
import tuindice.evaluations.generated.resources.error_evaluation_subject_missed
import tuindice.evaluations.generated.resources.error_evaluation_type_missed
import tuindice.evaluations.generated.resources.evaluation_name
import tuindice.evaluations.generated.resources.label_add_evaluation_date
import tuindice.evaluations.generated.resources.label_add_evaluation_subject
import tuindice.evaluations.generated.resources.label_add_evaluation_type

@Composable
fun EvaluationContentView(
	modifier: Modifier = Modifier,
	state: Evaluation.State.Content,
	onAttemptChange: (attempt: EditableAttemptDescriptor?) -> Unit,
	onTypeChange: (type: EvaluationType?) -> Unit,
	onDateChange: (date: Long?) -> Unit,
	onGradeClick: (evaluationName: String, subjectCode: String, grade: Double?, maxGrade: Double?) -> Unit,
	onMaxGradeClick: (evaluationName: String, subjectCode: String, grade: Double?) -> Unit,
	onDoneClick: () -> Unit
) {
	val selectedTypeLabel = state.typeItems.firstOrNull { item -> item.isSelected }?.labelText.orEmpty()
	val dialogEvaluationName = if (selectedTypeLabel.isBlank()) {
		""
	} else {
		stringResource(Res.string.evaluation_name, selectedTypeLabel, 1)
	}
	val dialogSubjectCode = state.selectedAttempt?.code.orEmpty()

	Box(
		modifier = Modifier
			.testTag(EvaluationsUiTags.EvaluationContentContainer)
			.fillMaxSize()
	) {
		Column(
			modifier = modifier
				.verticalScroll(rememberScrollState())
				.padding(
					top = InternalScreenDefaults.TopBarSpacing,
					start = 16.dp,
					end = 16.dp
				)
		) {
			Text(
				modifier = Modifier
					.fillMaxWidth(),
				text = stringResource(Res.string.label_add_evaluation_subject),
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.Medium
			)

			EvaluationAttemptPicker(
				items = state.attemptItems,
				onAttemptChange = onAttemptChange
			)

			if (EvaluationRequiredField.SUBJECT in state.missingFields) {
				EvaluationRequiredFieldError(
					modifier = Modifier.testTag(EvaluationsUiTags.EvaluationSubjectRequiredError),
					text = stringResource(Res.string.error_evaluation_subject_missed)
				)
			}

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
				items = state.typeItems,
				onTypeChange = onTypeChange
			)

			if (EvaluationRequiredField.TYPE in state.missingFields) {
				EvaluationRequiredFieldError(
					modifier = Modifier.testTag(EvaluationsUiTags.EvaluationTypeRequiredError),
					text = stringResource(Res.string.error_evaluation_type_missed)
				)
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

			EvaluationDatePicker(
				modifier = Modifier.fillMaxWidth(),
				selectedScheduleMode = state.scheduleMode,
				selectedDate = state.date,
				onDateChange = onDateChange,
				selectableRange = state.selectableDateRange
			)

			Text(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 12.dp),
				text = state.gradeSection.titleText,
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.Medium
			)

			AnimatedVisibility(visible = !state.gradeSection.showsGradeChip) {
				InputChip(
					modifier = Modifier.testTag(EvaluationsUiTags.EvaluationMaxGradeChip),
					selected = false,
					onClick = {
						onMaxGradeClick(dialogEvaluationName, dialogSubjectCode, state.maxGrade)
					},
					label = {
						Text(
							text = state.gradeSection.maxGradeText,
							style = MaterialTheme.typography.titleMedium
						)
					}
				)
			}

			if (EvaluationRequiredField.MAX_GRADE in state.missingFields) {
				EvaluationRequiredFieldError(
					modifier = Modifier.testTag(EvaluationsUiTags.EvaluationMaxGradeRequiredError),
					text = stringResource(Res.string.error_evaluation_max_grade_missed)
				)
			}

			AnimatedVisibility(visible = state.gradeSection.showsGradeChip) {
				Row(
					verticalAlignment = Alignment.CenterVertically
				) {
					InputChip(
						modifier = Modifier.testTag(EvaluationsUiTags.EvaluationGradeChip),
						selected = false,
						onClick = {
							onGradeClick(dialogEvaluationName, dialogSubjectCode, state.grade, state.maxGrade)
						},
						label = {
							Text(
								text = state.gradeSection.gradeText,
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
							onMaxGradeClick(dialogEvaluationName, dialogSubjectCode, state.maxGrade)
						},
						label = {
							Text(
								text = state.gradeSection.maxGradeText,
								style = MaterialTheme.typography.titleMedium
							)
						}
					)
				}
			}
		}

		Button(
			modifier = Modifier
				.testTag(EvaluationsUiTags.EvaluationDoneFab)
				.align(Alignment.BottomEnd)
				.padding(24.dp)
				.height(52.dp)
				.widthIn(min = 168.dp),
			shape = RoundedCornerShape(TuIndiceRadius.Full),
			enabled = state.canSubmit,
			onClick = {
				onDoneClick()
			}
		) {
			if (state.isSubmitting) {
				CircularProgressIndicator(
					modifier = Modifier
						.testTag(EvaluationsUiTags.EvaluationDoneProgress)
						.size(20.dp),
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					strokeWidth = 2.dp
				)
			} else {
				Text(
					text = if (state.evaluationId == null)
						stringResource(Res.string.button_add_evaluation)
					else
						stringResource(Res.string.button_save_evaluation_changes)
				)
			}
		}
	}
}
