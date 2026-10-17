package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// A state shown as an outlined chip («en curso», «disponible», «pesada»), in the muted tone: never the accent.
@Composable
fun CardChipView(
	text: String,
	modifier: Modifier = Modifier
) {
	val shape = RoundedCornerShape(TuIndiceRadius.Large)

	Text(
		text = text,
		modifier = modifier
			.border(TuIndiceSpacing.Hairline, MaterialTheme.colorScheme.outlineVariant, shape)
			.padding(horizontal = TuIndiceSpacing.Medium, vertical = TuIndiceSpacing.XSmall),
		style = MaterialTheme.typography.labelSmall,
		color = MaterialTheme.colorScheme.onSurfaceVariant
	)
}
