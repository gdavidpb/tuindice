package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryAttentionItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * What needs the student's attention, one line per thing: a warning icon, what it is and a chevron
 * that says the line leads somewhere. It is a block of its own, with a border, and it is absent when
 * there is nothing to attend to. Unlike a notice it acts: each line leads to the screen that owns it.
 */
@Composable
fun SummaryAttentionView(
	items: List<SummaryAttentionItem>,
	onItemClick: (SummaryHubTarget) -> Unit,
	modifier: Modifier = Modifier
) {
	SummaryHubCardView(
		modifier = modifier.testTag(SummaryUiTags.AttentionContainer),
		hasBorder = true
	) {
		items.forEachIndexed { index, item ->
			if (index > 0) {
				SummaryHubDividerView()
			}

			Row(
				modifier = Modifier
					.fillMaxWidth()
					.testTag(SummaryUiTags.attentionItem(item.key))
					.clickable(role = Role.Button, onClick = { onItemClick(item.target) })
					.heightIn(min = AttentionRowMinHeight)
					.padding(horizontal = TuIndiceSpacing.Screen, vertical = TuIndiceSpacing.Large),
				horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge),
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					modifier = Modifier.size(TuIndiceComponentSizes.IconMedium),
					imageVector = Icons.Outlined.ErrorOutline,
					contentDescription = null,
					tint = AcademicStatusColors.warning()
				)

				Text(
					modifier = Modifier.weight(1f),
					text = item.text.asString(),
					style = MaterialTheme.typography.bodyLarge
				)

				Icon(
					modifier = Modifier.size(TuIndiceComponentSizes.IconMedium),
					imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Muted)
				)
			}
		}
	}
}

private val AttentionRowMinHeight = 48.dp
