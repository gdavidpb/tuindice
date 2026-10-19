package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.TermReportDeltaItem
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_index_missing
import tuindice.summary.generated.resources.term_report_index_scale

// The index of the term in large, with the scale beside it, or the words that say it has none yet, and its change: the
// chip holds the arrow and the figure, and the term the change is measured from is written under the index.
@Composable
fun TermReportIndexRow(
	index: String?,
	indexDelta: TermReportDeltaItem?
) {
	// The index takes all the width the chip leaves: a spacer sharing that width with it squeezed the scale into a
	// column of single letters.
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		Column(modifier = Modifier.weight(1f)) {
			Row(
				verticalAlignment = Alignment.Bottom,
				horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
			) {
				if (index != null) {
					Text(
						modifier = Modifier.testTag(TermReportUiTags.TermReportIndex),
						text = index,
						style = MaterialTheme.typography.displaySmall,
						color = MaterialTheme.colorScheme.onSurface
					)

					Text(
						modifier = Modifier.padding(bottom = TuIndiceSpacing.Small),
						text = stringResource(Res.string.term_report_index_scale),
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						maxLines = 1,
						softWrap = false
					)
				} else {
					Text(
						modifier = Modifier.testTag(TermReportUiTags.TermReportIndex),
						text = stringResource(Res.string.term_report_index_missing),
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}

			indexDelta?.reference?.let { reference ->
				Text(
					modifier = Modifier.testTag(TermReportUiTags.TermReportIndexReference),
					text = reference.asString(),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1
				)
			}
		}

		indexDelta?.let { delta ->
			TermReportDeltaChip(
				delta = delta,
				modifier = Modifier.testTag(TermReportUiTags.TermReportIndexDelta)
			)
		}
	}
}
