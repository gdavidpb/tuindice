package com.gdavidpb.tuindice.assistant.ui.component.informe

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.model.RecordStatsItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardHistogramView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardSectionLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.toCardFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_record_histogram
import tuindice.assistant.generated.resources.assistant_card_a_record_label

// informe.estadisticas/1: the approved subjects as the figure, the other counts as rows, and the grade histogram as
// bars in the data colour, one bar per grade from 1 to 5.
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
			CardSectionLabelView(text = stringResource(Res.string.assistant_card_a_record_histogram))
			CardHistogramView(bars = item.histogram)
		}
	}
}
