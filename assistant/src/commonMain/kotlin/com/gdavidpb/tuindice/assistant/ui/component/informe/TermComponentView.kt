package com.gdavidpb.tuindice.assistant.ui.component.informe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.attemptBadgeText
import com.gdavidpb.tuindice.assistant.presentation.mapper.attemptOutcomeText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardChipView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.model.toCardIndexFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.TermComponent
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_cumulative_caption
import tuindice.assistant.generated.resources.assistant_card_a_cumulative_description
import tuindice.assistant.generated.resources.assistant_card_a_link_record
import tuindice.assistant.generated.resources.assistant_card_a_term_index_caption
import tuindice.assistant.generated.resources.assistant_card_a_term_index_description

// informe.trimestre/1: the index of the term, with the cumulative index beside it, then a row per subject with its
// grade or, when it has none, its result. The card links to the report when it names its term.
@Composable
fun TermComponentView(
	component: TermComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val termIndex = component.termIndex?.toCardIndexFigure(
		caption = Res.string.assistant_card_a_term_index_caption,
		description = Res.string.assistant_card_a_term_index_description
	)
	val cumulative = component.cumulativeIndex?.toCardIndexFigure(
		caption = Res.string.assistant_card_a_cumulative_caption,
		description = Res.string.assistant_card_a_cumulative_description
	)

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Report.term(component.id))) {
		CardLabelView(text = component.termLabel)
		(termIndex ?: cumulative)?.let { primary ->
			CardFiguresView(primary = primary, secondary = if (termIndex != null) cumulative else null)
		}
		component.rows.forEachIndexed { index, row ->
			CardRowView(
				code = row.subjectCode,
				name = row.subjectName,
				showsDivider = index > 0,
				trailing = {
					Row(
						horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small),
						verticalAlignment = Alignment.CenterVertically
					) {
						row.grade?.let { grade -> CardValueView(text = grade.toString()) }
						if (row.grade == null) {
							attemptOutcomeText(row.outcome)?.let { text -> CardChipView(text = text.asString()) }
						}
						attemptBadgeText(row.badge)?.let { text -> CardChipView(text = text.asString()) }
					}
				}
			)
		}
		if (component.entityRefs.any { ref -> ref.kind == EntityKind.TERM }) {
			CardLinkView(
				text = stringResource(Res.string.assistant_card_a_link_record),
				onClick = { onAction(AssistantChat.Action.OpenNavTarget(NavTarget.RECORD, component.entityRefs)) }
			)
		}
	}
}
