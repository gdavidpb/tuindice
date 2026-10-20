package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.gdavidpb.tuindice.assistant.ui.model.CardTitleTone
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// The title of a chat card («Advertencia», «El asistente está en pausa»): the first line of the card, with an optional
// icon in front of it. The tone paints both, and a warning, a pause or a suspension is drawn in the tone of its
// gravity.
@Composable
fun CardTitleView(
	text: String,
	modifier: Modifier = Modifier,
	tone: CardTitleTone = CardTitleTone.Neutral,
	icon: ImageVector? = null
) {
	val color = tone.color()

	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small)
	) {
		icon?.let { vector ->
			Icon(
				imageVector = vector,
				contentDescription = null,
				modifier = Modifier.size(TuIndiceComponentSizes.IconMedium),
				tint = color
			)
		}

		Text(
			text = text,
			modifier = Modifier.semantics { heading() },
			style = MaterialTheme.typography.titleMedium,
			color = color
		)
	}
}

@Composable
private fun CardTitleTone.color(): Color =
	when (this) {
		CardTitleTone.Neutral -> MaterialTheme.colorScheme.onSurface
		CardTitleTone.Warning -> AcademicStatusColors.warning()
		CardTitleTone.Error -> MaterialTheme.colorScheme.error
	}
