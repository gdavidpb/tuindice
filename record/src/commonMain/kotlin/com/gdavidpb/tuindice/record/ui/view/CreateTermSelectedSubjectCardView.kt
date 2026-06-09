package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.view.SubjectResultCard
import com.gdavidpb.tuindice.record.presentation.model.CreateTermSubjectItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.CreateTermSubjectCardAction
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_subject_available

@Composable
fun CreateTermSelectedSubjectCard(
	subject: CreateTermSubjectItem,
	action: CreateTermSubjectCardAction,
	enabled: Boolean = true,
	onClick: () -> Unit,
	onStatsClick: ((String) -> Unit)? = null,
	modifier: Modifier = Modifier
) {
	SubjectResultCard(
		subjectCode = subject.subjectCode,
		nameText = subject.nameText,
		creditsText = subject.creditsText,
		modifier = modifier,
		containerTestTag = RecordUiTags.createSyntheticTermSubject(subject.subjectCode),
		statusContent = {
			CreateTermSubjectStatusRow(
				subject = subject,
				availableText = stringResource(Res.string.create_term_subject_available),
				availableIcon = CreateTermSubjectStatusIcon.Available
			)
		},
		trailingContent = {
			if (action == CreateTermSubjectCardAction.Remove || enabled || onStatsClick != null) {
				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					if (action == CreateTermSubjectCardAction.Remove || enabled) {
						CreateTermSubjectActionButton(
							action = action,
							enabled = enabled,
							onClick = onClick,
							testTag = RecordUiTags.createSyntheticTermSubjectAction(
								subjectCode = subject.subjectCode,
								action = action.name.lowercase()
							)
						)
					}
					if (onStatsClick != null) {
						CreateTermSubjectStatsButton(
							subjectCode = subject.subjectCode,
							onClick = { onStatsClick(subject.subjectCode) }
						)
					}
				}
			}
		}
	)
}
