package com.gdavidpb.tuindice.assistant.ui.component.pensum

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.RequirementSectionItem
import com.gdavidpb.tuindice.assistant.ui.component.common.CardChipView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.base.presentation.model.asString

// One section of the requirements: its label, then a row per subject with its status, and its link to the subject.
@Composable
fun RequirementSectionView(
	section: RequirementSectionItem,
	onAction: (AssistantChat.Action) -> Unit
) {
	CardLabelView(text = section.label.asString())
	section.subjects.forEachIndexed { index, subject ->
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
}
