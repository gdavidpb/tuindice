package com.gdavidpb.tuindice.assistant.ui.component.pensum

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.SubjectImpactItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardSectionLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardTrailingView
import com.gdavidpb.tuindice.assistant.ui.model.toCardFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_impact_empty
import tuindice.assistant.generated.resources.assistant_card_b_impact_label

// pensum.impacto/1: the subject is the label, and the terms it delays are the figure. Then the subjects it blocks, in
// the order the service gives them, each with its status. With none, a line says so.
@Composable
fun SubjectImpactComponentView(
	item: SubjectImpactItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Pensum.impact(item.id))) {
		CardLabelView(text = item.subjectName)
		CardFiguresView(primary = item.termsFigure.toCardFigure())
		CardSectionLabelView(text = stringResource(Res.string.assistant_card_b_impact_label))
		if (item.chain.isEmpty()) {
			CardBodyTextView(text = stringResource(Res.string.assistant_card_b_impact_empty), muted = true)
		}
		item.chain.forEachIndexed { index, subject ->
			CardRowView(
				code = subject.code,
				name = subject.nameText.asString(),
				showsDivider = index > 0,
				trailing = { CardTrailingView(value = null, chips = listOfNotNull(subject.statusChip)) }
			)
		}
		item.recordLink?.let { link -> CardLinkItemView(link = link, onAction = onAction) }
	}
}
