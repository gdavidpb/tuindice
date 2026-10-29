package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.base.ui.dialog.rememberTuIndiceSheetState
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.contract.ShareTerm
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.share_term_sheet_title

/**
 * The sheet that shares a closed term: the terms as chips, the card of the chosen one over the chosen background, the
 * backgrounds as a strip, and the share button. Without a closed term there is no card, so the button is disabled and
 * the sheet says what it waits for. The picture that is shared is the card of the choice on screen when the student
 * asks for it, taken once its artwork is there.
 */
@Composable
fun ShareTermContentDialog(
	state: ShareTerm.State,
	snackbarHostState: SnackbarHostState,
	actions: ShareTermSheetActions,
	onDismissRequest: () -> Unit
) {
	ConfirmationDialog(
		// Whole from the start: the card and its picker are what the sheet is for, so it does not open half hidden.
		sheetState = rememberTuIndiceSheetState(skipPartiallyExpanded = true),
		titleText = stringResource(Res.string.share_term_sheet_title),
		onDismissRequest = onDismissRequest
	) {
		Column(
			modifier = Modifier
				.testTag(SummaryUiTags.ShareTermSheet)
				.fillMaxWidth()
				.padding(bottom = TuIndiceSpacing.Dialog),
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Section)
		) {
			when (state) {
				ShareTerm.State.Idle, is ShareTerm.State.Loading -> ShareTermSheetLoading()

				is ShareTerm.State.Content -> ShareTermSheetContent(
					content = state,
					actions = actions
				)

				is ShareTerm.State.Unavailable -> ShareTermSheetUnavailable(
					title = state.title.asString(),
					message = state.message.asString()
				)
			}

			SnackbarHost(
				hostState = snackbarHostState,
				modifier = Modifier.fillMaxWidth()
			)
		}
	}
}
