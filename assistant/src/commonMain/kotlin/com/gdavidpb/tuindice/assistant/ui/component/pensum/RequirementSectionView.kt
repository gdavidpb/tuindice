package com.gdavidpb.tuindice.assistant.ui.component.pensum

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.RequirementSectionItem
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowText
import com.gdavidpb.tuindice.assistant.ui.component.common.CardSectionLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardTrailingView
import com.gdavidpb.tuindice.base.presentation.model.asString

// One section of the requirements: its label, then a row per subject with its status. A row is the link to its subject,
// and a section with no subject says so in its own line.
@Composable
fun RequirementSectionView(
	section: RequirementSectionItem,
	onAction: (AssistantChat.Action) -> Unit
) {
	CardSectionLabelView(text = section.label.asString())
	if (section.subjects.isEmpty()) {
		CardBodyTextView(text = section.emptyText.asString(), muted = true)
	}
	section.subjects.forEachIndexed { index, subject ->
		CardLinkRowView(
			link = subject.link,
			onAction = onAction,
			text = CardRowText(name = subject.nameText.asString(), codes = listOf(subject.code)),
			showsDivider = index > 0,
			trailing = { CardTrailingView(value = null, chips = listOfNotNull(subject.statusChip)) }
		)
	}
}
