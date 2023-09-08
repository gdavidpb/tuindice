package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.R

@Composable
fun QuarterItemView(
	modifier: Modifier = Modifier,
	name: String,
	grade: Double,
	gradeSum: Double,
	credits: Int,
	subjects: List<Subject>,
	onSubjectGradeChange: (subject: Subject, newGrade: Int, isSelected: Boolean) -> Unit
) {
	val subjectsStates = rememberSubjectsStates(subjects = subjects)

	ElevatedCard(
		modifier = modifier
			.fillMaxWidth()
			.padding(
				horizontal = dimensionResource(id = R.dimen.dp_16),
				vertical = dimensionResource(id = R.dimen.dp_8)
			)
	) {
		Column(
			modifier = Modifier
				.padding(dimensionResource(id = R.dimen.dp_8))
		) {
			Text(
				modifier = Modifier
					.fillMaxWidth()
					.padding(dimensionResource(id = R.dimen.dp_8)),
				text = name,
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Black
			)

			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(
						vertical = dimensionResource(id = R.dimen.dp_4)
					),
				horizontalArrangement = Arrangement.SpaceAround
			) {
				Text(
					text = stringResource(id = R.string.quarter_grade_diff, grade)
						.annotatedQuarterValue(),
					style = MaterialTheme.typography.titleMedium
				)
				Text(
					text = stringResource(id = R.string.quarter_grade_sum, gradeSum)
						.annotatedQuarterValue(),
					style = MaterialTheme.typography.titleMedium
				)
				Text(
					text = stringResource(id = R.string.quarter_credits, credits)
						.annotatedQuarterValue(-0.1f),
					style = MaterialTheme.typography.titleMedium
				)
			}

			subjects.forEach { subject ->
				SubjectItemView(
					code = subject.code,
					name = subject.name,
					credits = subject.credits,
					grade = subjectsStates[subject.id]?.intValue ?: subject.grade,
					isRetired = subject.grade == MIN_SUBJECT_GRADE || subject.isRetired,
					isNoEffect = subject.isNoEffect,
					isEditable = subject.isEditable,
					onGradeChange = { newGrade, isSelected ->
						subjectsStates[subject.id]?.intValue = newGrade

						onSubjectGradeChange(subject, newGrade, isSelected)
					}
				)
			}
		}
	}
}