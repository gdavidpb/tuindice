package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.contract.ShareTerm
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.share.ShareTermCardArtPrefetch
import com.gdavidpb.tuindice.summary.ui.share.ShareTermPreview
import com.gdavidpb.tuindice.summary.ui.share.ShareTermTermChips
import com.gdavidpb.tuindice.summary.ui.share.swipeBetweenBackgrounds
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.share_term_empty_message

/**
 * The body of the sheet when there are closed terms: the terms as chips, the card of the chosen term over the chosen
 * background, the strip of backgrounds and the share button. The chosen term and background come resolved from the
 * [content], a swipe across the card moves to the backgrounds beside the chosen one, and the card pictures of the other
 * backgrounds are brought to disk while the sheet is open. While the card waits for the picture of the chosen
 * background, its tile in the strip is marked as loading.
 */
@Composable
fun ShareTermSheetContent(
	content: ShareTerm.State.Content,
	actions: ShareTermSheetActions
) {
	val selectedTerm = content.selectedTerm
	val selectedBackground = content.selectedBackground
	// The background the card still waits for, as the card reports it.
	val pendingBackgroundId = remember { mutableStateOf<String?>(null) }

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
			ShareTermCardArtPrefetch(backgrounds = content.backgrounds)

			Box(
				modifier = Modifier
					.fillMaxWidth()
					.swipeBetweenBackgrounds(
						previousId = content.previousBackgroundId,
						nextId = content.nextBackgroundId,
						onBackgroundClick = actions.onBackgroundClick
					),
				contentAlignment = Alignment.Center
			) {
				ShareTermPreview(
					card = selectedTerm.card,
					artwork = selectedBackground,
					onArtworkPending = { artworkId -> pendingBackgroundId.value = artworkId }
				)
			}
		}

		ShareTermBackgroundSection(
			backgrounds = content.backgrounds,
			onBackgroundClick = actions.onBackgroundClick,
			loadingId = pendingBackgroundId.value
		)

		ShareTermShareButton(
			term = selectedTerm,
			artwork = selectedBackground,
			isSharing = content.isSharing,
			onShareClick = actions.onShareClick
		)
	}
}
