package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumModalityItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumOptionItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.pensum.ui.view.PensumElementShape
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_selection_active_title
import tuindice.pensum.generated.resources.pensum_selection_progress_summary
import tuindice.pensum.generated.resources.pensum_summary_pensum_label

@Composable
fun PensumCurrentSelectionSummary(
	model: PensumScreenModel,
	currentPensum: PensumOptionItem,
	currentModality: PensumModalityItem
) {
	val pensumLabel = stringResource(Res.string.pensum_summary_pensum_label)
	val title = model.careerName.ifBlank { "$pensumLabel ${currentPensum.year}" }
	val subtitle = listOf(
		"$pensumLabel ${currentPensum.year}",
		currentModality.name
	)
		.filter(String::isNotBlank)
		.joinToString(separator = " · ")

	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.testTag(PensumUiTags.PensumCurrentSelectionSummary),
		shape = PensumElementShape,
		color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
		border = BorderStroke(
			width = 1.dp,
			color = MaterialTheme.colorScheme.outlineVariant
		)
	) {
		Column(
			modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
			verticalArrangement = Arrangement.spacedBy(4.dp)
		) {
			Text(
				text = stringResource(Res.string.pensum_selection_active_title),
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.SemiBold,
				color = MaterialTheme.colorScheme.primary
			)
			Text(
				text = title,
				style = MaterialTheme.typography.titleSmall,
				fontWeight = FontWeight.SemiBold,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis
			)
			Text(
				text = subtitle,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			Text(
				text = stringResource(
					Res.string.pensum_selection_progress_summary,
					"${model.progressPercent}%",
					model.approvedCredits,
					model.totalCredits
				),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}
