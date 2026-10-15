package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.RichTooltipText
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * The head of a section of the hub: its title and, at the other end, the link to the screen that
 * owns it ("Horario", "Sep - Dic 2026", "Pensum"). The link is the text with its chevron and has a
 * touch target of the minimum size; [sectionTag] names both parts for the tests. A section whose
 * screen has nothing to open says no [linkText] and shows no link, and its head keeps the height it
 * would have with one, so the sections keep their rhythm. An [info] puts its mark beside the title.
 */
@Composable
fun SummarySectionHeaderView(
	title: String,
	linkText: String?,
	onLinkClick: () -> Unit,
	sectionTag: String,
	info: RichTooltipText? = null
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(min = TuIndiceComponentSizes.IconButton)
			.padding(horizontal = TuIndiceSpacing.XSmall),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Row(
			modifier = Modifier.weight(1f, fill = false),
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				modifier = Modifier.testTag(SummaryUiTags.sectionTitle(sectionTag)),
				text = title,
				style = MaterialTheme.typography.titleMedium
			)

			info?.let { tooltip ->
				SummarySectionInfoView(
					info = tooltip,
					tag = SummarySectionUiTags.info(sectionTag)
				)
			}
		}

		if (linkText != null) {
			Row(
				modifier = Modifier
					.clip(RoundedCornerShape(TuIndiceRadius.Small))
					.clickable(role = Role.Button, onClick = onLinkClick)
					.minimumInteractiveComponentSize()
					.testTag(SummaryUiTags.sectionLink(sectionTag)),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = linkText,
					style = MaterialTheme.typography.labelLarge,
					fontWeight = FontWeight.Medium,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Deemphasis)
				)

				Icon(
					modifier = Modifier.size(TuIndiceComponentSizes.IconMedium),
					imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Deemphasis)
				)
			}
		}
	}
}
