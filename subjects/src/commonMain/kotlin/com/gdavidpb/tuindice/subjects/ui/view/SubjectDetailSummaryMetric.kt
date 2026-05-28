package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailSummaryMetric(
	testTag: String,
	valueText: String,
	contentDescription: String,
	tooltipLine1: StringResource,
	tooltipLine2: StringResource,
	icon: @Composable () -> Unit
) {
	val tooltipState = rememberTooltipState(isPersistent = true)
	val tooltipScope = rememberCoroutineScope()

	TooltipBox(
		positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
			TooltipAnchorPosition.Above
		),
		tooltip = {
			PlainTooltip(
				caretShape = TooltipDefaults.caretShape()
			) {
				Column(
					verticalArrangement = Arrangement.spacedBy(2.dp)
				) {
					Text(text = stringResource(tooltipLine1))
					Text(text = stringResource(tooltipLine2))
				}
			}
		},
		state = tooltipState,
		onDismissRequest = tooltipState::dismiss,
		enableUserInput = false
	) {
		Row(
			modifier = Modifier
				.testTag(testTag)
				.clickable {
					if (tooltipState.isVisible) {
						tooltipState.dismiss()
					} else {
						tooltipScope.launch {
							tooltipState.show()
						}
					}
				}
				.semantics(mergeDescendants = true) {
					this.contentDescription = contentDescription
				},
			horizontalArrangement = Arrangement.spacedBy(6.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			icon()
			Text(
				text = valueText,
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Medium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}
