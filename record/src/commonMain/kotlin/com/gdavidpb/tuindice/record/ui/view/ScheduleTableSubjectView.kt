package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.model.SubjectCodeChipVariant
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.SubjectCodeChip
import com.gdavidpb.tuindice.record.presentation.model.ScheduleTableRowItem

/**
 * The subject of a row of the schedule table: "CI5311" over "Sec. 1 · MYS-116", or only its code
 * when there is no detail to show.
 */
@Composable
fun ScheduleTableSubjectView(
	modifier: Modifier = Modifier,
	row: ScheduleTableRowItem
) {
	Column(
		modifier = modifier,
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
	) {
		// The chip every other screen names a subject with, in its own colour.
		SubjectCodeChip(
			subjectCode = row.subjectCode,
			variant = SubjectCodeChipVariant.Dense
		)

		if (row.detailText != null) {
			Text(
				text = row.detailText.asString(),
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}
