package com.gdavidpb.tuindice.base.ui.dialog

import androidx.compose.ui.window.DialogProperties

// Edge to edge: the window covers the status and navigation bars too, so the sheet's scrim and surface reach the
// bottom of the screen, and the sheet pads its content with the system bars and the keyboard instead of the window
// fitting them.
internal actual fun tuIndiceSheetDialogProperties(dismissOnBackPress: Boolean): DialogProperties =
	DialogProperties(
		dismissOnBackPress = dismissOnBackPress,
		dismissOnClickOutside = false,
		usePlatformDefaultWidth = false,
		decorFitsSystemWindows = false
	)
