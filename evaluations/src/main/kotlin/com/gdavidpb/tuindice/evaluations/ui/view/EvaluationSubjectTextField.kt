package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.ui.view.DropdownMenuItem
import com.gdavidpb.tuindice.base.ui.view.DropdownMenuTextField

data class SubjectDropdownMenuItem(
	val subjectId: String,
	override val text: String
) : DropdownMenuItem

@Composable
fun EvaluationSubjectTextField(
	modifier: Modifier = Modifier,
	subjects: List<Subject>,
	selectedSubject: Subject? = subjects.firstOrNull(),
	onSubjectChanged: (subjectId: String) -> Unit,
	error: String? = null
) {
	val subjectItems = subjects
		.map { subject ->
			SubjectDropdownMenuItem(
				subjectId = subject.id,
				text = "${subject.code} — ${subject.name}"
			)
		}

	val selectedSubjectItem = subjectItems.find { subject ->
		subject.subjectId == selectedSubject?.id
	}

	DropdownMenuTextField(
		modifier = modifier,
		items = subjectItems,
		selectedItem = selectedSubjectItem,
		onItemSelected = { item -> onSubjectChanged(item.subjectId) },
		error = error,
		leadingIcon = {
			Icon(
				imageVector = Icons.Outlined.School,
				contentDescription = null
			)
		}
	)
}