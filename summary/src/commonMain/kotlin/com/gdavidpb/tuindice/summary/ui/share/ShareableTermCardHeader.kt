package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.view.universityLogoPainter
import com.gdavidpb.tuindice.summary.presentation.model.ShareableTermCardItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.share_term_card_title

/**
 * The header of the card: "Mi trimestre" over the term, and the university's logo at its right.
 */
@Composable
fun BoxScope.ShareableTermCardHeader(
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
			Text(
				text = term.term.asString(),
				color = CardInk.copy(alpha = 0.75f),
				style = TextStyle(fontSize = 14.sp, lineHeight = 19.sp)
			)
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
