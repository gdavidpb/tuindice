package com.gdavidpb.tuindice.assistant.ui.component.informe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.TermItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardChipView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.toCardFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// informe.trimestre/1: the index of the term, with the cumulative index beside it, then a row per subject with its
// grade or, when it has none, its result. The card links to the report when it names its term.
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
		item.rows.forEachIndexed { index, row ->
			CardRowView(
				code = row.code,
				name = row.name,
				showsDivider = index > 0,
				trailing = {
					Row(
						horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small),
						verticalAlignment = Alignment.CenterVertically
					) {
						row.gradeText?.let { grade -> CardValueView(text = grade) }
						row.outcomeText?.let { text -> CardChipView(text = text.asString()) }
						row.badgeText?.let { text -> CardChipView(text = text.asString()) }
					}
				}
			)
		}
		item.recordLink?.let { link ->
			CardLinkView(
				text = link.text.asString(),
				onClick = { onAction(AssistantChat.Action.OpenNavTarget(link.target, link.entityRefs)) }
			)
		}
	}
}
