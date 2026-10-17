package com.gdavidpb.tuindice.assistant.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantMemory
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantMemoryViewModel
import com.gdavidpb.tuindice.assistant.ui.screen.AssistantMemoryScreen
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle

// The memory screen bridged to its view model. Clearing everything is asked through the confirmation dialog, which the
// host opens on onClearAllConfirmationRequested. Going back is the shell's: the screen has no back action of its own.
@Composable
fun AssistantMemoryRoute(
	viewModel: AssistantMemoryViewModel,
	onClearAllConfirmationRequested: () -> Unit,
	showSnackBar: (SnackBarMessage) -> Unit
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is AssistantMemory.Effect.ShowSnackBar -> showSnackBar(SnackBarMessage(message = effect.message))

			AssistantMemory.Effect.ShowClearAllConfirmation -> onClearAllConfirmationRequested()
		}
	}

	AssistantMemoryScreen(
		state = viewState,
		onAction = viewModel::dispatch
	)
}
