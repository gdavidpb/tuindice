package com.gdavidpb.tuindice.assistant.ui.component.pensum

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.PensumComparisonItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardSectionLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.toCardFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_comparison_career
import tuindice.assistant.generated.resources.assistant_card_b_comparison_label
import tuindice.assistant.generated.resources.assistant_card_b_comparison_missing
import tuindice.assistant.generated.resources.assistant_card_b_comparison_missing_empty
import tuindice.assistant.generated.resources.assistant_card_b_comparison_pensum

// pensum.comparacion/1: the credits the two pensums share are the figure. Then the career and the pensum compared, the
// subjects still missing, and the notes the service gives, in rows and nothing more than the service brings. With no
// subject missing, a line says so.
@Composable
fun PensumComparisonComponentView(
	item: PensumComparisonItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Pensum.comparison(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_comparison_label))
		CardFiguresView(primary = item.sharedFigure.toCardFigure())
		CardRowView(
			name = stringResource(Res.string.assistant_card_b_comparison_career),
			showsDivider = false,
			trailing = { CardValueView(text = item.otherCareerName) }
		)
		CardRowView(
			name = stringResource(Res.string.assistant_card_b_comparison_pensum),
			showsDivider = true,
			trailing = { CardValueView(text = item.otherPensumLabel) }
		)
		CardSectionLabelView(text = stringResource(Res.string.assistant_card_b_comparison_missing))
		if (item.missingSubjects.isEmpty()) {
			CardBodyTextView(text = stringResource(Res.string.assistant_card_b_comparison_missing_empty), muted = true)
		}
		item.missingSubjects.forEachIndexed { index, subject ->
			CardRowView(code = subject.code, name = subject.nameText.asString(), showsDivider = index > 0)
		}
		item.notes.forEach { note -> CardBodyTextView(text = note) }
		item.recordLink?.let { link -> CardLinkItemView(link = link, onAction = onAction) }
	}
}
