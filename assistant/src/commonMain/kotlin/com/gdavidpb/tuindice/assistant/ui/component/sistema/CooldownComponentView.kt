package com.gdavidpb.tuindice.assistant.ui.component.sistema

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.model.chatDateText
import com.gdavidpb.tuindice.assistant.ui.model.textResource
import com.gdavidpb.tuindice.assistant.ui.view.AssistantAppealActionView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.chatcontract.component.CooldownComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_cooldown_title
import tuindice.assistant.generated.resources.assistant_cooldown_until

// sistema.enfriamiento/1: the assistant is paused for the student until a moment; they may appeal when allowed.
@Composable
fun CooldownComponentView(
	component: CooldownComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier) {
		Text(
			text = stringResource(Res.string.assistant_cooldown_title),
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		Text(
			text = stringResource(component.reason.textResource()),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		Text(
			text = stringResource(Res.string.assistant_cooldown_until, component.untilMillis.chatDateText()),
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
		if (component.appealable) {
			AssistantAppealActionView(onAppeal = { text -> onAction(AssistantChat.Action.Appeal(text)) })
		}
	}
}
