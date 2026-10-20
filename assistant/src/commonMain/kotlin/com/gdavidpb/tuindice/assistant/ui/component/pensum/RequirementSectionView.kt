package com.gdavidpb.tuindice.assistant.ui.component.pensum

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.RequirementSectionItem
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardChipView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardSectionLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardTrailingColumn
import com.gdavidpb.tuindice.base.presentation.model.asString

// One section of the requirements: its label, then a row per subject with its status, and its link to the subject. A
// section with no subject says so in its own line.
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
		CardRowView(
			code = subject.code,
			name = subject.nameText.asString(),
			showsDivider = index > 0,
			trailing = {
				CardTrailingColumn {
					subject.statusChip?.let { chip -> CardChipView(text = chip.text.asString(), tone = chip.tone) }
					subject.link?.let { link -> CardLinkItemView(link = link, onAction = onAction) }
				}
			}
		)
	}
}
