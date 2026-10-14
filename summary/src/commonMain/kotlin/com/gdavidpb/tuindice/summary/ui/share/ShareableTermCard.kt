package com.gdavidpb.tuindice.summary.ui.share

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import com.gdavidpb.tuindice.base.presentation.model.asString
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
 * credit of the artwork, and never the name or the code of the student. [logo] is drawn at the right of the header when
 * there is one, and [onArtReady] is called once the artwork is on screen: at once for the one the app ships with, and
 * when a remote artwork has loaded, or has failed and fallen back to the one the app ships with.
 */
@Composable
fun ShareableTermCard(
	item: ShareableTermCardItem,
	background: ShareBackground,
	modifier: Modifier = Modifier,
	logo: (@Composable () -> Unit)? = null,
	onArtReady: () -> Unit = {}
) {
	val description = stringResource(Res.string.a11y_share_term_card)

	Box(
		modifier = modifier
			.requiredSize(ShareableTermCardSize)
			.background(CardBone)
			.testTag(SummaryUiTags.ShareTermCard)
			.semantics { contentDescription = description }
	) {
		ShareableTermCardArt(background = background, onArtReady = onArtReady)

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

		ShareableTermCardHeader(term = item, logo = logo)
		ShareableTermCardFigures(item = item)
		ShareableTermCardFooter(item = item, background = background)
	}
}

@Composable
private fun ShareableTermCardArt(
	background: ShareBackground,
	onArtReady: () -> Unit
) {
	when (val art = background.art) {
		ShareBackgroundArt.Bundled -> {
			LaunchedEffect(background) { onArtReady() }

			Image(
				painter = painterResource(Res.drawable.share_background_laberinto_cromovegetal_card),
				contentDescription = null,
				modifier = Modifier.fillMaxSize(),
				contentScale = ContentScale.Crop
			)
		}

		is ShareBackgroundArt.Remote -> {
			val platformContext = LocalPlatformContext.current
			// The app-wide loader, the same one the profile picture reads: its memory holds the artwork between visits.
			val imageLoader = remember(platformContext) { SingletonImageLoader.get(platformContext) }

			AsyncImage(
				model = art.cardUrl,
				imageLoader = imageLoader,
				contentDescription = null,
				modifier = Modifier.fillMaxSize(),
				error = painterResource(Res.drawable.share_background_laberinto_cromovegetal_card),
				contentScale = ContentScale.Crop,
				onSuccess = { onArtReady() },
				onError = { onArtReady() }
			)
		}
	}
}

@Composable
private fun BoxScope.ShareableTermCardHeader(
	term: ShareableTermCardItem,
	logo: (@Composable () -> Unit)?
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
			Text(
				text = term.term.asString(),
				color = CardInk.copy(alpha = 0.75f),
				style = TextStyle(fontSize = 14.sp, lineHeight = 19.sp)
			)
		}

		if (logo != null) {
			Box(modifier = Modifier.size(40.dp)) {
				logo()
			}
		}
	}
}

@Composable
private fun BoxScope.ShareableTermCardFigures(item: ShareableTermCardItem) {
	Column(
		modifier = Modifier
			.align(Alignment.TopStart)
			.fillMaxWidth()
			.padding(start = 24.dp, top = 82.dp, end = 24.dp)
	) {
		Text(
			text = item.index,
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
			ShareableTermCardFigure(figure = item.approved)
			ShareableTermCardFigure(figure = item.credits)
			ShareableTermCardFigure(figure = item.cumulative)
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
	background: ShareBackground
) {
	Column(
		modifier = Modifier
			.align(Alignment.BottomStart)
			.fillMaxWidth()
			.padding(start = 24.dp, end = 24.dp, bottom = 20.dp)
	) {
		Text(
			text = artCredit(background),
			modifier = Modifier
				.testTag(SummaryUiTags.ShareTermCardCredit)
				.padding(bottom = 6.dp),
			color = CardInk.copy(alpha = 0.72f),
			style = TextStyle(fontSize = 11.sp, lineHeight = 14.sp),
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
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

// The title in italics, then the author when the work has one: the credit reads as the work is named in the catalogs.
private fun artCredit(background: ShareBackground): AnnotatedString {
	return buildAnnotatedString {
		withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
			append(background.title)
		}

		background.author?.let { author -> append(" · $author") }
	}
}
