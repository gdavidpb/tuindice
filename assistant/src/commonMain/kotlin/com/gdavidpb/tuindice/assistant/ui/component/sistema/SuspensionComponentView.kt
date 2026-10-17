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
import com.gdavidpb.tuindice.chatcontract.component.SuspensionComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_suspension_open_ended
import tuindice.assistant.generated.resources.assistant_suspension_title
import tuindice.assistant.generated.resources.assistant_suspension_until

// sistema.suspension/1: the assistant is off for the student, until a moment when the service sets one.
@Composable
fun SuspensionComponentView(
	component: SuspensionComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val untilMillis = component.untilMillis

	AssistantEmbeddedCardView(modifier = modifier) {
		Text(
			text = stringResource(Res.string.assistant_suspension_title),
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		Text(
			text = stringResource(component.reason.textResource()),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		if (untilMillis != null) {
			Text(
				text = stringResource(Res.string.assistant_suspension_until, untilMillis.chatDateText()),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		} else {
			Text(
				text = stringResource(Res.string.assistant_suspension_open_ended),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
		if (component.appealable) {
			AssistantAppealActionView(onAppeal = { text -> onAction(AssistantChat.Action.Appeal(text)) })
		}
	}
}
