package com.gdavidpb.tuindice.assistant.ui.component.materias

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.SubjectListItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowText
import com.gdavidpb.tuindice.assistant.ui.component.common.CardTrailingView
import com.gdavidpb.tuindice.assistant.ui.model.visibleRows
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_subjects_empty
import tuindice.assistant.generated.resources.assistant_card_b_subjects_label

// materias.lista/1: a row per subject: its code, its name and its credits, and on the right the chip of its
// status. A row is the link to its subject, so a tap anywhere on it opens the subject. With no subject, a line says
// so.
@Composable
fun SubjectListComponentView(
	item: SubjectListItem,
	rowLimit: Int?,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Subjects.list(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_subjects_label))
		if (item.rows.isEmpty()) {
			CardBodyTextView(text = stringResource(Res.string.assistant_card_b_subjects_empty), muted = true)
		}
		item.rows.visibleRows(rowLimit).forEachIndexed { index, subject ->
			CardLinkRowView(
				link = subject.link,
				onAction = onAction,
				text = CardRowText(name = subject.nameText.asString(), codes = listOf(subject.code)),
				showsDivider = index > 0,
				trailing = { CardTrailingView(value = null, chips = listOfNotNull(subject.statusChip)) }
			)
		}
		item.recordLink?.let { link -> CardLinkItemView(link = link, onAction = onAction) }
	}
}
