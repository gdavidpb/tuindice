package com.gdavidpb.tuindice.assistant.ui.component.materias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.SubjectStatsItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardChipView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
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
		item.difficultyText?.let { difficulty ->
			CardRowView(
				name = stringResource(Res.string.assistant_card_b_stats_difficulty),
				trailing = { CardChipView(text = difficulty.asString()) }
			)
		}
		item.medianText?.let { median ->
			CardRowView(
				name = stringResource(Res.string.assistant_card_b_stats_median),
				trailing = { CardValueView(text = median) }
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
		item.recordLink?.let { link -> CardLinkItemView(link = link, onAction = onAction) }
	}
}
