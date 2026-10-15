package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.base.ui.view.universityLogoPainter
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.domain.model.ShareBackgroundArt
import com.gdavidpb.tuindice.summary.presentation.model.ShareableTermCardItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFigureItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_share_term_card
import tuindice.summary.generated.resources.share_background_laberinto_cromovegetal_card
import tuindice.summary.generated.resources.share_term_card_brand
import tuindice.summary.generated.resources.share_term_card_index_label
import tuindice.summary.generated.resources.share_term_card_title

// The card is an image: its ink, its bone fades and its type are its own, whatever the theme and the font scale of the
// screen it is drawn on. Its size in dp is the size of its mockup; captured at three pixels per dp it is 1080 by 1620.
internal val ShareableTermCardSize = DpSize(width = 360.dp, height = 540.dp)

private const val CARD_INK = 0xFF22271A
private const val CARD_BONE = 0xFFF4EFE3

private val CardInk = Color(CARD_INK)
private val CardBone = Color(CARD_BONE)

private const val TOP_FADE_FRACTION = 0.52f
private const val BOTTOM_FADE_FRACTION = 0.24f

// Where each fade is at half its opacity, between its edge and the edge of the card.
private const val TOP_FADE_MIDDLE = 0.55f
private const val BOTTOM_FADE_MIDDLE = 0.5f

/**
 * The "Mi trimestre" card of one closed term over the [background] artwork. It carries the figures of the term and the
 * credit of the artwork, and never the name or the code of the student. The university's logo signs the header at its
 * right.
 *
 * The card stays in place as the student moves between terms and backgrounds: the figures of a term change by a short
 * fade and slide, and a new artwork fades in over the one before it. A remote artwork takes the place of the one on
 * screen only once it has loaded, so the card never goes bare meanwhile; one that cannot be loaded gives way to the
 * one the app ships with, credit and all. [onArtReady] is called once the artwork on screen is drawn: at once for the
 * one the app ships with, and when a remote artwork has loaded, or has failed and the card has turned to the one the
 * app ships with.
 */
@Composable
fun ShareableTermCard(
	item: ShareableTermCardItem,
	background: ShareBackground,
	modifier: Modifier = Modifier,
	onArtReady: () -> Unit = {}
) {
	val description = stringResource(Res.string.a11y_share_term_card)
	val platformContext = LocalPlatformContext.current
	// The app-wide loader, the same one the profile picture reads: its memory holds the artwork between visits.
	val imageLoader = remember(platformContext) { SingletonImageLoader.get(platformContext) }
	// The artwork on screen: the one the app ships with at once, and otherwise nothing until the chosen one is known.
	var shown by remember { mutableStateOf(background.takeIf { chosen -> chosen.art is ShareBackgroundArt.Bundled }) }

	LaunchedEffect(background) {
		shown = background.resolvedBy(imageLoader = imageLoader, platformContext = platformContext)
	}

	Box(
		modifier = modifier
			.requiredSize(ShareableTermCardSize)
			.background(CardBone)
			.testTag(SummaryUiTags.ShareTermCard)
			.semantics { contentDescription = description }
	) {
		ShareableTermCardArt(
			shown = shown,
			onArtReady = onArtReady,
			onArtFailed = { failed -> if (shown?.id == failed.id) shown = ShareBackground.included }
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
		ShareableTermCardFooter(item = item, background = shown)
	}
}

// A part of the card that changes with the term or the background. With the animations on it changes by its transition;
// with them off it is drawn at once, with no transition wrapper at all, so the picture never depends on how the part
// got its content. Its size changes at once in both cases: a size that animates would still be moving when a picture
// is taken.
@Composable
private fun <T> ChangingCardContent(
	targetState: T,
	modifier: Modifier,
	label: String,
	transitionSpec: AnimatedContentTransitionScope<T>.() -> ContentTransform,
	content: @Composable (T) -> Unit
) {
	if (LocalTuIndiceAnimationsEnabled.current) {
		AnimatedContent(
			targetState = targetState,
			modifier = modifier,
			transitionSpec = { transitionSpec().using(sizeTransform = null) },
			label = label
		) { shownState ->
			content(shownState)
		}
	} else {
		Box(modifier = modifier) {
			content(targetState)
		}
	}
}

@Composable
private fun ShareableTermCardArt(
	shown: ShareBackground?,
	onArtReady: () -> Unit,
	onArtFailed: (ShareBackground) -> Unit
) {
	ChangingCardContent(
		targetState = shown,
		modifier = Modifier.fillMaxSize(),
		label = "ShareableTermCardArtAnimatedContent",
		transitionSpec = { artTransform() }
	) { art ->
		if (art != null) {
			ShareableTermCardArtLayer(art = art, onArtReady = onArtReady, onArtFailed = onArtFailed)
		}
	}
}

@Composable
private fun ShareableTermCardArtLayer(
	art: ShareBackground,
	onArtReady: () -> Unit,
	onArtFailed: (ShareBackground) -> Unit
) {
	when (val source = art.art) {
		ShareBackgroundArt.Bundled -> {
			LaunchedEffect(art) { onArtReady() }

			Image(
				painter = painterResource(Res.drawable.share_background_laberinto_cromovegetal_card),
				contentDescription = null,
				modifier = Modifier.fillMaxSize(),
				contentScale = ContentScale.Crop
			)
		}

		is ShareBackgroundArt.Remote -> {
			val platformContext = LocalPlatformContext.current
			val imageLoader = remember(platformContext) { SingletonImageLoader.get(platformContext) }
			val request = remember(platformContext, source.cardUrl) {
				remoteArtRequest(platformContext = platformContext, url = source.cardUrl)
			}

			AsyncImage(
				model = request,
				imageLoader = imageLoader,
				contentDescription = null,
				modifier = Modifier.fillMaxSize(),
				contentScale = ContentScale.Crop,
				onSuccess = { onArtReady() },
				onError = { onArtFailed(art) }
			)
		}
	}
}

@Composable
private fun BoxScope.ShareableTermCardHeader(
	term: ShareableTermCardItem
) {
	Row(
		modifier = Modifier
			.align(Alignment.TopStart)
			.fillMaxWidth()
			.padding(start = 24.dp, top = 22.dp, end = 24.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		Column {
			Text(
				text = stringResource(Res.string.share_term_card_title),
				color = CardInk,
				style = TextStyle(fontSize = 17.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold)
			)
			ChangingCardContent(
				targetState = term.term,
				modifier = Modifier,
				label = "ShareableTermCardTermAnimatedContent",
				transitionSpec = { dissolveTransform() }
			) { shownTerm ->
				Text(
					text = shownTerm.asString(),
					color = CardInk.copy(alpha = 0.75f),
					style = TextStyle(fontSize = 14.sp, lineHeight = 19.sp)
				)
			}
		}

		Icon(
			modifier = Modifier
				.size(40.dp)
				.testTag(SummaryUiTags.ShareTermCardLogo),
			painter = universityLogoPainter(),
			contentDescription = null,
			tint = CardInk
		)
	}
}

@Composable
private fun BoxScope.ShareableTermCardFigures(item: ShareableTermCardItem) {
	ChangingCardContent(
		targetState = item,
		modifier = Modifier
			.align(Alignment.TopStart)
			.fillMaxWidth()
			.padding(start = 24.dp, top = 82.dp, end = 24.dp),
		label = "ShareableTermCardFiguresAnimatedContent",
		transitionSpec = { figuresTransform() }
	) { figures ->
		Column {
			Text(
				text = figures.index,
				modifier = Modifier.testTag(SummaryUiTags.ShareTermCardIndex),
				color = CardInk,
				style = TextStyle(fontSize = 54.sp, lineHeight = 54.sp)
			)
			Text(
				text = stringResource(Res.string.share_term_card_index_label),
				modifier = Modifier.padding(top = 2.dp),
				color = CardInk.copy(alpha = 0.75f),
				style = TextStyle(fontSize = 14.sp, lineHeight = 19.sp)
			)
			Row(
				modifier = Modifier.padding(top = 10.dp),
				horizontalArrangement = Arrangement.spacedBy(24.dp)
			) {
				ShareableTermCardFigure(figure = figures.approved)
				ShareableTermCardFigure(figure = figures.credits)
				ShareableTermCardFigure(figure = figures.cumulative)
			}
		}
	}
}

@Composable
private fun ShareableTermCardFigure(figure: SummaryStatisticsFigureItem) {
	Column {
		Text(
			text = figure.value,
			color = CardInk,
			style = TextStyle(fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium)
		)
		Text(
			text = figure.label.asString(),
			color = CardInk,
			style = TextStyle(fontSize = 12.sp, lineHeight = 15.sp)
		)
	}
}

@Composable
private fun BoxScope.ShareableTermCardFooter(
	item: ShareableTermCardItem,
	background: ShareBackground?
) {
	Column(
		modifier = Modifier
			.align(Alignment.BottomStart)
			.fillMaxWidth()
			.padding(start = 24.dp, end = 24.dp, bottom = 20.dp)
	) {
		ChangingCardContent(
			targetState = background,
			modifier = Modifier,
			label = "ShareableTermCardCreditAnimatedContent",
			transitionSpec = { creditTransform() }
		) { credited ->
			ShareableTermCardCredit(credited = credited)
		}

		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(
				text = item.career.asString(),
				modifier = Modifier
					.weight(1f)
					.padding(end = 10.dp),
				color = CardInk,
				style = TextStyle(fontSize = 13.sp, lineHeight = 17.sp),
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			Text(
				text = stringResource(Res.string.share_term_card_brand),
				color = CardInk,
				style = TextStyle(fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold)
			)
		}
	}
}

@Composable
private fun ShareableTermCardCredit(credited: ShareBackground?) {
	Text(
		// A blank line holds the place of the credit until the artwork is known, so the footer does not move.
		text = credited?.let(::artCredit) ?: AnnotatedString(" "),
		modifier = Modifier
			.testTag(SummaryUiTags.ShareTermCardCredit)
			.padding(bottom = 6.dp),
		color = CardInk.copy(alpha = 0.72f),
		style = TextStyle(fontSize = 11.sp, lineHeight = 14.sp),
		maxLines = 1,
		overflow = TextOverflow.Ellipsis
	)
}
