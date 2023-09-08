package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.ui.view.DropdownMenuItem
import com.gdavidpb.tuindice.base.ui.view.DropdownMenuTextField
import com.gdavidpb.tuindice.evaluations.R

data class SubjectDropdownMenuItem(
	val subject: Subject,
	override val text: String
) : DropdownMenuItem

@Composable
fun EvaluationSubjectTextField(
	modifier: Modifier = Modifier,
	subjects: List<Subject>,
	selectedSubject: Subject? = subjects.firstOrNull(),
	onSubjectChange: (subject: Subject) -> Unit,
	error: String? = null
) {
	val subjectItems = subjects
		.map { subject ->
			SubjectDropdownMenuItem(
				subject = subject,
				text = "${subject.code} — ${subject.name}"
			)
		}

	val selectedSubjectItem = subjectItems.find { subject ->
		subject.subject.id == selectedSubject?.id
	}

	DropdownMenuTextField(
		modifier = modifier,
		items = subjectItems,
		selectedItem = selectedSubjectItem,
		onItemSelected = { item -> onSubjectChange(item.subject) },
		placeholder = {
			Text(
				text = stringResource(id = R.string.hint_evaluation_subject)
			)
		},
		leadingIcon = {
			Icon(
				imageVector = Icons.Outlined.School,
				contentDescription = null
			)
		},
		error = error
	)
}