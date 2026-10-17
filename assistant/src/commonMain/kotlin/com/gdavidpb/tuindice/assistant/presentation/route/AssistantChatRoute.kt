package com.gdavidpb.tuindice.assistant.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.navigation.AssistantDestination
import com.gdavidpb.tuindice.assistant.presentation.navigation.AssistantNavDependencies
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantChatViewModel
import com.gdavidpb.tuindice.assistant.ui.dialog.AssistantChatSheet
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavActions
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openCameraPicker
import io.github.vinceglb.filekit.dialogs.openFilePicker
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_nav_copied

// The chat sheet bridged to its view model. The sheet opens with the context of the screen it came from. Its dismissal
// and its component sheets go through the navigation actions; the copy, the snackbars, the navigation and the
// enrollment proof go through the host. The camera and the gallery are opened here, as the summary opens them, and the
// photo they return goes back to the view model. Whether the device has a camera is read once, here.
@Composable
fun AssistantChatRoute(
	viewModel: AssistantChatViewModel,
	context: AssistantScreenContext?,
	navActions: TuIndiceNavActions,
	dependencies: AssistantNavDependencies,
	showSnackBar: (SnackBarMessage) -> Unit
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val copiedText = stringResource(Res.string.assistant_nav_copied)
	val deviceInfo = koinInject<DeviceInfoRepository>()
	val isCameraAvailable = remember { deviceInfo.hasCamera() }

	// The view model has no initial action: the sheet is opened once, when the route enters.
	LaunchedEffect(Unit) {
		viewModel.dispatch(AssistantChat.Action.Open(context = context))
	}

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			AssistantChat.Effect.Dismiss -> navActions.pop()

			is AssistantChat.Effect.CopyText -> {
				dependencies.onCopyText(effect.text)
				showSnackBar(SnackBarMessage(message = copiedText))
			}

			is AssistantChat.Effect.ShowSnackBar -> showSnackBar(SnackBarMessage(message = effect.message))

			is AssistantChat.Effect.NavigateTo -> dependencies.onNavigateTo(effect.target, effect.entityRefs)

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
		}
	}

	AssistantChatSheet(
		state = viewState,
		onAction = viewModel::dispatch,
		isCameraAvailable = isCameraAvailable
	)
}
