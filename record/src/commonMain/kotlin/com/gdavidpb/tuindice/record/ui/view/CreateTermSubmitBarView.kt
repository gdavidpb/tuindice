package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_button
import tuindice.record.generated.resources.edit_term_button
import tuindice.record.generated.resources.create_term_selected_count

@Composable
fun CreateTermSubmitBar(
	selectedCount: Int,
	canSubmit: Boolean,
	isEditing: Boolean,
	isSubmitting: Boolean,
	submitError: UiText,
	onCreateClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier.fillMaxWidth(),
		color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
		border = BorderStroke(
			width = 1.dp,
			color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)
		),
		tonalElevation = 6.dp
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 20.dp, vertical = 14.dp),
		) {
			if (submitError != UiText.Empty) {
				Text(
					modifier = Modifier
						.fillMaxWidth()
						.padding(bottom = 10.dp)
						.testTag(RecordUiTags.CreateSyntheticTermSubmitError),
					text = submitError.asString(),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.error
				)
			}
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(16.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					modifier = Modifier
						.weight(1f)
						.widthIn(min = 148.dp),
					text = stringResource(Res.string.create_term_selected_count, selectedCount),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
				Button(
					modifier = Modifier
						.width(162.dp)
						.height(52.dp)
						.testTag(RecordUiTags.CreateSyntheticTermSubmitButton),
					enabled = canSubmit,
					onClick = onCreateClick,
					shape = RoundedCornerShape(999.dp)
				) {
					if (isSubmitting) {
						CircularProgressIndicator(
							modifier = Modifier
								.testTag(RecordUiTags.CreateSyntheticTermSubmitProgress)
								.size(20.dp),
							color = MaterialTheme.colorScheme.onSurfaceVariant,
							strokeWidth = 2.dp
						)
					} else {
						Text(
							text = stringResource(
								if (isEditing) Res.string.edit_term_button else Res.string.create_term_button
							),
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
						)
					}
				}
			}
		}
	}
}
