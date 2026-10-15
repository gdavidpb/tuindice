package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArt
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArtwork
import com.gdavidpb.tuindice.summary.presentation.model.ShareableTermCardItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_share_term_card

// The card is an image: its size in dp is the size of its mockup; captured at three pixels per dp it is 1080 by 1620.
internal val ShareableTermCardSize = DpSize(width = 360.dp, height = 540.dp)

private const val TOP_FADE_FRACTION = 0.52f
private const val BOTTOM_FADE_FRACTION = 0.24f

// Where each fade is at half its opacity, between its edge and the edge of the card.
private const val TOP_FADE_MIDDLE = 0.55f
private const val BOTTOM_FADE_MIDDLE = 0.5f

/**
 * The "Mi trimestre" card of one closed term over the [artwork]. It carries the figures of the term and the credit of
 * the artwork, and never the name or the code of the student. The university's logo signs the header at its right.
 *
 * The card stays in place as the student moves between terms and backgrounds: the figures of a term change by a short
 * fade and slide, and a new artwork fades in over the one before it. A remote artwork takes the place of the one on
 * screen only once it has loaded, so the card never goes bare meanwhile; one that cannot be loaded gives way to the one
 * the app ships with, credit and all. [onArtReady] is called once the artwork on screen is drawn: at once for the one
 * the app ships with, and when a remote artwork has loaded, or has failed and the card has turned to the one the app
 * ships with.
 */
@Composable
fun ShareableTermCard(
	item: ShareableTermCardItem,
	artwork: ShareTermArtwork,
	modifier: Modifier = Modifier,
	onArtReady: () -> Unit = {}
) {
	val description = stringResource(Res.string.a11y_share_term_card)
	val platformContext = LocalPlatformContext.current
	// The app-wide loader, the same one the profile picture reads: its memory holds the artwork between visits.
	val imageLoader = remember(platformContext) { SingletonImageLoader.get(platformContext) }
	// The artwork on screen: the one the app ships with at once, and otherwise nothing until the chosen one is known.
	val shown = remember { mutableStateOf(artwork.takeIf { chosen -> chosen.art == ShareTermArt.Bundled }) }

	LaunchedEffect(artwork) {
		val loaded = artwork.loadsWith(imageLoader = imageLoader, platformContext = platformContext)

		shown.value = artwork.shownAfter(loaded = loaded)
	}

	Box(
		modifier = modifier
			.requiredSize(ShareableTermCardSize)
			.background(CardBone)
			.testTag(SummaryUiTags.ShareTermCard)
			.semantics { contentDescription = description }
	) {
		ShareableTermCardArt(
			shown = shown.value,
			onArtReady = onArtReady,
			onArtFailed = { failed ->
				if (shown.value?.id == failed.id) shown.value = failed.shownAfter(loaded = false)
			}
		)

		Box(
			modifier = Modifier
				.align(Alignment.TopCenter)
				.fillMaxWidth()
				.fillMaxHeight(TOP_FADE_FRACTION)
				.background(
					Brush.verticalGradient(
						0f to CardBone.copy(alpha = 0.9f),
						TOP_FADE_MIDDLE to CardBone.copy(alpha = 0.78f),
						1f to CardBone.copy(alpha = 0f)
					)
				)
		)

		Box(
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.fillMaxWidth()
				.fillMaxHeight(BOTTOM_FADE_FRACTION)
				.background(
					Brush.verticalGradient(
						0f to CardBone.copy(alpha = 0f),
						BOTTOM_FADE_MIDDLE to CardBone.copy(alpha = 0.7f),
						1f to CardBone.copy(alpha = 0.92f)
					)
				)
		)

		ShareableTermCardHeader(term = item)
		ShareableTermCardFigures(item = item)
		ShareableTermCardFooter(item = item, artwork = shown.value)
	}
}
