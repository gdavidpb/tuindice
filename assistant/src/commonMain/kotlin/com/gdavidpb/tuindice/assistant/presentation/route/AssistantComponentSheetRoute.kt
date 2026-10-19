package com.gdavidpb.tuindice.assistant.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantComponentSheet
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantComponentSheetViewModel
import com.gdavidpb.tuindice.assistant.ui.dialog.AssistantComponentSheet as AssistantComponentSheetDialog
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityRef

// The component sheet bridged to its view model. The sheet opens on the component it names and follows it. Only a link
// tapped inside the component is handled here (OpenNavTarget); any other action the component emits is dropped by this
// route, so a component with actions of its own is not meant for a sheet.
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
			component = content.component,
			onAction = { action ->
				if (action is AssistantChat.Action.OpenNavTarget) {
					viewModel.dispatch(
						AssistantComponentSheet.Action.OpenNavTarget(
							target = action.target,
							entityRefs = action.entityRefs
						)
					)
				}
			},
			onDismissRequest = onDismissRequest
		)
	}
}
