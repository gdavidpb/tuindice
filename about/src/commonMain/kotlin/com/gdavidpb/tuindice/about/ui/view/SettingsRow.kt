package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

/**
 * A row of Settings that opens something: another screen, a page, or another app. The chevron at the end says it leads
 * somewhere, which is what tells it from the rows that act on the spot ([SettingsActionRow]).
 */
@Composable
fun SettingsLinkRow(
	icon: Painter,
	text: String,
	testTag: String,
	onClick: () -> Unit
) {
	SettingsRowLayout(
		modifier = Modifier
			.testTag(testTag)
			.clickable(role = Role.Button, onClick = onClick),
		icon = icon,
		text = text,
		contentColor = MaterialTheme.colorScheme.onSurfaceVariant
	) {
		Icon(
			modifier = Modifier.size(TuIndiceComponentSizes.Icon),
			imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

/**
 * A row of Settings that does its thing at once (share, rate, sign out), with no chevron. A [isDestructive] row takes
 * the error color of the theme, as the destructive actions do everywhere else.
 */
@Composable
fun SettingsActionRow(
	icon: Painter,
	text: String,
	testTag: String,
	isDestructive: Boolean,
	onClick: () -> Unit
) {
	SettingsRowLayout(
		modifier = Modifier
			.testTag(testTag)
			.clickable(role = Role.Button, onClick = onClick),
		icon = icon,
		text = text,
		contentColor = if (isDestructive) {
			MaterialTheme.colorScheme.error
		} else {
			MaterialTheme.colorScheme.onSurfaceVariant
		},
		trailing = {}
	)
}

@Composable
private fun SettingsRowLayout(
	modifier: Modifier,
	icon: Painter,
	text: String,
	contentColor: Color,
	trailing: @Composable () -> Unit
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = TuIndiceSpacing.Screen, vertical = TuIndiceSpacing.XLarge),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Start
	) {
		Image(
			painter = icon,
			colorFilter = ColorFilter.tint(contentColor),
			contentDescription = null,
			modifier = Modifier.size(TuIndiceComponentSizes.Icon)
		)

		AboutSpanText(
			modifier = Modifier.weight(1f),
			text = text,
			color = contentColor
		)

		trailing()
	}
}
