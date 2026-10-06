package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class RecordEnrollmentProofActionViewUiTest {
	@Test
	fun when_theActionIsDrawn_then_itIsReadAsOpeningTheProof_andIsWideEnoughToTap() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordEnrollmentProofActionView(onClick = {})
		}

		assertNodeVisible(RecordUiTags.EnrollmentProofButton)
		// The icon has no label on screen: the description is what names the action.
		onNodeWithTag(RecordUiTags.EnrollmentProofButton)
			.assertContentDescriptionEquals("Ver comprobante")
			.assertHasClickAction()
			// The small button sits inside a target the size of the create button under it.
			.assertWidthIsEqualTo(56.dp)
			.assertHeightIsAtLeast(56.dp)
	}

	@Test
	fun when_theButtonIsTapped_then_theTapIsReportedOnce() = runTuIndiceUiTest {
		var clicks = 0

		setTuIndiceTestContent {
			RecordEnrollmentProofActionView(onClick = { clicks++ })
		}

		onNodeWithTag(RecordUiTags.EnrollmentProofButton).performClick()

		// The button and the target around it share the tap: it must not open the proof twice.
		assertEquals(1, clicks)
	}

	@Test
	fun when_theTapLandsOnTheEdgeOfTheTarget_outsideTheSmallButton_then_itStillCounts() = runTuIndiceUiTest {
		var clicks = 0

		setTuIndiceTestContent {
			RecordEnrollmentProofActionView(onClick = { clicks++ })
		}

		onNodeWithTag(RecordUiTags.EnrollmentProofButton).performTouchInput {
			click(position = Offset(x = 2f, y = 2f))
		}

		assertEquals(1, clicks)
	}
}
