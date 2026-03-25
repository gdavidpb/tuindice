package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags

@Composable
fun QuarterSelectorView(
	modifier: Modifier = Modifier,
	quarters: List<QuarterItem>,
	selectedQuarterId: String?,
	onQuarterSelected: (quarterId: String) -> Unit
) {
	val lazyListState = rememberLazyListState()

	LaunchedEffect(
		quarters.map { quarter -> quarter.quarterId },
		selectedQuarterId
	) {
		val selectedIndex = quarters.indexOfFirst { quarter ->
			quarter.quarterId == selectedQuarterId
		}

		if (selectedIndex >= 0) {
			lazyListState.scrollToItem(selectedIndex)
		}
	}

	LazyRow(
		modifier = modifier
			.fillMaxWidth()
			.padding(
				horizontal = 16.dp,
				vertical = 8.dp
			)
			.testTag(RecordUiTags.QuarterSelectorRow),
		state = lazyListState,
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		items(
			items = quarters,
			key = { quarter -> quarter.quarterId }
		) { quarter ->
			FilterChip(
				modifier = Modifier.testTag(RecordUiTags.quarterChip(quarter.quarterId)),
				selected = quarter.quarterId == selectedQuarterId,
				onClick = {
					onQuarterSelected(quarter.quarterId)
				},
				label = {
					Text(
						text = quarter.nameText,
						style = MaterialTheme.typography.titleMedium,
						maxLines = 1
					)
				}
			)
		}
	}
}
