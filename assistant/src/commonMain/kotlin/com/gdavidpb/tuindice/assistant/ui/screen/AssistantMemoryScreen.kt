package com.gdavidpb.tuindice.assistant.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantMemory
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.view.AssistantMemoryContentView
import com.gdavidpb.tuindice.base.ui.view.EmptyStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.EmptyView
import com.gdavidpb.tuindice.base.ui.view.ErrorView
import com.gdavidpb.tuindice.base.ui.view.LoadingView
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_memory_empty_message
import tuindice.assistant.generated.resources.assistant_memory_empty_title
import tuindice.assistant.generated.resources.assistant_memory_failed_title
import tuindice.assistant.generated.resources.assistant_retry

// What the assistant remembers about the student. The screen paints its own background in every state.
@Composable
fun AssistantMemoryScreen(
	state: AssistantMemory.State,
	onAction: (AssistantMemory.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
			.testTag(AssistantUiTags.MemoryScreen)
	) {
		SealedCrossfade(targetState = state) { current ->
			when (current) {
				AssistantMemory.State.Idle -> Spacer(modifier = Modifier.fillMaxSize())

				AssistantMemory.State.Loading -> LoadingView(
					modifier = Modifier.testTag(AssistantUiTags.MemoryLoading)
				)

				AssistantMemory.State.Empty -> Box(
					modifier = Modifier.fillMaxSize().testTag(AssistantUiTags.MemoryEmpty)
				) {
					EmptyView(
						title = stringResource(Res.string.assistant_memory_empty_title),
						message = stringResource(Res.string.assistant_memory_empty_message),
						headerContent = { EmptyStateAnimationView() }
					)
				}

				is AssistantMemory.State.Failed -> Box(
					modifier = Modifier.fillMaxSize().testTag(AssistantUiTags.MemoryFailed)
				) {
					ErrorView(
						title = stringResource(Res.string.assistant_memory_failed_title),
						message = current.message,
						retryText = stringResource(Res.string.assistant_retry),
						onRetryClick = { onAction(AssistantMemory.Action.Retry) }
					)
				}

				is AssistantMemory.State.Content -> AssistantMemoryContentView(
					entries = current.entries,
					onAction = onAction
				)
			}
		}
	}
}
