package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.record.presentation.model.CreateTermAddSubjectTab
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.StringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_search_tab
import tuindice.record.generated.resources.create_term_suggested_tab

@Composable
fun CreateTermAddSubjectTabs(
	selectedTab: CreateTermAddSubjectTab,
	onTabSelected: (CreateTermAddSubjectTab) -> Unit
) {
	val tabs = CreateTermAddSubjectTab.entries
	val colors = SegmentedButtonDefaults.colors(
		activeContainerColor = MaterialTheme.colorScheme.primary,
		activeContentColor = MaterialTheme.colorScheme.onPrimary,
		activeBorderColor = MaterialTheme.colorScheme.primary,
		inactiveContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.48f),
		inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
		inactiveBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = TuIndiceAlpha.Muted)
	)

	SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
		tabs.forEachIndexed { index, tab ->
			SegmentedButton(
				modifier = Modifier.testTag(tab.testTag),
				selected = selectedTab == tab,
				onClick = { onTabSelected(tab) },
				shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size),
				colors = colors,
				icon = {}
			) {
				Text(
					text = stringResource(tab.labelResource),
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
		}
	}
}

private val CreateTermAddSubjectTab.labelResource: StringResource
	get() = when (this) {
		CreateTermAddSubjectTab.Suggested -> Res.string.create_term_suggested_tab
		CreateTermAddSubjectTab.Search -> Res.string.create_term_search_tab
	}

private val CreateTermAddSubjectTab.testTag: String
	get() = when (this) {
		CreateTermAddSubjectTab.Suggested -> RecordUiTags.CreateSyntheticTermSuggestedTab
		CreateTermAddSubjectTab.Search -> RecordUiTags.CreateSyntheticTermSearchTab
	}
