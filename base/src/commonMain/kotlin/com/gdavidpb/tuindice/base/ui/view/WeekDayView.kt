package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.model.WeekStripDayItem
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// One day of the week strip: its weekday above its number. The selected day has its number in a filled circle, and a
// day that holds an evaluation has a dot under it.
@Composable
fun WeekDayView(
	item: WeekStripDayItem,
	modifier: Modifier = Modifier
) {
	val selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
	val selectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer
	val selectedAccentColor = MaterialTheme.colorScheme.secondary
	val evaluationIndicatorColor = MaterialTheme.colorScheme.primary

	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			text = item.weekdayText.asString(),
			color = if (item.isSelected) selectedAccentColor else MaterialTheme.colorScheme.onSurfaceVariant,
			style = MaterialTheme.typography.labelSmall,
			fontWeight = FontWeight.Bold
		)

		Box(
			modifier = Modifier
				.padding(top = TuIndiceSpacing.XSmall)
				.size(36.dp)
				.background(
					color = if (item.isSelected) selectedContainerColor else Color.Transparent,
					shape = CircleShape
				),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = item.dayText,
				color = if (item.isSelected) selectedContentColor else MaterialTheme.colorScheme.onSurfaceVariant,
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold
			)
		}

		Box(
			modifier = Modifier
				.padding(top = 3.dp)
				.size(4.dp)
				.background(
					color = if (item.hasEvaluations) evaluationIndicatorColor else Color.Transparent,
					shape = CircleShape
				)
		)
	}
}
