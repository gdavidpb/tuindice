package com.gdavidpb.tuindice.assistant.ui.component.sistema

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.SuspensionItem
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardTitleView
import com.gdavidpb.tuindice.assistant.ui.model.CardTitleTone
import com.gdavidpb.tuindice.assistant.ui.view.AssistantAppealActionView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_suspension_title

// sistema.suspension/1: the assistant is off for the student, until a moment when the service sets one. The suspension
// is drawn in the error tone of its title, with a block mark in front of it; the card stays neutral.
@Composable
fun SuspensionComponentView(
	item: SuspensionItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier) {
		CardTitleView(
			text = stringResource(Res.string.assistant_suspension_title),
			tone = CardTitleTone.Error,
			icon = Icons.Outlined.Block
		)
		CardBodyTextView(text = item.reasonText.asString())
		CardBodyTextView(text = item.untilText.asString(), muted = true)
		if (item.appealable) {
			AssistantAppealActionView(onAppeal = { text -> onAction(AssistantChat.Action.Appeal(text)) })
		}
	}
}
