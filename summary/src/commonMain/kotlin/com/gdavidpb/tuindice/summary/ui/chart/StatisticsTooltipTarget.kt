package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.gdavidpb.tuindice.base.ui.view.RichTooltipBox

/**
 * The touch area of one point of a chart, a button that the screen reader names by its description. It fills the box
 * it is placed in, and that box is the anchor of its tooltip, so the caller places it. Tapping it asks for the opposite
 * of [isOpen] through [onOpenChange], which keeps one tooltip open per chart. The toolkit asks for it to close when the
 * user taps outside it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsTooltipTarget(
	target: StatisticsTarget,
	isOpen: Boolean,
	onOpenChange: (Boolean) -> Unit,
	content: @Composable BoxScope.() -> Unit
) {
	RichTooltipBox(
		isVisible = isOpen,
		onDismissRequest = { onOpenChange(false) },
		tooltip = target.tooltip
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.testTag(target.tag)
				.semantics { contentDescription = target.description }
				.clickable(role = Role.Button, onClickLabel = target.actionLabel) {
					onOpenChange(!isOpen)
				},
			contentAlignment = Alignment.Center
		) {
			content()
		}
	}
}
