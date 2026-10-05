package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Schedule
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.dialog_title_schedule

/**
 * The schedule sheet's title with the term it speaks of right under it, as one block, and the view
 * switch at its side once there is a schedule to draw.
 */
@Composable
fun ScheduleHeaderView(
	modifier: Modifier = Modifier,
	content: Schedule.State.Content?,
	onViewModeSelected: (ScheduleViewMode) -> Unit
) {
	Row(
		modifier = modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Section),
		verticalAlignment = Alignment.CenterVertically
	) {
		Column(
			modifier = Modifier.weight(1f),
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Two)
		) {
			Text(
				modifier = Modifier.testTag(RecordUiTags.ScheduleTitle),
				text = stringResource(Res.string.dialog_title_schedule),
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Bold
			)

			if (content != null) {
				Text(
					text = content.termName,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
		}

		if (content != null) {
			ScheduleViewSwitchView(
				selectedMode = content.viewMode,
				onModeSelected = onViewModeSelected
			)
		}
	}
}
