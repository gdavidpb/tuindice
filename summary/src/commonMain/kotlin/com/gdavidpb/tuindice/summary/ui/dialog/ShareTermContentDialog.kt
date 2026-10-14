package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.base.ui.icon.ShareIcon
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.contract.ShareTerm
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.share.ShareTermBackgroundStrip
import com.gdavidpb.tuindice.summary.ui.share.ShareTermPreview
import com.gdavidpb.tuindice.summary.ui.share.ShareTermTermChips
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.share_term_background_label
import tuindice.summary.generated.resources.share_term_empty_message
import tuindice.summary.generated.resources.share_term_share_action
import tuindice.summary.generated.resources.share_term_sheet_title

/**
 * The sheet that shares a closed term: the terms as chips, the card of the chosen one over the chosen background, the
 * backgrounds as a strip, and the share button. Without a closed term there is no card, so the button is disabled and
 * the sheet says what it waits for. The picture that is shared is the card as it was captured.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareTermContentDialog(
	state: ShareTerm.State,
	snackbarHostState: SnackbarHostState,
	actions: ShareTermSheetActions,
	onDismissRequest: () -> Unit
) {
	ConfirmationDialog(
		// Whole from the start: the card and its picker are what the sheet is for, so it does not open half hidden.
		sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
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

@Composable
private fun ShareTermSheetLoading() {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(LoadingAreaHeight),
		contentAlignment = Alignment.Center
	) {
		CircularProgressIndicator(
			modifier = Modifier.testTag(SummaryUiTags.ShareTermLoadingIndicator)
		)
	}
}

@Composable
private fun ShareTermSheetContent(
	content: ShareTerm.State.Content,
	actions: ShareTermSheetActions
) {
	val selectedTerm = content.terms.firstOrNull { item -> item.selected }
	val selectedBackground = content.backgrounds.firstOrNull { item -> item.selected }?.background

	// The picture of the card on screen, with the term and the background it was captured for: a picture of another
	// choice is never shared.
	var captured by remember { mutableStateOf<CapturedShareTerm?>(null) }
	val capturedBitmap = captured
		?.takeIf { picture -> picture.termKey == selectedTerm?.key && picture.backgroundId == selectedBackground?.id }
		?.bitmap

	Column(verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Section)) {
		if (selectedTerm == null) {
			Text(
				modifier = Modifier.testTag(SummaryUiTags.ShareTermEmpty),
				text = stringResource(Res.string.share_term_empty_message),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		} else {
			ShareTermTermChips(
				terms = content.terms,
				onTermClick = actions.onTermClick
			)
		}

		if (selectedTerm != null && selectedBackground != null) {
			Box(
				modifier = Modifier.fillMaxWidth(),
				contentAlignment = Alignment.Center
			) {
				ShareTermPreview(
					card = selectedTerm.card,
					background = selectedBackground,
					onCaptured = { bitmap: ImageBitmap ->
						captured = CapturedShareTerm(
							termKey = selectedTerm.key,
							backgroundId = selectedBackground.id,
							bitmap = bitmap
						)
					}
				)
			}
		}

		ShareTermBackgroundSection(
			backgrounds = content.backgrounds,
			onBackgroundClick = actions.onBackgroundClick
		)

		ShareTermShareButton(
			enabled = capturedBitmap != null && !content.isSharing,
			onClick = { capturedBitmap?.let(actions.onShareClick) }
		)
	}
}

@Composable
private fun ShareTermBackgroundSection(
	backgrounds: List<ShareTermBackgroundItem>,
	onBackgroundClick: (backgroundId: String) -> Unit
) {
	Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
		Text(
			text = stringResource(Res.string.share_term_background_label),
			style = MaterialTheme.typography.labelMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		ShareTermBackgroundStrip(
			backgrounds = backgrounds,
			onBackgroundClick = onBackgroundClick
		)
	}
}

// The primary button of the app, yellow with dark text, carrying the share icon of the platform.
@Composable
private fun ShareTermShareButton(
	enabled: Boolean,
	onClick: () -> Unit
) {
	Button(
		modifier = Modifier
			.fillMaxWidth()
			.testTag(SummaryUiTags.ShareTermShareButton),
		onClick = onClick,
		enabled = enabled
	) {
		Icon(
			imageVector = ShareIcon,
			contentDescription = null,
			modifier = Modifier.size(18.dp)
		)
		Text(
			modifier = Modifier.padding(start = 8.dp),
			text = stringResource(Res.string.share_term_share_action)
		)
	}
}

// The sheet cannot read the record, so it says why there is nothing to share and offers nothing to retry.
@Composable
private fun ShareTermSheetUnavailable(
	title: String,
	message: String
) {
	Column(
		modifier = Modifier
			.testTag(SummaryUiTags.ShareTermUnavailable)
			.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Text(
			modifier = Modifier.testTag(SummaryUiTags.ShareTermUnavailableTitle),
			text = title,
			style = MaterialTheme.typography.titleMedium
		)

		Text(
			modifier = Modifier.testTag(SummaryUiTags.ShareTermUnavailableMessage),
			text = message,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

// The room the sheet keeps while its terms are read, so that it does not shrink when they arrive.
private val LoadingAreaHeight = 120.dp

// The picture of the card on screen, and the term and background it was captured for.
private class CapturedShareTerm(
	val termKey: String,
	val backgroundId: String,
	val bitmap: ImageBitmap
)
