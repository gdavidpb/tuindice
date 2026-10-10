package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PensumSubjectMoreDetailButtonUiTest {
	@Test
	fun when_buttonIsRendered_then_itIsAnEnabledMoreDetailAction() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumSubjectMoreDetailButton(onClick = {})
		}

		onNodeWithTag(PensumUiTags.SubjectDetailMoreButton)
			.assertTextEquals("Más detalle")
			.assertHasClickAction()
			.assertIsEnabled()
	}

	@Test
	fun when_buttonIsTapped_then_invokesOnClickOncePerTap() = runTuIndiceUiTest {
		var clickCount = 0

		setTuIndiceTestContent {
			PensumSubjectMoreDetailButton(onClick = { clickCount += 1 })
		}

		onNodeWithTag(PensumUiTags.SubjectDetailMoreButton).performClick()
		runOnIdle { assertEquals(1, clickCount) }
		onNodeWithTag(PensumUiTags.SubjectDetailMoreButton).performClick()
		runOnIdle { assertEquals(2, clickCount) }
	}
}
