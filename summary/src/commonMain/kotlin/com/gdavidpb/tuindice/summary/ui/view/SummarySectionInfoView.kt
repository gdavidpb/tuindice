package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.model.RichTooltipText
import com.gdavidpb.tuindice.base.ui.view.RichTooltipBox
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_summary_statistics_info

/**
 * The mark beside the title of a section: an "i" that opens, in a rich tooltip, what the section shows.
 * Tapping it opens the tooltip and taps again close it; the toolkit keeps one tooltip open at a time in the app.
 * A screen reader hears it as the button that explains the section.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummarySectionInfoView(
	info: RichTooltipText,
	tag: String,
	modifier: Modifier = Modifier
) {
	val isOpen = remember { mutableStateOf(false) }
	val description = stringResource(Res.string.a11y_summary_statistics_info)

	RichTooltipBox(
		isVisible = isOpen.value,
		onDismissRequest = { isOpen.value = false },
		tooltip = info,
		anchorPosition = TooltipAnchorPosition.Below
	) {
		Box(
			modifier = modifier
				.minimumInteractiveComponentSize()
				.testTag(tag)
				.semantics { contentDescription = description }
				.clickable(role = Role.Button) { isOpen.value = !isOpen.value },
			contentAlignment = Alignment.Center
		) {
			Icon(
				modifier = Modifier.size(InfoIconSize),
				imageVector = Icons.Outlined.Info,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

private val InfoIconSize = 18.dp
