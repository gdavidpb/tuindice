package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.testing.sampleSubjectItem
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SubjectItemViewUiTest {
	@Test
	fun when_subjectItemIsReadOnly_then_displaysMetadataWithoutSlider() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectItemView(
				item = sampleSubjectItem(
					subjectId = "subject-id",
					isReadOnly = true
				),
				onGradeChange = { _, _ -> }
			)
		}

		assertNodeVisible(RecordUiTags.subjectItem("subject-id"))
		assertNodeHidden(RecordUiTags.subjectGradeSlider("subject-id"))
		onNodeWithText("FS1113").assertIsDisplayed()
		onNodeWithText("FISICA III").assertIsDisplayed()
		onNodeWithText("4 / 5").assertIsDisplayed()
		onNodeWithText("3 UC").assertIsDisplayed()
	}

	@Test
	fun when_subjectItemIsEditable_then_displaysSlider() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectItemView(
				item = sampleSubjectItem(
					subjectId = "subject-editable",
					isReadOnly = false
				),
				onGradeChange = { _, _ -> }
			)
		}

		assertNodeVisible(RecordUiTags.subjectItem("subject-editable"))
		assertNodeVisible(RecordUiTags.subjectGradeSlider("subject-editable"))
	}

	@Test
	fun when_sliderProgressChanges_then_emitsDraftGradeChange() = runTuIndiceUiTest {
		val events = mutableListOf<Pair<Int, Boolean>>()

		setTuIndiceTestContent {
			SubjectItemView(
				item = sampleSubjectItem(
					subjectId = "subject-slider",
					grade = 4,
					isReadOnly = false
				),
				onGradeChange = { grade, isSelected ->
					events += grade to isSelected
				}
			)
		}

		onNodeWithTag(RecordUiTags.subjectGradeSlider("subject-slider"))
			.performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
				assertTrue(setProgress(2f))
			}

		waitForIdle()

		assertTrue(events.isNotEmpty())
		assertTrue(
			events.any { (grade, isSelected) ->
				grade == 2 && !isSelected
			}
		)
	}

	@Test
	fun when_subjectItemIsRetired_then_displaysRetiredChip() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectItemView(
				item = sampleSubjectItem(
					subjectId = "subject-retired",
					grade = 0,
					isReadOnly = true
				),
				onGradeChange = { _, _ -> }
			)
		}

		onNodeWithText("FS1113").assertIsDisplayed()
		onNodeWithText("Retirada").assertIsDisplayed()
	}

	@Test
	fun when_subjectItemHasWithoutEffectStatus_then_displaysWithoutEffectChip() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectItemView(
				item = sampleSubjectItem(
					subjectId = "subject-without-effect",
					status = SubjectStatus.WITHOUT_EFFECT,
					isReadOnly = true
				),
				onGradeChange = { _, _ -> }
			)
		}

		onNodeWithText("FS1113").assertIsDisplayed()
		onNodeWithText("Sin efecto").assertIsDisplayed()
		onNodeWithText("4 / 5").assertIsDisplayed()
	}
}
