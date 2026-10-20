package com.gdavidpb.tuindice.assistant.ui.component.sistema

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.CooldownItem
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardTitleView
import com.gdavidpb.tuindice.assistant.ui.model.CardTitleTone
import com.gdavidpb.tuindice.assistant.ui.view.AssistantAppealActionView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_cooldown_title

// sistema.enfriamiento/1: the assistant is paused for the student until a moment; they may appeal when allowed. The
// pause is drawn in the error tone of its title, with a pause mark in front of it; the card stays neutral.
@Composable
fun CooldownComponentView(
	item: CooldownItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier) {
		CardTitleView(
			text = stringResource(Res.string.assistant_cooldown_title),
			tone = CardTitleTone.Error,
			icon = Icons.Outlined.PauseCircle
		)
		CardBodyTextView(text = item.reasonText.asString())
		CardBodyTextView(text = item.untilText.asString(), muted = true)
		if (item.appealable) {
			AssistantAppealActionView(onAppeal = { text -> onAction(AssistantChat.Action.Appeal(text)) })
		}
	}
}
