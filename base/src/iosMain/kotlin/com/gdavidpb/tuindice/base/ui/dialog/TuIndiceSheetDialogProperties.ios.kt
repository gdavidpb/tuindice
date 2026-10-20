package com.gdavidpb.tuindice.base.ui.dialog

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties

// Material's ModalBottomSheet opens its dialog with the default properties: the content is limited above the software
// keyboard, so the sheet ends at the keyboard's top edge and the dimmed screen shows behind the keyboard's corners.
// Here the dialog is the whole window, the keyboard is only an inset (useSoftwareKeyboardInset = false), and the
// platform neither scrims nor animates the window, because the sheet does both itself.
@OptIn(ExperimentalComposeUiApi::class)
internal actual fun tuIndiceSheetDialogProperties(dismissOnBackPress: Boolean): DialogProperties =
	DialogProperties(
		dismissOnBackPress = dismissOnBackPress,
		dismissOnClickOutside = false,
		usePlatformDefaultWidth = false,
		usePlatformInsets = false,
		useSoftwareKeyboardInset = false,
		scrimColor = Color.Transparent,
		animateTransition = false
	)
