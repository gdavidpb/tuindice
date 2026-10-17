package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// A card the chat embeds in a message or in the memory list: a surface tone over the sheet, a hairline
// border of the outline tone and no shadow.
@Composable
fun AssistantEmbeddedCardView(
	modifier: Modifier = Modifier,
	content: @Composable ColumnScope.() -> Unit
) {
	Card(
		modifier = modifier.fillMaxWidth(),
		shape = RoundedCornerShape(TuIndiceRadius.Card),
		colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
		border = BorderStroke(TuIndiceSpacing.Hairline, MaterialTheme.colorScheme.outlineVariant),
		elevation = CardDefaults.cardElevation(defaultElevation = TuIndiceSpacing.None)
	) {
		Column(
			modifier = Modifier.padding(TuIndiceSpacing.XLarge),
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
			content = content
		)
	}
}
