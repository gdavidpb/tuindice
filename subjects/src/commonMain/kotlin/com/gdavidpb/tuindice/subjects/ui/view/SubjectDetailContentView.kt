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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.subjects.presentation.mapper.toCompactCountText
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetail as SubjectDetailModel
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.domain.model.SubjectStatsSegment
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@Composable
fun SubjectDetailContentView(
	detail: SubjectDetailModel,
	selectedTab: SubjectSegmentTab,
	careerTabText: String,
	globalTabText: String,
	onTabSelected: (SubjectSegmentTab) -> Unit
) {
	val segment = detail.resolveSegment(selectedTab) ?: return

	Column(
		modifier = Modifier
			.testTag(SubjectsUiTags.Content)
			.fillMaxSize()
			.verticalScroll(rememberScrollState())
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

		if (detail.careerSegment != null && detail.globalSegment != null) {
			SubjectDetailSegmentTabsView(
				selectedTab = selectedTab,
				careerTabText = careerTabText,
				globalTabText = globalTabText,
				onTabSelected = onTabSelected
			)
		}

		SubjectDetailSegmentSummaryView(
			studentsText = segment.sampleStudents.toCompactCountText(),
			attemptsText = segment.closedAttempts.toCompactCountText()
		)

		SubjectDetailKpiRowView(
			segment = segment
		)

		SubjectDetailChartsView(
			detail = detail,
			segment = segment
		)

		Text(
			text = "Actualizado ${detail.generatedAt.toDateText()}",
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		Spacer(modifier = Modifier.height(8.dp))
	}
}

private fun SubjectDetailModel.resolveSegment(
	selectedTab: SubjectSegmentTab
): SubjectStatsSegment? {
	return when (selectedTab) {
		SubjectSegmentTab.CAREER -> careerSegment ?: globalSegment
		SubjectSegmentTab.GLOBAL -> globalSegment ?: careerSegment
	}
}

private fun Long.toDateText(): String {
	val localDate = Instant.fromEpochMilliseconds(this)
		.toLocalDateTime(TimeZone.currentSystemDefault())
		.date
	return "${localDate.day}/${localDate.month.number}/${localDate.year}"
}
