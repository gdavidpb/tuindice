package com.gdavidpb.tuindice.assistant.ui.component.informe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.presentation.model.IndexPointItem
import com.gdavidpb.tuindice.assistant.presentation.model.IndexSeriesItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardChipView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.toCardFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_series_cumulative_header
import tuindice.assistant.generated.resources.assistant_card_a_series_index_header
import tuindice.assistant.generated.resources.assistant_card_a_series_label

// informe.serie/1: the cumulative index of the latest term as the figure, then a row per term with its index and its
// cumulative index. The highest and the lowest terms carry a chip. A conversation draws the latest terms, up to its row
// limit.
@Composable
fun IndexSeriesComponentView(
	item: IndexSeriesItem,
	rowLimit: Int?,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Report.indexSeries(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_a_series_label))
		item.currentFigure?.let { figure -> CardFiguresView(primary = figure.toCardFigure()) }
		Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
			Text(
				text = stringResource(Res.string.assistant_card_a_series_index_header),
				modifier = Modifier.width(valueColumnWidth),
				textAlign = TextAlign.End,
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Spacer(modifier = Modifier.width(TuIndiceSpacing.Small))
			Text(
				text = stringResource(Res.string.assistant_card_a_series_cumulative_header),
				modifier = Modifier.width(valueColumnWidth),
				textAlign = TextAlign.End,
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
		// The series runs from the oldest term to the newest, so a cut keeps the last of its terms.
		val points = rowLimit?.let { limit -> item.points.takeLast(limit) } ?: item.points
		points.forEachIndexed { index, point ->
			IndexSeriesRowView(point = point, showsDivider = index > 0)
		}
	}
}

// One term of the series: its name, and on the right its mark and its two index columns. The columns keep their fixed
// width, so the row gives its figures the room they need and the name keeps the rest. The row is the card's own, not
// the kit's, until the series is redesigned with the text sizes.
@Composable
private fun IndexSeriesRowView(
	point: IndexPointItem,
	showsDivider: Boolean
) {
	if (showsDivider) {
		HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
		Spacer(modifier = Modifier.height(TuIndiceSpacing.Small))
	}

	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = point.termLabel,
			modifier = Modifier.weight(1f),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			maxLines = 2
		)

		Row(
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small),
			verticalAlignment = Alignment.CenterVertically
		) {
			point.markChip?.let { chip -> CardChipView(text = chip.text.asString(), tone = chip.tone) }
			Box(modifier = Modifier.width(valueColumnWidth), contentAlignment = Alignment.CenterEnd) {
				point.termIndexText?.let { value -> CardValueView(text = value) }
			}
			Box(modifier = Modifier.width(valueColumnWidth), contentAlignment = Alignment.CenterEnd) {
				point.cumulativeIndexText?.let { value -> CardValueView(text = value) }
			}
		}
	}
}

// The width of each index column, so that the figures of every term line up under their headers.
private val valueColumnWidth = 68.dp
