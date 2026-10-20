package com.gdavidpb.tuindice.assistant.ui.component.informe

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.model.PaceItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.toCardFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_pace_empty
import tuindice.assistant.generated.resources.assistant_card_a_pace_label
import tuindice.assistant.generated.resources.assistant_card_a_pace_remaining

// informe.ritmo/1: the average of UC per term as the figure, then the UC of each term and, when the service estimates
// them, the terms left. With no term, a line says so.
@Composable
fun PaceComponentView(
	item: PaceItem,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Report.pace(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_a_pace_label))
		CardFiguresView(primary = item.averageFigure.toCardFigure())
		if (item.terms.isEmpty()) {
			CardBodyTextView(text = stringResource(Res.string.assistant_card_a_pace_empty), muted = true)
		}
		item.terms.forEachIndexed { index, term ->
			CardRowView(
				name = term.label,
				showsDivider = index > 0,
				trailing = { CardValueView(text = term.credits.asString()) }
			)
		}
		item.remainingTerms?.let { terms ->
			CardRowView(
				name = stringResource(Res.string.assistant_card_a_pace_remaining),
				showsDivider = item.terms.isNotEmpty(),
				trailing = { CardValueView(text = terms) }
			)
		}
	}
}
