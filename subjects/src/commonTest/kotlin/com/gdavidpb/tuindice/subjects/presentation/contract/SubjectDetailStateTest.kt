package com.gdavidpb.tuindice.subjects.presentation.contract

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectDetailItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.top_bar_subject_detail

class SubjectDetailStateTest {
	@Test
	fun content_exposesSubjectCodeTopBarTitle() {
		val state = SubjectDetail.State.Content(
			detail = SubjectDetailItem(
				id = "MAT101",
				name = "Calculo I",
				creditsText = "5 UC",
				gradingModeText = null,
				generatedAtText = "Actualizado 9/3/2024",
				selectedTab = SubjectSegmentTab.CAREER,
				hasSegmentTabs = false,
				chartMode = SubjectDetailItem.ChartMode.NUMERIC_GRADES,
				careerSegment = null,
				globalSegment = null
			)
		)

		assertEquals(
			UiText.Resource(Res.string.top_bar_subject_detail, args = listOf("MAT101")),
			state.topBarTitle
		)
		assertTrue(state.isTopBarVisible)
	}

	@Test
	fun idle_exposesEmptyTopBarTitle() {
		assertEquals(UiText.Empty, SubjectDetail.State.Idle.topBarTitle)
		assertTrue(SubjectDetail.State.Idle.isTopBarVisible)
	}

	@Test
	fun loading_exposesEmptyTopBarTitle() {
		assertEquals(UiText.Empty, SubjectDetail.State.Loading.topBarTitle)
		assertTrue(SubjectDetail.State.Loading.isTopBarVisible)
	}
}
