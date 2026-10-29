package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import com.gdavidpb.tuindice.base.ui.LocalTuIndiceCompositionProbe
import com.gdavidpb.tuindice.subjects.testing.subjectDetailItem
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// The probe names the body of the detail. A scroll moves the content in layout, and the charts' thresholds are
// read from a snapshot: no frame of a scroll composes the body, and the charts still change at the same points.
@OptIn(ExperimentalTestApi::class)
class SubjectDetailContentViewCompositionUiTest {
	@Test
	fun when_contentIsScrolled_then_theBodyComposesForNoFrameOfTheScroll() = runTuIndiceUiTest {
		val compositions = mutableListOf<String>()
		val probe: (String) -> Unit = { name -> compositions += name }

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceCompositionProbe provides probe) {
				SubjectDetailContentView(
					detail = subjectDetailItem(),
					careerTabText = "Tu carrera",
					globalTabText = "General",
					onTabSelected = {}
				)
			}
		}

		waitForIdle()
		val settled = compositions.count { it == "subject-detail" }
		val generatedAtBefore = onNodeWithTag(SubjectsUiTags.GeneratedAt).fetchSemanticsNode().positionInRoot.y
		assertTrue(settled > 0, "the probe names the body once it composes")

		onNodeWithTag(SubjectsUiTags.Content).performTouchInput { swipeUp() }
		waitForIdle()

		val generatedAtAfter = onNodeWithTag(SubjectsUiTags.GeneratedAt).fetchSemanticsNode().positionInRoot.y
		assertTrue(generatedAtAfter < generatedAtBefore, "the content scrolled up under the gesture")
		assertEquals(
			settled,
			compositions.count { it == "subject-detail" },
			"the frames of the scroll compose the body for none"
		)
	}

	@Test
	fun when_contentIsScrolledDownAndBackUp_then_theChartsReportEachThresholdCrossing() = runTuIndiceUiTest {
		val visibilityChanges = mutableListOf<Boolean>()

		setTuIndiceTestContent {
			SubjectDetailContentView(
				detail = subjectDetailItem(),
				careerTabText = "Tu carrera",
				globalTabText = "General",
				onTabSelected = {},
				onChartsVisibilityChange = { isVisible -> visibilityChanges += isVisible }
			)
		}

		waitForIdle()
		assertEquals(emptyList(), visibilityChanges)

		onNodeWithTag(SubjectsUiTags.GeneratedAt).performScrollTo()
		waitUntil(timeoutMillis = 5_000) {
			visibilityChanges == listOf(true)
		}

		onNodeWithText("Calculo I").performScrollTo()
		waitUntil(timeoutMillis = 5_000) {
			visibilityChanges == listOf(true, false)
		}
	}
}
