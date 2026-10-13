package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_evaluation_legend

/**
 * What the evaluation icon of the schedule means, said once under the rows of the table or under the
 * grid: "Evaluación esta semana".
 */
@Composable
fun ScheduleEvaluationLegendView(
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier.testTag(RecordUiTags.ScheduleEvaluationLegend),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			modifier = Modifier.size(TuIndiceComponentSizes.IconSmall),
			imageVector = Icons.Outlined.Assignment,
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onSurfaceVariant
		)

		Text(
			text = stringResource(Res.string.schedule_evaluation_legend),
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}
