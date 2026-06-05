package com.gdavidpb.tuindice.record.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.presentation.model.TermItemKind
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.term_selection_kind_current
import tuindice.record.generated.resources.term_selection_kind_historical
import tuindice.record.generated.resources.term_selection_kind_projection

@Composable
fun TermSelectionTermRowView(
	term: TermItem,
	isSelected: Boolean,
	selectedContentDescription: String,
	onClick: () -> Unit
) {
	val rowShape = RoundedCornerShape(8.dp)
	val containerColor = if (isSelected) {
		MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
	} else {
		MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f)
	}
	val kindText = when (term.kind) {
		TermItemKind.SYNTHETIC -> stringResource(Res.string.term_selection_kind_projection)
		TermItemKind.CURRENT -> stringResource(Res.string.term_selection_kind_current)
		TermItemKind.HISTORICAL -> stringResource(Res.string.term_selection_kind_historical)
	}

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(rowShape)
			.background(containerColor)
			.clickable(onClick = onClick)
			.testTag(RecordUiTags.termSelectionOption(term.termId))
			.padding(12.dp),
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		if (isSelected) {
			Icon(
				modifier = Modifier
					.size(24.dp)
					.testTag(RecordUiTags.termSelectionSelectedIcon(term.termId)),
				imageVector = Icons.Outlined.CheckCircleOutline,
				contentDescription = selectedContentDescription,
				tint = MaterialTheme.colorScheme.primary
			)
		} else {
			Spacer(modifier = Modifier.size(24.dp))
		}

		Column(
			modifier = Modifier.weight(1f),
			verticalArrangement = Arrangement.spacedBy(6.dp)
		) {
			Text(
				text = term.shortNameText,
				style = MaterialTheme.typography.titleMedium,
				fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)

			Row(
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Box(
					modifier = Modifier
						.clip(RoundedCornerShape(8.dp))
						.background(MaterialTheme.colorScheme.secondaryContainer)
						.testTag(RecordUiTags.termSelectionKind(term.termId))
						.padding(
							horizontal = 8.dp,
							vertical = 3.dp
						)
				) {
					Text(
						text = kindText,
						style = MaterialTheme.typography.labelSmall,
						fontWeight = FontWeight.SemiBold,
						color = MaterialTheme.colorScheme.onSecondaryContainer
					)
				}

				Text(
					text = term.gradeSumText,
					style = MaterialTheme.typography.labelMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)

				Text(
					text = term.creditsText,
					style = MaterialTheme.typography.labelMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
		}
	}
}
