package com.gdavidpb.tuindice.assistant.ui.dialog

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.view.AssistantChatContent
import com.gdavidpb.tuindice.base.ui.dialog.TuIndiceModalBottomSheet
import com.gdavidpb.tuindice.base.ui.dialog.TuIndiceSheetValue
import com.gdavidpb.tuindice.base.ui.dialog.rememberTuIndiceSheetState
import com.gdavidpb.tuindice.base.ui.exposeTestTagsAsResourceId
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import kotlinx.coroutines.flow.first

private const val CHAT_SHEET_HEIGHT_FRACTION = 0.6f

// While the student types the body takes 90% of the height left above the keyboard: at 60% the title, the chip,
// the offer and the suggestions pushed the composer out of the sheet.
private const val CHAT_SHEET_TYPING_HEIGHT_FRACTION = 0.9f

// The chat sheet: the conversation carries the title «Asistente» after the sparkle, without the name of the app; the
// consent carries its own, with the sparkle too. It opens showing its whole body, composer included. Its notices show
// inside it, through [snackbarHostState]: the host of the screen under the sheet is covered by the sheet.
@Composable
fun AssistantChatSheet(
	state: AssistantChat.State,
	onAction: (AssistantChat.Action) -> Unit,
	isCameraAvailable: Boolean = false,
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
	// The body takes 60% of the window, more than the half a partly expanded sheet shows, so the first time the sheet
	// settles there it is lifted to show the composer. The half-open state itself stays; afterwards the student can drag
	// the sheet as any other.
	val sheetState = rememberTuIndiceSheetState(skipPartiallyExpanded = false)

	LaunchedEffect(sheetState) {
		snapshotFlow { sheetState.currentValue }.first { value -> value == TuIndiceSheetValue.PartiallyExpanded }
		sheetState.expand()
	}

	TuIndiceModalBottomSheet(
		modifier = Modifier
			.exposeTestTagsAsResourceId()
			.testTag(AssistantUiTags.ChatSheet),
		sheetState = sheetState,
		// The sheet pads only the top: the body pads the bottom itself, with the keyboard or the home indicator, so the
		// composer sits right above the keyboard instead of above the sheet's inset and the body's own on top of it.
		contentWindowInsets = { WindowInsets.safeDrawing.only(WindowInsetsSides.Top) },
		onDismissRequest = { onAction(AssistantChat.Action.Dismiss) }
	) {
		// The keyboard's inset is real inside the sheet, so the typing height comes from it, not from the focus.
		val isTyping = WindowInsets.ime.getBottom(LocalDensity.current) > 0
		val heightFraction = if (isTyping) CHAT_SHEET_TYPING_HEIGHT_FRACTION else CHAT_SHEET_HEIGHT_FRACTION

		AssistantChatContent(
			state = state,
			isCameraAvailable = isCameraAvailable,
			onAction = onAction,
			snackbarHostState = snackbarHostState,
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight(heightFraction)
				.padding(
					start = TuIndiceSpacing.Dialog,
					end = TuIndiceSpacing.Dialog,
					bottom = TuIndiceSpacing.Medium
				)
		)
	}
}
