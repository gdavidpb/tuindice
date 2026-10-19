package com.gdavidpb.tuindice.assistant.ui.component.sistema

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.SuspensionItem
import com.gdavidpb.tuindice.assistant.ui.view.AssistantAppealActionView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_suspension_title

// sistema.suspension/1: the assistant is off for the student, until a moment when the service sets one.
@Composable
fun SuspensionComponentView(
	item: SuspensionItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier) {
		Text(
			text = stringResource(Res.string.assistant_suspension_title),
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		Text(
			text = item.reasonText.asString(),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		Text(
			text = item.untilText.asString(),
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
		if (item.appealable) {
			AssistantAppealActionView(onAppeal = { text -> onAction(AssistantChat.Action.Appeal(text)) })
		}
	}
}
