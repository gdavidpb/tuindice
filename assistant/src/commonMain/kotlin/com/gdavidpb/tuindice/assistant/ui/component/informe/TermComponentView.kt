package com.gdavidpb.tuindice.assistant.ui.component.informe

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.TermItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardTrailingView
import com.gdavidpb.tuindice.assistant.ui.model.toCardFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_term_empty

// informe.trimestre/1: the index of the term, with the cumulative index beside it, then a row per subject with its
// grade or, when it has none, the chip of its result or a dash, and the chip of its badge. The card links to the report
// when it names its term. With no subject, a line says so.
@Composable
fun TermComponentView(
	item: TermItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Report.term(item.id))) {
		CardLabelView(text = item.termLabel)
		item.primaryFigure?.let { primary ->
			CardFiguresView(primary = primary.toCardFigure(), secondary = item.secondaryFigure?.toCardFigure())
		}
		if (item.rows.isEmpty()) {
			CardBodyTextView(text = stringResource(Res.string.assistant_card_a_term_empty), muted = true)
		}
		item.rows.forEachIndexed { index, row ->
			CardRowView(
				code = row.code,
				name = row.name,
				showsDivider = index > 0,
				trailing = {
					CardTrailingView(
						value = row.valueText?.asString(),
						chips = listOfNotNull(row.outcomeChip, row.badgeChip)
					)
				}
			)
		}
		item.recordLink?.let { link -> CardLinkItemView(link = link, onAction = onAction) }
	}
}
