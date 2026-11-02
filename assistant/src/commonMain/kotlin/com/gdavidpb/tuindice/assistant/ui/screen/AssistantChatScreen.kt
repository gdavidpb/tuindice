package com.gdavidpb.tuindice.assistant.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.view.AssistantChatContent
import com.gdavidpb.tuindice.assistant.ui.view.AssistantTitleView
import com.gdavidpb.tuindice.base.ui.exposeTestTagsAsResourceId
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.a11y_assistant_back
import tuindice.assistant.generated.resources.assistant_chat_title

// The assistant occupies a screen of its own. Its sparkle and title stay in the header over consent and chat.
@Composable
fun AssistantChatScreen(
	state: AssistantChat.State,
	onAction: (AssistantChat.Action) -> Unit,
	isCameraAvailable: Boolean = false,
	modifier: Modifier = Modifier,
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
	Column(
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
			.exposeTestTagsAsResourceId()
			.testTag(AssistantUiTags.ChatScreen)
	) {
		Row(
			modifier = Modifier.fillMaxWidth().padding(start = TuIndiceSpacing.Medium, end = TuIndiceSpacing.Dialog),
			verticalAlignment = Alignment.CenterVertically
		) {
			IconButton(
				onClick = { onAction(AssistantChat.Action.Dismiss) },
				modifier = Modifier.testTag(AssistantUiTags.ChatBack)
			) {
				Icon(
					imageVector = Icons.AutoMirrored.Filled.ArrowBack,
					contentDescription = stringResource(Res.string.a11y_assistant_back)
				)
			}
			AssistantTitleView(
				text = stringResource(Res.string.assistant_chat_title),
				modifier = Modifier.testTag(AssistantUiTags.ChatTitle)
			)
		}
		AssistantChatContent(
			state = state,
			onAction = onAction,
			isCameraAvailable = isCameraAvailable,
			snackbarHostState = snackbarHostState,
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth()
				.padding(start = TuIndiceSpacing.Dialog, end = TuIndiceSpacing.Dialog, bottom = TuIndiceSpacing.Medium)
		)
	}
}
