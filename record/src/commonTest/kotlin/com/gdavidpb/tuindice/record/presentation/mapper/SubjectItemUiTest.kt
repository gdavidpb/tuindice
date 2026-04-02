package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.base.ui.style.SubjectColorGenerator
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem
import com.gdavidpb.tuindice.record.testing.recordMapperTexts
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
				resolvedStatus = null,
				texts = recordMapperTexts()
			)
		}

		waitForIdle()

		val mappedSubject = assertNotNull(mapped)
		val expectedColors = SubjectColorGenerator.fromCode("FS1113")
		assertEquals("FS1113", mappedSubject.codeText)
		assertEquals(null, mappedSubject.status)
		assertEquals("", mappedSubject.gradeText)
		assertEquals("3 UC", mappedSubject.creditsText)
		assertEquals(expectedColors.color, mappedSubject.codeColor)
		assertEquals(expectedColors.containerColor, mappedSubject.codeContainerColor)
	}

	@Test
	fun when_subjectHasWithoutEffectStatus_then_mapsStatusAndPreservesGradeText() = runTuIndiceUiTest {
		val subject = Subject(
			id = "subject-2",
			quarterId = "quarter-1",
			code = "MA1112",
			name = "MATEMATICAS II",
			credits = 4,
			grade = 5,
			status = SubjectStatus.WITHOUT_EFFECT
		)
		var mapped: SubjectItem? = null

		setTuIndiceTestContent {
			mapped = subject.toSubjectItem(
				isReadOnly = true,
				resolvedStatus = subject.status,
				texts = recordMapperTexts()
			)
		}

		waitForIdle()

		val mappedSubject = assertNotNull(mapped)
		val expectedColors = SubjectColorGenerator.fromCode("MA1112")
		assertEquals("MA1112", mappedSubject.codeText)
		assertEquals(SubjectStatus.WITHOUT_EFFECT, mappedSubject.status)
		assertEquals("5 / 5", mappedSubject.gradeText)
		assertEquals("4 UC", mappedSubject.creditsText)
		assertEquals(expectedColors.color, mappedSubject.codeColor)
		assertEquals(expectedColors.containerColor, mappedSubject.codeContainerColor)
	}

	@Test
	fun when_subjectHasSimulationStatus_then_mapsResolvedStatus() = runTuIndiceUiTest {
		val subject = Subject(
			id = "subject-3",
			quarterId = "quarter-1",
			code = "MA1112",
			name = "MATEMATICAS II",
			credits = 4,
			grade = 2,
			simulationStatus = SubjectStatus.WITHOUT_EFFECT
		)
		var mapped: SubjectItem? = null

		setTuIndiceTestContent {
			mapped = subject.toSubjectItem(
				isReadOnly = true,
				resolvedStatus = subject.simulationStatus,
				texts = recordMapperTexts()
			)
		}

		waitForIdle()

		val mappedSubject = assertNotNull(mapped)
		assertEquals(SubjectStatus.WITHOUT_EFFECT, mappedSubject.status)
		assertEquals("2 / 5", mappedSubject.gradeText)
	}
}
