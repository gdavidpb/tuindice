package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// A calm, non-actionable notice that sits above content: neutral surface, never the error color.
// With a title the icon aligns to the top; a one-line notice centers it. The notice animates in
// and out with [visible] so a sync that clears it does not make the content jump: callers keep it
// in the composition and flip [visible], holding the last text with [rememberLastNonNull].
@Composable
fun NoticeView(
	message: String,
	modifier: Modifier = Modifier,
	title: String? = null,
	icon: ImageVector = Icons.Outlined.Info,
	visible: Boolean = true
) {
	AnimatedVisibility(
		visible = visible,
		enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
		exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
	) {
		val shape = RoundedCornerShape(TuIndiceRadius.Medium)

		Row(
			modifier = modifier
				.testTag(BaseUiTags.NoticeView)
				.padding(horizontal = TuIndiceSpacing.Screen)
				.fillMaxWidth()
				.background(MaterialTheme.colorScheme.surfaceVariant, shape)
				.border(TuIndiceSpacing.Hairline, MaterialTheme.colorScheme.outlineVariant, shape)
				.padding(horizontal = TuIndiceSpacing.XLarge, vertical = TuIndiceSpacing.Large),
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
			verticalAlignment = if (title != null) Alignment.Top else Alignment.CenterVertically
		) {
			Icon(
				modifier = Modifier
					.testTag(BaseUiTags.NoticeIcon)
					// Beside a title the icon sits on its first line, not above it.
					.padding(top = if (title != null) TuIndiceSpacing.Two else TuIndiceSpacing.None)
					.size(TuIndiceComponentSizes.IconSmall),
				imageVector = icon,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Column(verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Two)) {
				if (title != null) {
					Text(
						modifier = Modifier.testTag(BaseUiTags.NoticeTitle),
						text = title,
						color = MaterialTheme.colorScheme.onSurface,
						style = MaterialTheme.typography.titleSmall
					)
				}
				Text(
					modifier = Modifier.testTag(BaseUiTags.NoticeMessage),
					text = message,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					style = MaterialTheme.typography.bodySmall
				)
			}
		}
	}
}
