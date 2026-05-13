package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import tuindice.record.generated.resources.create_term_subject_requirements_met

@Composable
internal fun CreateTermSelectedSubjectCard(
	subject: SyntheticTermSubject,
	action: CreateTermSubjectCardAction,
	enabled: Boolean = true,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier
			.fillMaxWidth()
			.testTag(RecordUiTags.createSyntheticTermSubject(subject.subjectCode)),
		shape = RoundedCornerShape(14.dp),
		color = MaterialTheme.colorScheme.surface.copy(alpha = 0.56f),
		border = BorderStroke(
			width = 1.dp,
			color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 14.dp, vertical = 14.dp),
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Column(modifier = Modifier.weight(1f)) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(12.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					CreateTermSubjectCodeChip(subjectCode = subject.subjectCode)
					Text(
						modifier = Modifier.weight(1f),
						text = subject.name,
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onSurface,
						maxLines = 2,
						overflow = TextOverflow.Ellipsis
					)
				}
				Spacer(modifier = Modifier.height(8.dp))
				Text(
					text = subject.creditsText,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				Spacer(modifier = Modifier.height(8.dp))
				CreateTermSubjectStatusRow(
					subject = subject,
					availableText = stringResource(Res.string.create_term_subject_requirements_met),
					availableIcon = CreateTermSubjectStatusIcon.Check
				)
			}
			if (action == CreateTermSubjectCardAction.Remove || enabled) {
				CreateTermSubjectActionButton(
					action = action,
					enabled = enabled,
					onClick = onClick
				)
			}
		}
	}
}
