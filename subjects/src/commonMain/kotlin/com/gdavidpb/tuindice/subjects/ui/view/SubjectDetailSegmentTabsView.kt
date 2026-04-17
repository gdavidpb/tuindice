package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags

@Composable
fun SubjectDetailSegmentTabsView(
	selectedTab: SubjectSegmentTab,
	careerTabText: String,
	globalTabText: String,
	onTabSelected: (SubjectSegmentTab) -> Unit
) {
	Row(
		horizontalArrangement = Arrangement.spacedBy(10.dp)
	) {
		FilterChip(
			selected = selectedTab == SubjectSegmentTab.CAREER,
			onClick = { onTabSelected(SubjectSegmentTab.CAREER) },
			label = { Text(careerTabText) },
			modifier = Modifier.testTag(SubjectsUiTags.CareerTab)
		)
		FilterChip(
			selected = selectedTab == SubjectSegmentTab.GLOBAL,
			onClick = { onTabSelected(SubjectSegmentTab.GLOBAL) },
			label = { Text(globalTabText) },
			modifier = Modifier.testTag(SubjectsUiTags.GlobalTab)
		)
	}
}
