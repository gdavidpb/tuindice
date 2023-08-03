package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
			.fillMaxSize()
	) {
		EvaluationSubjectTextField(
			modifier = Modifier
				.fillMaxWidth(),
			subjects = subjectItems,
			selectedSubject = selectedSubject,
			onSubjectChanged = { subject ->
			}
		)

		EvaluationNameTextField(
			modifier = Modifier
				.fillMaxWidth(),
			name = state.name ?: "",
			onNameChanged = { name ->
			}
		)

		EvaluationGradeTextField(
			modifier = Modifier
				.fillMaxWidth(),
			grade = state.grade ?: 0.0,
			maxGrade = state.maxGrade ?: 0.0,
			onGradeChanged = { grade ->
			}
		)
	}
}