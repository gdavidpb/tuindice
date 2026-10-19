package com.gdavidpb.tuindice.assistant.ui.component.materias

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.SubjectListItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardChipView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.model.toCardFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_subjects_label

// materias.lista/1: how many subjects there are, then a row per subject: its code, its name and its credits, the chip
// of its status, and a link to its screen.
@Composable
fun SubjectListComponentView(
	item: SubjectListItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Subjects.list(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_subjects_label))
		CardFiguresView(primary = item.countFigure.toCardFigure())
		item.rows.forEachIndexed { index, subject ->
			CardRowView(
				code = subject.code,
				name = subject.nameText.asString(),
				showsDivider = index > 0,
				trailing = {
					Column(horizontalAlignment = Alignment.End) {
						subject.statusText?.let { text -> CardChipView(text = text.asString()) }
						subject.link?.let { link -> CardLinkItemView(link = link, onAction = onAction) }
					}
				}
			)
		}
		item.recordLink?.let { link -> CardLinkItemView(link = link, onAction = onAction) }
	}
}
