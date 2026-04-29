package com.gdavidpb.tuindice.subjects.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetail as SubjectDetailModel
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SubjectDetailStateTest {
	@Test
	fun content_exposesSubjectCodeTopBarTitle() {
		val state = SubjectDetail.State.Content(
			detail = SubjectDetailModel(
				id = "MAT101",
				name = "Calculo I",
				credits = 5,
				gradingMode = GradingMode.NUMERIC,
				generatedAt = 1710000000000,
				expiresAt = 1712592000000
			),
			selectedTab = SubjectSegmentTab.CAREER
		)

		assertEquals("Sobre MAT101", state.topBarTitle)
		assertTrue(state.isTopBarVisible)
	}

	@Test
	fun loading_exposesEmptyTopBarTitle() {
		assertEquals("", SubjectDetail.State.Loading.topBarTitle)
		assertTrue(SubjectDetail.State.Loading.isTopBarVisible)
	}
}
