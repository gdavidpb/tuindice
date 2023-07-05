package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.base.ui.view.DropdownMenuItem
import com.gdavidpb.tuindice.base.ui.view.DropdownMenuTextField
import com.gdavidpb.tuindice.evaluations.R

data class SubjectDropdownMenuItem(
	val subjectId: String,
	override val text: String
) : DropdownMenuItem

@Composable
fun EvaluationSubjectTextField(
	modifier: Modifier = Modifier,
	subjects: List<SubjectDropdownMenuItem>,
	selectedSubject: SubjectDropdownMenuItem? = subjects.firstOrNull(),
	onSubjectChanged: (subject: SubjectDropdownMenuItem) -> Unit,
	error: String? = null
) {
	DropdownMenuTextField(
		modifier = modifier,
		label = stringResource(id = R.string.hint_evaluation_subject),
		items = subjects,
		selectedItem = selectedSubject,
		onItemSelected = onSubjectChanged,
		error = error,
		leadingIcon = {
			Icon(
				imageVector = Icons.Outlined.School,
				contentDescription = null
			)
		}
	)
}