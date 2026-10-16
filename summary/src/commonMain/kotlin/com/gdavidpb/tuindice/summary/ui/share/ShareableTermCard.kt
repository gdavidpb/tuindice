package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArtwork
import com.gdavidpb.tuindice.summary.presentation.model.ShareableTermCardItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_share_term_card

// The card is an image: its size in dp is the size of its mockup; captured at three pixels per dp it is 1080 by 1620.
internal val ShareableTermCardSize = DpSize(width = 360.dp, height = 540.dp)

/**
 * The "Mi trimestre" card of one closed term over the [artwork]. It carries the figures of the term and the credit of
 * the artwork, and never the name or the code of the student. The university's logo signs the header at its right.
 *
 * The card stays in place as the student moves between terms and backgrounds, and it changes as a whole: a new term or
 * a new artwork is one fade of the entire card (see [ShareableTermCardCrossfade]), and nothing in it slides. A remote
 * artwork takes the place of the one on screen only once it has loaded, so the card never goes bare meanwhile, and the
 * term waits for it too; one that cannot be loaded gives way to the one the app ships with, credit and all.
 * [onArtReady] is called once the artwork on screen is drawn: at once for the one the app ships with, and when a remote
 * artwork has loaded, or has failed and the card has turned to the one the app ships with. [onArtworkPending] is told
 * the id of the chosen artwork while the card still waits for its picture, and null once the card wears it or the one
 * it fell back to, so the sheet can mark the background on its way.
 */
@Composable
fun ShareableTermCard(
	item: ShareableTermCardItem,
	artwork: ShareTermArtwork,
	modifier: Modifier = Modifier,
	onArtReady: () -> Unit = {},
	onArtworkPending: (artworkId: String?) -> Unit = {}
) {
	val description = stringResource(Res.string.a11y_share_term_card)
	val platformContext = LocalPlatformContext.current
	// The app-wide loader, the same one the profile picture reads: its memory holds the artwork between visits.
	val imageLoader = remember(platformContext) { SingletonImageLoader.get(platformContext) }
	val latestArtwork = rememberUpdatedState(artwork)
	val latestItem = rememberUpdatedState(item)
	val shown = remember { mutableStateOf(ShareableTermCardShown.initial(term = item, chosen = artwork)) }
	val latestOnArtworkPending = rememberUpdatedState(onArtworkPending)
	// The chosen artwork while the card has not settled on it yet: its picture is still on its way.
	val pendingId = artwork.id.takeIf { shown.value.settledFor != artwork }

	LaunchedEffect(item) {
		shown.value = shown.value.showingTerm(term = item, chosen = latestArtwork.value)
	}

	LaunchedEffect(artwork) {
		val loaded = artwork.loadsWith(imageLoader = imageLoader, platformContext = platformContext)

		shown.value = shown.value.showingArtwork(
			chosen = artwork,
			resolved = artwork.shownAfter(loaded = loaded),
			term = latestItem.value
		)
	}

	LaunchedEffect(pendingId) {
		latestOnArtworkPending.value(pendingId)
	}

	Box(
		modifier = modifier
			.requiredSize(ShareableTermCardSize)
			.testTag(SummaryUiTags.ShareTermCard)
			.semantics { contentDescription = description }
	) {
		ShareableTermCardCrossfade(
			targetState = shown.value,
			modifier = Modifier.fillMaxSize(),
			isEmpty = { card -> card.artwork == null }
		) { card ->
			Box(modifier = Modifier.fillMaxSize().background(CardBone)) {
				ShareableTermCardArt(
					shown = card.artwork,
					onArtReady = onArtReady,
					onArtFailed = { failed -> shown.value = shown.value.fallingBackFrom(failed) }
				)

				ShareableTermCardHeader(term = card.item)
				ShareableTermCardFigures(item = card.item)
				ShareableTermCardFooter(item = card.item, artwork = card.artwork)
			}
		}
	}
}
