package com.gdavidpb.tuindice.wizard.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.wizard.presentation.model.Coachmark
import com.gdavidpb.tuindice.wizard.ui.CoachmarkUiTags
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tuindice.wizard.generated.resources.Res
import tuindice.wizard.generated.resources.coachmark_back
import tuindice.wizard.generated.resources.coachmark_confirm
import tuindice.wizard.generated.resources.coachmark_next

@Composable
fun CoachmarkBubble(
	coachmark: Coachmark,
	hasPreviousCoachmark: Boolean,
	hasNextCoachmark: Boolean,
	onPreviousActionClick: () -> Unit,
	onPrimaryActionClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier.testTag(CoachmarkUiTags.Bubble),
		shape = RoundedCornerShape(TuIndiceRadius.Medium),
		color = MaterialTheme.colorScheme.surface,
		tonalElevation = 4.dp,
		shadowElevation = 8.dp
	) {
		Column(
			verticalArrangement = Arrangement.spacedBy(10.dp),
			modifier = Modifier
				.widthIn(min = 220.dp, max = 320.dp)
				.padding(16.dp)
		) {
			Column(
				modifier = Modifier.widthIn(min = 220.dp, max = 320.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Text(
					modifier = Modifier.testTag(CoachmarkUiTags.currentCoachmark(coachmark.id)),
					text = stringResource(coachmark.title),
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.SemiBold,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)

				Text(
					text = stringResource(coachmark.message),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 4,
					overflow = TextOverflow.Ellipsis
				)
			}

			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = if (hasPreviousCoachmark) {
					Arrangement.SpaceBetween
				} else {
					Arrangement.End
				}
			) {
				if (hasPreviousCoachmark) {
					TextButton(
						modifier = Modifier.testTag(CoachmarkUiTags.BackButton),
						shape = RoundedCornerShape(TuIndiceRadius.Medium),
						contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
						onClick = onPreviousActionClick
					) {
						Text(
							text = stringResource(Res.string.coachmark_back),
							maxLines = 1
						)
					}
				}

				Button(
					modifier = Modifier.testTag(CoachmarkUiTags.ConfirmButton),
					shape = RoundedCornerShape(TuIndiceRadius.Medium),
					contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
					onClick = onPrimaryActionClick
				) {
					Text(
						text = stringResource(coachmarkPrimaryActionText(hasNextCoachmark)),
						maxLines = 1
					)
				}
			}
		}
	}
}

private fun coachmarkPrimaryActionText(hasNextCoachmark: Boolean): StringResource {
	return if (hasNextCoachmark) {
		Res.string.coachmark_next
	} else {
		Res.string.coachmark_confirm
	}
}
