package com.gdavidpb.tuindice.subjects.presentation.contract

import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectDetailItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

		assertEquals("Sobre MAT101", state.topBarTitle)
		assertTrue(state.isTopBarVisible)
	}

	@Test
	fun idle_exposesEmptyTopBarTitle() {
		assertEquals("", SubjectDetail.State.Idle.topBarTitle)
		assertTrue(SubjectDetail.State.Idle.isTopBarVisible)
	}

	@Test
	fun loading_exposesEmptyTopBarTitle() {
		assertEquals("", SubjectDetail.State.Loading.topBarTitle)
		assertTrue(SubjectDetail.State.Loading.isTopBarVisible)
	}
}
