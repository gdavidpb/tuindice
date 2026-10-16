package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSectionItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_summary_open_statistics
import tuindice.summary.generated.resources.summary_statistics_index_suffix

// The card of the terms over. The whole card is one button that leads to the Statistics screen, so its line and its
// phrase are part of that button and take no touch of their own.
@Composable
fun SummaryStatisticsFiguresCard(
	item: SummaryStatisticsSectionItem.Figures,
	onClick: () -> Unit
) {
	SummaryHubCardView(
		modifier = Modifier
			.testTag(SummaryUiTags.StatisticsCard)
			.clickable(
				onClickLabel = stringResource(Res.string.a11y_summary_open_statistics),
				role = Role.Button,
				onClick = onClick
			)
	) {
		Column(
			modifier = Modifier.padding(TuIndiceSpacing.Screen),
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
		) {
			item.grade?.let { grade ->
				SummaryGradeFigureView(
					tag = SummaryUiTags.StatisticsFigureText,
					grade = grade,
					label = stringResource(Res.string.summary_statistics_index_suffix),
					style = MaterialTheme.typography.titleLarge
				)
			}

			if (item.line.isNotEmpty()) {
				SummaryStatisticsSparkline(values = item.line)
			}

			Text(
				modifier = Modifier.testTag(SummaryUiTags.StatisticsPhrase),
				text = item.phrase.asString(),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Deemphasis)
			)
		}
	}
}
