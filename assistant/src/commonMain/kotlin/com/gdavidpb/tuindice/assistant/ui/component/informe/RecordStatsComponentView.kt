package com.gdavidpb.tuindice.assistant.ui.component.informe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.model.RecordStatsItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardProgressView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.toCardFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_record_histogram
import tuindice.assistant.generated.resources.assistant_card_a_record_label

// informe.estadisticas/1: the approved subjects as the figure, the other counts as rows, and the grade histogram as
// bars in the accent color, one bar per grade from 1 to 5.
@Composable
fun RecordStatsComponentView(
	item: RecordStatsItem,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Report.recordStats(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_a_record_label))
		CardFiguresView(primary = item.approvedFigure.toCardFigure())
		item.counts.forEachIndexed { index, count ->
			CardRowView(
				name = count.label.asString(),
				showsDivider = index > 0,
				trailing = { CardValueView(text = count.value) }
			)
		}
		if (item.histogram.isNotEmpty()) {
			CardLabelView(text = stringResource(Res.string.assistant_card_a_record_histogram))
			item.histogram.forEach { bar ->
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						text = bar.grade,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
					CardProgressView(fraction = bar.fraction, modifier = Modifier.weight(1f))
					CardValueView(text = bar.count)
				}
			}
		}
	}
}
