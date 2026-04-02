package com.gdavidpb.tuindice.record.ui.model

import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.record.testing.sampleSubjectItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SubjectItemDisplayTest {
	@Test
	fun when_currentGradeMatchesMappedGrade_then_usesMappedDisplayText() {
		val item = sampleSubjectItem(
			grade = 4,
			status = null
		)

		val display = item.toDisplay(currentGrade = 4)

		assertNull(display.status)
		assertEquals("4 / 5", display.gradeText)
	}

	@Test
	fun when_currentGradeChangesToMinimum_then_marksSubjectAsRetired() {
		val item = sampleSubjectItem(
			grade = 4,
			status = null
		)

		val display = item.toDisplay(currentGrade = 0)

		assertEquals(SubjectStatus.RETIRED, display.status)
		assertEquals("0 / 5", display.gradeText)
	}

	@Test
	fun when_subjectHasExplicitStatus_then_preservesIt() {
		val item = sampleSubjectItem(
			grade = 4,
			status = SubjectStatus.WITHOUT_EFFECT
		)

		val display = item.toDisplay(currentGrade = 4)

		assertEquals(SubjectStatus.WITHOUT_EFFECT, display.status)
		assertEquals("4 / 5", display.gradeText)
	}
}
