package com.gdavidpb.tuindice.assistant.ui.component.pensum

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.RequirementsItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView

// pensum.requisitos/1: the subject is the label. Then one section each for its prerequisites, its corequisites and the
// subjects it unlocks, and a section with no subjects is not drawn. A subject shows its status and links to its screen.
@Composable
fun RequirementsComponentView(
	item: RequirementsItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Pensum.requirements(item.id))) {
		CardLabelView(text = item.subjectName)
		item.sections.forEach { section -> RequirementSectionView(section = section, onAction = onAction) }
		item.recordLink?.let { link -> CardLinkItemView(link = link, onAction = onAction) }
	}
}
