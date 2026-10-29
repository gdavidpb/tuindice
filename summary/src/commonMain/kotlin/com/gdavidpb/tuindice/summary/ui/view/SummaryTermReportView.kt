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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.icon.assistantSparkleTint
import com.gdavidpb.tuindice.base.ui.icon.rememberAssistantSparkleIcon
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermReportItem
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_term_report_entry

/**
 * The line that offers a term report the student has not opened: the sparkle, what the report is about, a "Nuevo" mark,
 * the report's own words under it and a chevron. It is a block of its own, as the attention line is, and it leads to
 * the report.
 */
@Composable
fun SummaryTermReportView(
	item: SummaryTermReportItem,
	onItemClick: (SummaryHubTarget) -> Unit,
	modifier: Modifier = Modifier
) {
	SummaryHubCardView(
		modifier = modifier.fillMaxWidth(),
		hasBorder = true
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.testTag(TermReportUiTags.TermReportEntry)
				.clickable(
					role = Role.Button,
					onClickLabel = stringResource(Res.string.a11y_term_report_entry),
					onClick = { onItemClick(item.target) }
				)
				.heightIn(min = EntryRowMinHeight)
				.padding(horizontal = TuIndiceSpacing.Screen, vertical = TuIndiceSpacing.Large),
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				modifier = Modifier.size(TuIndiceComponentSizes.IconMedium),
				imageVector = rememberAssistantSparkleIcon(),
				contentDescription = null,
				tint = assistantSparkleTint()
			)

			TermReportEntryText(
				item = item,
				modifier = Modifier.weight(1f)
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

private val EntryRowMinHeight = 48.dp
