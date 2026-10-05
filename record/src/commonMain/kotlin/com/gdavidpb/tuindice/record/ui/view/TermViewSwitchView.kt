package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.term_view_grades
import tuindice.record.generated.resources.term_view_schedule

/** Notas | Horario: what the current term's page shows below the summary. */
@Composable
fun TermViewSwitchView(
	modifier: Modifier = Modifier,
	isScheduleSelected: Boolean,
	onScheduleSelectedChange: (Boolean) -> Unit
) {
	val colors = SegmentedButtonDefaults.colors(
		activeContainerColor = MaterialTheme.colorScheme.primary,
		activeContentColor = MaterialTheme.colorScheme.onPrimary,
		activeBorderColor = MaterialTheme.colorScheme.primary,
		inactiveContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.48f),
		inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
		inactiveBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = TuIndiceAlpha.Muted)
	)

	SingleChoiceSegmentedButtonRow(
		modifier = modifier
			.fillMaxWidth()
			.testTag(RecordUiTags.TermViewSwitch)
	) {
		SegmentedButton(
			modifier = Modifier.testTag(RecordUiTags.TermViewGradesTab),
			selected = !isScheduleSelected,
			onClick = { onScheduleSelectedChange(false) },
			shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
			colors = colors,
			icon = {}
		) {
			Text(
				text = stringResource(Res.string.term_view_grades),
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}

		SegmentedButton(
			modifier = Modifier.testTag(RecordUiTags.TermViewScheduleTab),
			selected = isScheduleSelected,
			onClick = { onScheduleSelectedChange(true) },
			shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
			colors = colors,
			icon = {}
		) {
			Text(
				text = stringResource(Res.string.term_view_schedule),
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}
