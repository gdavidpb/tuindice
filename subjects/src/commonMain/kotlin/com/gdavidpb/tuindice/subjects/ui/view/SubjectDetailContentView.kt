package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectDetailItem
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags

@Composable
fun SubjectDetailContentView(
	detail: SubjectDetailItem,
	careerTabText: String,
	globalTabText: String,
	onTabSelected: (SubjectSegmentTab) -> Unit,
	scrollEnabled: Boolean = true,
	initialScrollOffset: Dp = 0.dp,
	onChartsVisibilityChange: (Boolean) -> Unit = {}
) {
	val segment = detail.selectedSegment ?: return
	val density = LocalDensity.current
	val initialScrollOffsetPx = with(density) {
		initialScrollOffset.roundToPx()
	}
	val chartsEnterThresholdPx = with(density) { 420.dp.roundToPx() }
	val chartsExitThresholdPx = with(density) { 320.dp.roundToPx() }
	val scrollState = rememberScrollState(initial = initialScrollOffsetPx)
	var areChartsVisible by remember(initialScrollOffsetPx) {
		mutableStateOf(initialScrollOffsetPx >= chartsEnterThresholdPx)
	}

	LaunchedEffect(initialScrollOffsetPx) {
		scrollState.scrollTo(initialScrollOffsetPx)
	}

	LaunchedEffect(scrollState.value) {
		val nextChartsVisible = when {
			areChartsVisible && scrollState.value <= chartsExitThresholdPx -> false
			!areChartsVisible && scrollState.value >= chartsEnterThresholdPx -> true
			else -> areChartsVisible
		}

		if (nextChartsVisible != areChartsVisible) {
			areChartsVisible = nextChartsVisible
			onChartsVisibilityChange(nextChartsVisible)
		}
	}

	Column(
		modifier = Modifier
			.testTag(SubjectsUiTags.Content)
			.fillMaxSize()
			.verticalScroll(scrollState, enabled = scrollEnabled)
			.padding(
				start = 20.dp,
				top = InternalScreenDefaults.TopBarSpacing + 8.dp,
				end = 20.dp,
				bottom = 8.dp
			),
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		SubjectDetailHeaderView(
			detail = detail
		)

		if (detail.hasSegmentTabs) {
			SubjectDetailSegmentTabsView(
				selectedTab = detail.selectedTab,
				careerTabText = careerTabText,
				globalTabText = globalTabText,
				onTabSelected = onTabSelected
			)
		}

		SubjectDetailSegmentSummaryView(
			studentsText = segment.studentsText,
			attemptsText = segment.attemptsText
		)

		SubjectDetailKpiRowView(
			segment = segment
		)

		SubjectDetailChartsView(
			detail = detail,
			segment = segment
		)

		Text(
			text = detail.generatedAtText,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		Spacer(modifier = Modifier.height(8.dp))
	}
}
