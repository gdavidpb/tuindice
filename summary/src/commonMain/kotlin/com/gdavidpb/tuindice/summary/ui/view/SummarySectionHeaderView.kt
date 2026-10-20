package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.model.RichTooltipText
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.TuIndiceChevronLink
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * The head of a section of the hub: its title and, at the other end, the link to the screen that
 * owns it ("Horario", "Sep - Dic 2026", "Pensum"). The link is the shared chevron link of the app, with a
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
			TuIndiceChevronLink(
				text = linkText,
				onClick = onLinkClick,
				modifier = Modifier.testTag(SummaryUiTags.sectionLink(sectionTag))
			)
		}
	}
}
