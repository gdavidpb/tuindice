package com.gdavidpb.tuindice.assistant.presentation.route

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.navigation.AssistantDestination
import com.gdavidpb.tuindice.assistant.presentation.navigation.AssistantNavDependencies
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantChatViewModel
import com.gdavidpb.tuindice.assistant.ui.screen.AssistantChatScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.base.domain.repository.NotificationPermissionRepository
import com.gdavidpb.tuindice.base.presentation.navigation.LocalNavEntryScope
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavActions
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openCameraPicker
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_open_link_failed
import tuindice.assistant.generated.resources.assistant_nav_copied

// The chat opens with the previous screen's context. Its back action and component sheets go through navigation; copy,
// other destinations and the enrollment proof go through the host. Its notices have their own snackbar host.
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AssistantChatRoute(
	viewModel: AssistantChatViewModel,
	context: AssistantScreenContext?,
	navActions: TuIndiceNavActions,
	dependencies: AssistantNavDependencies
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val copiedText = stringResource(Res.string.assistant_nav_copied)
	val openLinkFailedText = stringResource(Res.string.assistant_chat_open_link_failed)
	val deviceInfo = koinInject<DeviceInfoRepository>()
	val isCameraAvailable = remember { deviceInfo.hasCamera() }
	val notificationPermission = koinInject<NotificationPermissionRepository>()
	val uriHandler = LocalUriHandler.current
	val snackbarHostState = remember { SnackbarHostState() }
	val scope = rememberCoroutineScope()
	val isCurrent = LocalNavEntryScope.current.isCurrent.value

	BackHandler(enabled = isCurrent) { viewModel.dispatch(AssistantChat.Action.Dismiss) }

	// A new notice replaces the one on screen, as the host of the screen does.
	val showSnackBar: (String) -> Unit = { message ->
		scope.launch {
			snackbarHostState.currentSnackbarData?.dismiss()
			snackbarHostState.showSnackbar(message = message)
		}
	}

	// The view model has no initial action: the chat opens once, when the route enters.
	LaunchedEffect(Unit) {
		viewModel.dispatch(AssistantChat.Action.Open(context = context))
	}

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			AssistantChat.Effect.Dismiss -> navActions.pop()

			is AssistantChat.Effect.CopyText -> {
				dependencies.onCopyText(effect.text)
				showSnackBar(copiedText)
			}

			is AssistantChat.Effect.ShowSnackBar -> showSnackBar(effect.message)

			is AssistantChat.Effect.NavigateTo -> dependencies.onNavigateTo(effect.target, effect.entityRefs)

			is AssistantChat.Effect.OpenUrl ->
				runCatching { uriHandler.openUri(effect.url) }
					.onFailure { showSnackBar(openLinkFailedText) }

			AssistantChat.Effect.OpenEnrollmentProof -> dependencies.onOpenEnrollmentProof()

			is AssistantChat.Effect.OpenComponentSheet -> navActions.push(
				AssistantDestination.ComponentSheet(messageId = effect.messageId, componentId = effect.componentId)
			)

			AssistantChat.Effect.OpenCamera ->
				runCatching { FileKit.openCameraPicker() }
					.getOrNull()
					?.let { file -> viewModel.dispatch(AssistantChat.Action.AttachPhoto(file)) }

			AssistantChat.Effect.OpenPhotoPicker ->
				runCatching { FileKit.openFilePicker(type = FileKitType.Image) }
					.getOrNull()
					?.let { file -> viewModel.dispatch(AssistantChat.Action.AttachPhoto(file)) }

			AssistantChat.Effect.RequestNotificationPermission -> notificationPermission.requestPermission()
		}
	}

	AssistantChatScreen(
		state = viewState,
		onAction = viewModel::dispatch,
		isCameraAvailable = isCameraAvailable,
		snackbarHostState = snackbarHostState
	)
}
