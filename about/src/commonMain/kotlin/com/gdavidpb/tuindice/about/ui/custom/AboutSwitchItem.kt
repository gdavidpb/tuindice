package com.gdavidpb.tuindice.about.ui.custom

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.about.ui.AboutUiTags

@Composable
fun AboutSwitchItem(
	icon: Painter,
	text: String,
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit,
	tint: Color? = null,
	size: Dp = 24.dp,
	testTag: String = AboutUiTags.ItemContainer
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onCheckedChange(!checked) }
			.padding(
				horizontal = 16.dp,
				vertical = 12.dp
			),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Start
	) {
		Image(
			painter = icon,
			colorFilter = tint?.let(ColorFilter::tint),
			contentDescription = text.substringBefore('\n'),
			modifier = Modifier.size(size)
		)

		AboutSpanText(
			modifier = Modifier.weight(1f),
			text = text
		)

		Switch(
			modifier = Modifier.testTag(testTag),
			checked = checked,
			onCheckedChange = onCheckedChange
		)
	}
}
