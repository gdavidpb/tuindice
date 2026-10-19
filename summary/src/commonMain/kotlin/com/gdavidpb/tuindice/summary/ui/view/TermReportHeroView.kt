package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.TermReportDeltaItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportHeroItem
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_cumulative
import tuindice.summary.generated.resources.term_report_index_missing
import tuindice.summary.generated.resources.term_report_index_scale

/**
 * The figures of the term: its index in large, the change from the term before, and under a divider the cumulative
 * index with its change. Each figure is read from the record; the words only name it.
 */
@Composable
fun TermReportHeroView(
	hero: TermReportHeroItem,
	modifier: Modifier = Modifier
) {
	SummaryHubCardView(
		modifier = modifier
			.fillMaxWidth()
			.testTag(TermReportUiTags.TermReportHero)
	) {
		Column(
			modifier = Modifier.padding(TuIndiceSpacing.Screen),
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
		) {
			Text(
				text = hero.title.asString(),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)

			TermReportIndexRow(index = hero.index, indexDelta = hero.indexDelta)

			HorizontalDivider(
				modifier = Modifier.padding(vertical = TuIndiceSpacing.XSmall),
				thickness = TuIndiceSpacing.Hairline,
				color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = TuIndiceAlpha.Deemphasis)
			)

			TermReportCumulativeRow(
				cumulativeIndex = hero.cumulativeIndex,
				cumulativeDelta = hero.cumulativeDelta
			)
		}
	}
}

// The index of the term in large, with the scale beside it, or the words that say it has none yet, and its change: the
// chip holds the arrow and the figure, and the term the change is measured from is written under the index.
@Composable
private fun TermReportIndexRow(
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

// The cumulative index with its change, on one line.
@Composable
private fun TermReportCumulativeRow(
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
