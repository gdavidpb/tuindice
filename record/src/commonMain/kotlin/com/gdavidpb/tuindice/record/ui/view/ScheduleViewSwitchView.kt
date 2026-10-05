package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarViewWeek
import androidx.compose.material.icons.outlined.TableRows
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_view_table
import tuindice.record.generated.resources.schedule_view_week

/**
 * Tabla | Semana: how the schedule below is drawn. Two icons that sit next to the sheet's title
 * instead of a row of their own; each one is read aloud by the name of its view.
 */
@Composable
fun ScheduleViewSwitchView(
	modifier: Modifier = Modifier,
	selectedMode: ScheduleViewMode,
	onModeSelected: (ScheduleViewMode) -> Unit
) {
	val modes = ScheduleViewMode.entries
	val colors = SegmentedButtonDefaults.colors(
		activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
		activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
		activeBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = TuIndiceAlpha.Muted),
		inactiveContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.48f),
		inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
		inactiveBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = TuIndiceAlpha.Muted)
	)

	SingleChoiceSegmentedButtonRow(
		modifier = modifier.testTag(RecordUiTags.ScheduleViewSwitch)
	) {
		modes.forEachIndexed { index, mode ->
			SegmentedButton(
				modifier = Modifier.testTag(mode.testTag),
				selected = selectedMode == mode,
				onClick = { onModeSelected(mode) },
				shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
				colors = colors,
				icon = {}
			) {
				Icon(
					imageVector = mode.icon,
					contentDescription = stringResource(mode.labelResource)
				)
			}
		}
	}
}

private val ScheduleViewMode.labelResource: StringResource
	get() = when (this) {
		ScheduleViewMode.Table -> Res.string.schedule_view_table
		ScheduleViewMode.Week -> Res.string.schedule_view_week
	}

private val ScheduleViewMode.icon: ImageVector
	get() = when (this) {
		ScheduleViewMode.Table -> Icons.Outlined.TableRows
		ScheduleViewMode.Week -> Icons.Outlined.CalendarViewWeek
	}

private val ScheduleViewMode.testTag: String
	get() = when (this) {
		ScheduleViewMode.Table -> RecordUiTags.ScheduleViewTableTab
		ScheduleViewMode.Week -> RecordUiTags.ScheduleViewWeekTab
	}
