package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.icon.ShareIcon
import com.gdavidpb.tuindice.summary.presentation.model.ShareCardPicture
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArtwork
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermTermItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.image.toPngBytes
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.share_term_share_action

// The primary button of the app, yellow with dark text, carrying the share icon of the platform. It keeps its size and
// its enabled state while the choice changes: the picture is taken of the card on screen when the student asks to
// share, and while it is on its way the button shows a small indicator in place of its icon.
/**
 * The share button of the sheet. It is enabled only when there is a closed term and its background to draw; a tap takes
 * the picture of the card on screen and hands it on, once.
 */
@Composable
fun ShareTermShareButton(
	term: ShareTermTermItem?,
	artwork: ShareTermArtwork?,
	isSharing: Boolean,
	onShareClick: (picture: ShareCardPicture) -> Unit
) {
	val isPreparing = remember { mutableStateOf(false) }
	val isBusy = isPreparing.value || isSharing

	Box(modifier = Modifier.fillMaxWidth()) {
		Button(
			modifier = Modifier
				.fillMaxWidth()
				.testTag(SummaryUiTags.ShareTermShareButton),
			onClick = { if (!isBusy) isPreparing.value = true },
			enabled = term != null && artwork != null
		) {
			if (isBusy) {
				CircularProgressIndicator(
					modifier = Modifier.size(18.dp),
					color = MaterialTheme.colorScheme.onPrimary,
					strokeWidth = 2.dp
				)
			} else {
				Icon(
					imageVector = ShareIcon,
					contentDescription = null,
					modifier = Modifier.size(18.dp)
				)
			}
			Text(
				modifier = Modifier.padding(start = 8.dp),
				text = stringResource(Res.string.share_term_share_action)
			)
		}

		if (isPreparing.value && term != null && artwork != null) {
			key(term.key, artwork.id) {
				ShareTermPicture(
					card = term.card,
					artwork = artwork,
					onCaptured = { bitmap ->
						isPreparing.value = false
						onShareClick(ShareCardPicture { bitmap.toPngBytes() })
					}
				)
			}
		}
	}
}
