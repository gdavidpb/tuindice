package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArtwork
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * The credit of the [artwork] on the card: the work's title in italics, then its author when the work has one. A blank
 * line holds the place of the credit until the artwork is known, so the footer does not move.
 */
@Composable
fun ShareableTermCardCredit(artwork: ShareTermArtwork?) {
	Text(
		text = artwork?.let(::creditText) ?: AnnotatedString(" "),
		modifier = Modifier
			.testTag(SummaryUiTags.ShareTermCardCredit)
			.padding(bottom = 6.dp),
		color = CardInk.copy(alpha = 0.72f),
		style = TextStyle(fontSize = 11.sp, lineHeight = 14.sp),
		maxLines = 1,
		overflow = TextOverflow.Ellipsis
	)
}

// The title in italics, then the author when the work has one: the credit reads as the work is named in the catalogs.
private fun creditText(artwork: ShareTermArtwork): AnnotatedString {
	return buildAnnotatedString {
		withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
			append(artwork.title)
		}

		artwork.author?.let { author -> append(" · $author") }
	}
}
