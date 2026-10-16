package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFigureItem

/**
 * One figure of the card: its [figure] value in large, with its label under it.
 */
@Composable
fun ShareableTermCardFigure(figure: SummaryStatisticsFigureItem) {
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
