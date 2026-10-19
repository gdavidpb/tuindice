package com.gdavidpb.tuindice.assistant.ui.component.informe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.presentation.mapper.toCardIndexText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardChipView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.toCardIndexFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.chatcontract.component.IndexSeriesComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_series_best
import tuindice.assistant.generated.resources.assistant_card_a_series_caption
import tuindice.assistant.generated.resources.assistant_card_a_series_cumulative_header
import tuindice.assistant.generated.resources.assistant_card_a_series_description
import tuindice.assistant.generated.resources.assistant_card_a_series_index_header
import tuindice.assistant.generated.resources.assistant_card_a_series_label
import tuindice.assistant.generated.resources.assistant_card_a_series_worst

// informe.serie/1: the cumulative index of the latest term as the figure, then a row per term with its index and its
// cumulative index. The highest and the lowest terms carry a chip.
@Composable
fun IndexSeriesComponentView(
	component: IndexSeriesComponent,
	modifier: Modifier = Modifier
) {
	val current = component.points.lastOrNull { point -> point.cumulativeIndex != null }?.cumulativeIndex

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Report.indexSeries(component.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_a_series_label))
		current?.let { index ->
			CardFiguresView(
				primary = index.toCardIndexFigure(
					caption = Res.string.assistant_card_a_series_caption,
					description = Res.string.assistant_card_a_series_description
				)
			)
		}
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
		component.points.forEachIndexed { index, point ->
			val mark = when (point.termId) {
				component.bestTermId -> Res.string.assistant_card_a_series_best
				component.worstTermId -> Res.string.assistant_card_a_series_worst
				else -> null
			}

			CardRowView(
				name = point.termLabel,
				showsDivider = index > 0,
				trailing = {
					Row(
						horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small),
						verticalAlignment = Alignment.CenterVertically
					) {
						mark?.let { text -> CardChipView(text = stringResource(text)) }
						Box(modifier = Modifier.width(valueColumnWidth), contentAlignment = Alignment.CenterEnd) {
							point.termIndex?.let { value -> CardValueView(text = value.toCardIndexText()) }
						}
						Box(modifier = Modifier.width(valueColumnWidth), contentAlignment = Alignment.CenterEnd) {
							point.cumulativeIndex?.let { value -> CardValueView(text = value.toCardIndexText()) }
						}
					}
				}
			)
		}
	}
}

// The width of each index column, so that the figures of every term line up under their headers.
private val valueColumnWidth = 68.dp
