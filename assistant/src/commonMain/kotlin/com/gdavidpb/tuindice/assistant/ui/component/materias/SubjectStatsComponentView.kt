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
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardChipView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFigure
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardProgressView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRecordLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.bandText
import com.gdavidpb.tuindice.assistant.ui.model.chatCardFraction
import com.gdavidpb.tuindice.assistant.ui.model.chatCardPercentText
import com.gdavidpb.tuindice.assistant.ui.model.toCardCompactNumberText
import com.gdavidpb.tuindice.assistant.ui.model.toCardPercent
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.chatcontract.component.SubjectStatsComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_stats_average_caption
import tuindice.assistant.generated.resources.assistant_card_b_stats_average_description
import tuindice.assistant.generated.resources.assistant_card_b_stats_difficulty
import tuindice.assistant.generated.resources.assistant_card_b_stats_histogram
import tuindice.assistant.generated.resources.assistant_card_b_stats_median
import tuindice.assistant.generated.resources.assistant_card_b_stats_pass_caption
import tuindice.assistant.generated.resources.assistant_card_b_stats_pass_description

// materias.estadisticas/1: the subject is the label, and the average is the figure, with the pass rate beside it. Then
// the difficulty as a chip, the median, and the grade histogram as bars, one per grade from 1 to 5, as the record stats
// card draws its own.
@Composable
fun SubjectStatsComponentView(
	component: SubjectStatsComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val averageFigure = component.average?.toCardCompactNumberText()?.let { average ->
		CardFigure(
			value = average,
			caption = stringResource(Res.string.assistant_card_b_stats_average_caption),
			contentDescription = stringResource(Res.string.assistant_card_b_stats_average_description, average)
		)
	}
	val passFigure = component.passRate?.toCardPercent()?.let { percent ->
		val rate = chatCardPercentText(percent).asString()

		CardFigure(
			value = rate,
			caption = stringResource(Res.string.assistant_card_b_stats_pass_caption),
			contentDescription = stringResource(Res.string.assistant_card_b_stats_pass_description, rate)
		)
	}
	val largest = component.histogram.maxOrNull() ?: 0

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Subjects.stats(component.id))) {
		CardLabelView(text = component.subjectName)
		(averageFigure ?: passFigure)?.let { primary ->
			CardFiguresView(primary = primary, secondary = if (averageFigure != null) passFigure else null)
		}
		bandText(component.difficulty)?.let { difficulty ->
			CardRowView(
				name = stringResource(Res.string.assistant_card_b_stats_difficulty),
				trailing = { CardChipView(text = difficulty.asString()) }
			)
		}
		component.median?.let { median ->
			CardRowView(
				name = stringResource(Res.string.assistant_card_b_stats_median),
				trailing = { CardValueView(text = median.toCardCompactNumberText()) }
			)
		}
		if (component.histogram.isNotEmpty()) {
			CardLabelView(text = stringResource(Res.string.assistant_card_b_stats_histogram))
			component.histogram.forEachIndexed { index, count ->
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						text = (index + 1).toString(),
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
					CardProgressView(
						fraction = chatCardFraction(count.toDouble(), largest.toDouble()),
						modifier = Modifier.weight(1f)
					)
					CardValueView(text = count.toString())
				}
			}
		}
		CardRecordLinkView(entityRefs = component.entityRefs, onAction = onAction)
	}
}
