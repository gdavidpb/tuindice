package com.gdavidpb.tuindice.assistant.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantComponentSheet
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantComponentSheetViewModel
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.assistant.ui.dialog.AssistantComponentSheet as AssistantComponentSheetDialog

// The component sheet bridged to its view model. The sheet opens on the component it names and follows it. A link
// tapped inside the component is the sheet's to take (OpenNavTarget); any other action the component emits reaches the
// sheet as Unhandled, which it takes without a change, so the message keeps it.
@Composable
fun AssistantComponentSheetRoute(
	viewModel: AssistantComponentSheetViewModel,
	messageId: String,
	componentId: String,
	onDismissRequest: () -> Unit,
	onNavigateTo: (NavTarget, List<EntityRef>) -> Unit
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	LaunchedEffect(messageId, componentId) {
		viewModel.dispatch(AssistantComponentSheet.Action.Open(messageId = messageId, componentId = componentId))
	}

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			AssistantComponentSheet.Effect.Dismiss -> onDismissRequest()

			is AssistantComponentSheet.Effect.NavigateTo -> onNavigateTo(effect.target, effect.entityRefs)
		}
	}

	val content = viewState

	if (content is AssistantComponentSheet.State.Content) {
		AssistantComponentSheetDialog(
			messageId = content.messageId,
			item = content.item,
			onAction = { action ->
				viewModel.dispatch(
					if (action is AssistantChat.Action.OpenNavTarget) {
						AssistantComponentSheet.Action.OpenNavTarget(target = action.target, entityRefs = action.entityRefs)
					} else {
						AssistantComponentSheet.Action.Unhandled(action)
					}
				)
			},
			onDismissRequest = onDismissRequest
		)
	}
}
