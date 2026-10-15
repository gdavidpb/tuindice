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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gdavidpb.tuindice.summary.presentation.model.ShareableTermCardItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.share_term_card_index_label

/**
 * The figures of the card: the index of the term in large, with its label, and the three figures under it: the approved
 * subjects, the approved credits and the cumulative average. They change with the term as one block.
 */
@Composable
fun BoxScope.ShareableTermCardFigures(item: ShareableTermCardItem) {
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
