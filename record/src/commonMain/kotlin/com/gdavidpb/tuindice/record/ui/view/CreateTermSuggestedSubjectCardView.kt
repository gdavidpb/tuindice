package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.CreateTermSubjectCardAction
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_subject_available

@Composable
fun CreateTermSuggestedSubjectCard(
	subject: SyntheticTermSubject,
	enabled: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier
			.width(144.dp)
			.height(166.dp)
			.testTag(RecordUiTags.createSyntheticTermSubject(subject.subjectCode)),
		shape = RoundedCornerShape(14.dp),
		color = MaterialTheme.colorScheme.surface.copy(alpha = 0.56f),
		border = BorderStroke(
			width = 1.dp,
			color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)
		)
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(12.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.Top
			) {
				CreateTermSubjectCodeChip(subjectCode = subject.subjectCode)
				CreateTermSubjectActionButton(
					action = CreateTermSubjectCardAction.Add,
					enabled = enabled,
					onClick = onClick,
					testTag = RecordUiTags.createSyntheticTermSubjectAction(
						subjectCode = subject.subjectCode,
						action = CreateTermSubjectCardAction.Add.name.lowercase()
					)
				)
			}
			Spacer(modifier = Modifier.height(18.dp))
			Text(
				text = subject.name,
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onSurface,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis
			)
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = subject.creditsText,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Spacer(modifier = Modifier.weight(1f))
			CreateTermSubjectStatusRow(
				subject = subject,
				availableText = stringResource(Res.string.create_term_subject_available),
				availableIcon = CreateTermSubjectStatusIcon.Dot
			)
		}
	}
}
