package com.gdavidpb.tuindice.assistant.ui.component.materias

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.SubjectStatsItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardHistogramView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardSectionLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardTrailingView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.toCardFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_record_histogram
import tuindice.assistant.generated.resources.assistant_card_b_stats_difficulty
import tuindice.assistant.generated.resources.assistant_card_b_stats_median

// materias.estadisticas/1: the subject is the label, and the average is the figure, with the pass rate beside it. Then
// the difficulty as a chip, the median, and the grade histogram as bars, one per grade from 1 to 5.
@Composable
fun SubjectStatsComponentView(
	item: SubjectStatsItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Subjects.stats(item.id))) {
		CardLabelView(text = item.subjectName)
		item.primaryFigure?.let { primary ->
			CardFiguresView(primary = primary.toCardFigure(), secondary = item.secondaryFigure?.toCardFigure())
		}
		item.difficultyChip?.let { chip ->
			CardRowView(
				name = stringResource(Res.string.assistant_card_b_stats_difficulty),
				showsDivider = true,
				trailing = { CardTrailingView(value = null, chips = listOf(chip)) }
			)
		}
		item.medianText?.let { median ->
			CardRowView(
				name = stringResource(Res.string.assistant_card_b_stats_median),
				showsDivider = true,
				trailing = { CardValueView(text = median) }
			)
		}
		if (item.histogram.isNotEmpty()) {
			CardSectionLabelView(text = stringResource(Res.string.assistant_card_a_record_histogram))
			CardHistogramView(bars = item.histogram)
		}
		item.recordLink?.let { link -> CardLinkItemView(link = link, onAction = onAction) }
	}
}
