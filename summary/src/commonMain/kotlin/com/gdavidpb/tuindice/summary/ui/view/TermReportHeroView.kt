package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.TermReportHeroItem
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags

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
