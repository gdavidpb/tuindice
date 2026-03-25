package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem
import com.gdavidpb.tuindice.record.testing.recordMapperTexts
import com.gdavidpb.tuindice.record.ui.style.SubjectColorGenerator
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SubjectItemUiTest {
	@Test
	fun when_subjectIsRetired_then_mapsRetiredStateWithoutGradeText() = runTuIndiceUiTest {
		val subject = Subject(
			id = "subject-1",
			quarterId = "quarter-1",
			code = "FS1113",
			name = "FISICA III",
			credits = 3,
			grade = 0
		)
		var mapped: SubjectItem? = null

		setTuIndiceTestContent {
			mapped = subject.toSubjectItem(
				isReadOnly = false,
				texts = recordMapperTexts()
			)
		}

		waitForIdle()

		val mappedSubject = assertNotNull(mapped)
		val expectedColors = SubjectColorGenerator.fromCode("FS1113")
		assertEquals("FS1113", mappedSubject.codeText)
		assertEquals("", mappedSubject.gradeText)
		assertEquals("3 UC", mappedSubject.creditsText)
		assertEquals(expectedColors.color, mappedSubject.codeColor)
		assertEquals(expectedColors.containerColor, mappedSubject.codeContainerColor)
		assertTrue(mappedSubject.isRetired)
	}

	@Test
	fun when_subjectIsActive_then_mapsCodeWithoutStatusAndNumericGrade() = runTuIndiceUiTest {
		val subject = Subject(
			id = "subject-2",
			quarterId = "quarter-1",
			code = "MA1112",
			name = "MATEMATICAS II",
			credits = 4,
			grade = 5
		)
		var mapped: SubjectItem? = null

		setTuIndiceTestContent {
			mapped = subject.toSubjectItem(
				isReadOnly = true,
				texts = recordMapperTexts()
			)
		}

		waitForIdle()

		val mappedSubject = assertNotNull(mapped)
		val expectedColors = SubjectColorGenerator.fromCode("MA1112")
		assertEquals("MA1112", mappedSubject.codeText)
		assertEquals("5 / 5", mappedSubject.gradeText)
		assertEquals("4 UC", mappedSubject.creditsText)
		assertEquals(expectedColors.color, mappedSubject.codeColor)
		assertEquals(expectedColors.containerColor, mappedSubject.codeContainerColor)
		assertFalse(mappedSubject.isRetired)
	}
}
