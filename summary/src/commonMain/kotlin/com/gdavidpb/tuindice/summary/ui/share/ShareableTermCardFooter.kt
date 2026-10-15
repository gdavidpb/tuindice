package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArtwork
import com.gdavidpb.tuindice.summary.presentation.model.ShareableTermCardItem
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.share_term_card_brand

/**
 * The foot of the card: the credit of the [artwork] over the career of the student and the brand. The credit is blank
 * until the artwork is known.
 */
@Composable
fun BoxScope.ShareableTermCardFooter(
	item: ShareableTermCardItem,
	artwork: ShareTermArtwork?
) {
	Column(
		modifier = Modifier
			.align(Alignment.BottomStart)
			.fillMaxWidth()
			.padding(start = 24.dp, end = 24.dp, bottom = 20.dp)
	) {
		ChangingCardContent(
			targetState = artwork,
			modifier = Modifier,
			label = "ShareableTermCardCreditAnimatedContent",
			transitionSpec = { creditTransform() }
		) { credited ->
			ShareableTermCardCredit(artwork = credited)
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
