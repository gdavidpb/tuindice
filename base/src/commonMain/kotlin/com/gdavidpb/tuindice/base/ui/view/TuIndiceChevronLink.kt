package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius

/**
 * The link that goes with a text of the app: the text in the emphasis tone, with an arrow after it that says the link
 * opens a screen. A link that leaves the app ([external]) shows the «open» mark in place of the arrow. The touch target
 * is of the minimum size, and the ripple is clipped to the rounded shape of the link.
 *
 * [modifier] goes last, inside the touch target, so a test tag it carries names the link itself. [onClickLabel] is what
 * a screen reader says the tap does, for a link whose text repeats beside others, such as «Ver materia CI5311».
 */
@Composable
fun TuIndiceChevronLink(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	external: Boolean = false,
	onClickLabel: String? = null
) {
	val tint = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Deemphasis)

	Row(
		modifier = Modifier
			.clip(RoundedCornerShape(TuIndiceRadius.Small))
			.clickable(role = Role.Button, onClickLabel = onClickLabel, onClick = onClick)
			.minimumInteractiveComponentSize()
			.then(modifier),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = text,
			style = MaterialTheme.typography.labelLarge,
			fontWeight = FontWeight.Medium,
			color = tint
		)

		Icon(
			modifier = Modifier.size(TuIndiceComponentSizes.IconMedium),
			imageVector = if (external) {
				Icons.AutoMirrored.Outlined.OpenInNew
			} else {
				Icons.AutoMirrored.Outlined.KeyboardArrowRight
			},
			contentDescription = null,
			tint = tint
		)
	}
}
