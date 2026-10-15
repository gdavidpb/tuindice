package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.model.RichTooltipText
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

/**
 * The body of a [RichTooltipBox]: one text for each line of [tooltip], spaced apart in a column. A single line carries
 * the [RichTooltipText.testTag] itself, so a test can read its text. Several lines share one stack that carries it.
 */
@Composable
fun RichTooltipLines(tooltip: RichTooltipText) {
	val isSingleLine = tooltip.lines.size == 1

	Column(
		modifier = Modifier.optionalTag(if (isSingleLine) null else tooltip.testTag),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Two)
	) {
		tooltip.lines.forEach { line ->
			Text(
				text = line,
				modifier = Modifier.optionalTag(if (isSingleLine) tooltip.testTag else null)
			)
		}
	}
}

private fun Modifier.optionalTag(tag: String?): Modifier = if (tag == null) this else testTag(tag)
