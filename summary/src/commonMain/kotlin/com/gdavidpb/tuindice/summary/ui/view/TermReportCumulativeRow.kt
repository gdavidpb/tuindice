package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.TermReportDeltaItem
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_cumulative

// The cumulative index with its change, on one line.
@Composable
fun TermReportCumulativeRow(
	cumulativeIndex: String?,
	cumulativeDelta: TermReportDeltaItem?
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		Text(
			text = stringResource(Res.string.term_report_cumulative),
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		Spacer(modifier = Modifier.weight(1f))

		cumulativeIndex?.let { cumulative ->
			Text(
				modifier = Modifier.testTag(TermReportUiTags.TermReportCumulativeIndex),
				text = cumulative,
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onSurface
			)
		}

		cumulativeDelta?.let { delta ->
			TermReportDeltaChip(
				delta = delta,
				modifier = Modifier.testTag(TermReportUiTags.TermReportCumulativeDelta)
			)
		}
	}
}
