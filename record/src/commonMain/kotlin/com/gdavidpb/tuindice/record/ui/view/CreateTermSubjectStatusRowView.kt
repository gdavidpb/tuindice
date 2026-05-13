package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubjectAvailability
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_subject_already_planned
import tuindice.record.generated.resources.create_term_subject_already_taken
import tuindice.record.generated.resources.create_term_subject_requirement_pending
import tuindice.record.generated.resources.create_term_subject_selected

@Composable
internal fun CreateTermSubjectStatusRow(
	subject: SyntheticTermSubject,
	availableText: String,
	availableIcon: CreateTermSubjectStatusIcon
) {
	val status = subject.status(
		availableText = availableText,
		availableIcon = availableIcon
	)
	Row(
		horizontalArrangement = Arrangement.spacedBy(6.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		when (status.icon) {
			CreateTermSubjectStatusIcon.Dot ->
				Box(
					modifier = Modifier
						.size(10.dp)
						.background(status.color, CircleShape)
				)

			CreateTermSubjectStatusIcon.Check ->
				Icon(
					modifier = Modifier.size(16.dp),
					imageVector = Icons.Outlined.CheckCircleOutline,
					contentDescription = null,
					tint = status.color
				)

			CreateTermSubjectStatusIcon.Clock ->
				Icon(
					modifier = Modifier.size(16.dp),
					imageVector = Icons.Outlined.Schedule,
					contentDescription = null,
					tint = status.color
				)
		}
		Text(
			text = status.text,
			style = MaterialTheme.typography.bodySmall,
			color = status.color,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
	}
}

@Composable
private fun SyntheticTermSubject.status(
	availableText: String,
	availableIcon: CreateTermSubjectStatusIcon
): SubjectStatus {
	return when (availability) {
		SyntheticTermSubjectAvailability.AVAILABLE ->
			SubjectStatus(
				text = availableText,
				color = CreateTermSuccessColor,
				icon = availableIcon
			)

		SyntheticTermSubjectAvailability.SELECTED ->
			SubjectStatus(
				text = stringResource(Res.string.create_term_subject_selected),
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				icon = CreateTermSubjectStatusIcon.Dot
			)

		SyntheticTermSubjectAvailability.ALREADY_TAKEN ->
			SubjectStatus(
				text = stringResource(Res.string.create_term_subject_already_taken),
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				icon = CreateTermSubjectStatusIcon.Dot
			)

		SyntheticTermSubjectAvailability.ALREADY_PLANNED ->
			SubjectStatus(
				text = stringResource(Res.string.create_term_subject_already_planned),
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				icon = CreateTermSubjectStatusIcon.Dot
			)

		SyntheticTermSubjectAvailability.UNAVAILABLE ->
			SubjectStatus(
				text = stringResource(Res.string.create_term_subject_requirement_pending),
				color = CreateTermWarningColor,
				icon = CreateTermSubjectStatusIcon.Clock
			)
	}
}

private data class SubjectStatus(
	val text: String,
	val color: Color,
	val icon: CreateTermSubjectStatusIcon
)
