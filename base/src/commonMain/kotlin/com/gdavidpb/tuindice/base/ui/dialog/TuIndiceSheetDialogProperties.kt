package com.gdavidpb.tuindice.base.ui.dialog

import androidx.compose.ui.window.DialogProperties

// The window that holds a TuIndiceModalBottomSheet. It covers the whole screen and the sheet draws its own scrim, so
// the sheet is not limited to the area above the keyboard: the keyboard is an inset of the content instead.
internal expect fun tuIndiceSheetDialogProperties(dismissOnBackPress: Boolean): DialogProperties
