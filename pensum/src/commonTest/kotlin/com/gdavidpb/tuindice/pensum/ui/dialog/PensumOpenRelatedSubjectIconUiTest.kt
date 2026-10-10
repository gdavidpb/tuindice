package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PensumOpenRelatedSubjectIconUiTest {
	@Test
	fun when_codeIsProvided_then_iconDescribesTheSubjectItOpensAtDeclaredSize() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumOpenRelatedSubjectIcon(code = "CI4325")
		}

		onNodeWithContentDescription("Abrir CI4325")
			.assertExists()
			.assertWidthIsEqualTo(SubjectDetailOpenIconSize)
			.assertHeightIsEqualTo(SubjectDetailOpenIconSize)
	}

	@Test
	fun when_codeChanges_then_descriptionFollowsTheNewSubject() = runTuIndiceUiTest {
		val codeState = mutableStateOf("CI4325")

		setTuIndiceTestContent {
			PensumOpenRelatedSubjectIcon(code = codeState.value)
		}

		runOnIdle { codeState.value = "MA1111" }
		waitForIdle()

		onNodeWithContentDescription("Abrir MA1111").assertExists()
		onAllNodesWithContentDescription("Abrir CI4325").assertCountEquals(0)
	}
}
