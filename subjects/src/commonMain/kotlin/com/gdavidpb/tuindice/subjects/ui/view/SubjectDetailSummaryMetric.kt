package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.model.RichTooltipText
import com.gdavidpb.tuindice.base.ui.view.RichTooltipBox
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
	val isTooltipVisible = remember { mutableStateOf(false) }

	RichTooltipBox(
		isVisible = isTooltipVisible.value,
		onDismissRequest = { isTooltipVisible.value = false },
		tooltip = RichTooltipText(
			lines = listOf(stringResource(tooltipLine1), stringResource(tooltipLine2))
		)
	) {
		Row(
			modifier = Modifier
				.testTag(testTag)
				.clickable {
					isTooltipVisible.value = !isTooltipVisible.value
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
