package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import com.gdavidpb.tuindice.base.ui.LocalTuIndiceCompositionProbe
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.testing.samplePensumScreenModelWithSelectableYears
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

// The probe names the row each time it composes. The opening animation runs for 700 ms, read frame by frame: a frame
// composes the row when a number the summary shows changes, and no frame that shows the same numbers composes it.
@OptIn(ExperimentalTestApi::class)
class PensumSummaryRowCompositionUiTest {
	@Test
	fun when_summaryOpens_then_theRowComposesOnlyWhenAShownNumberChanges() = runTuIndiceUiTest {
		val compositions = mutableListOf<String>()
		val probe: (String) -> Unit = { name -> compositions += name }
		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceCompositionProbe provides probe) {
				PensumSummaryRow(model = openingModel(), onPensumContextClick = {})
			}
		}
		waitForIdle()

		var shown = shownTexts()
		var composed = compositions.count { name -> name == ROW_PROBE }
		var changedFrames = 0

		repeat(OPENING_FRAMES) {
			mainClock.advanceTimeByFrame()
			waitForIdle()

			val nowShown = shownTexts()
			val nowComposed = compositions.count { name -> name == ROW_PROBE }
			val composedInFrame = nowComposed - composed

			if (nowShown != shown) {
				changedFrames++
				assertTrue(composedInFrame == 1, "the row composed $composedInFrame times in a frame that changed a number")
			} else {
				assertTrue(composedInFrame == 0, "the row composed in a frame that shows the same numbers")
			}

			shown = nowShown
			composed = nowComposed
		}

		assertTrue(changedFrames > 1, "the numbers changed on $changedFrames frames")
	}

	@Test
	fun when_summaryOpens_then_theNumbersCountUpToTheirValues() = runTuIndiceUiTest {
		val percents = mutableSetOf<String>()
		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			PensumSummaryRow(model = openingModel(), onPensumContextClick = {})
		}
		waitForIdle()

		repeat(OPENING_FRAMES) {
			mainClock.advanceTimeByFrame()
			waitForIdle()
			percents += shownTexts().filter { text -> text.endsWith(PROGRESS_SUFFIX) }
		}

		val finalTexts = shownTexts()
		assertTrue(FINAL_PERCENT in finalTexts, "the summary does not end on $FINAL_PERCENT")
		assertTrue(FINAL_CREDITS in finalTexts, "the summary does not end on $FINAL_CREDITS")
		assertTrue(percents.size > MIN_PERCENTS_ON_THE_WAY, "the percent passed through ${percents.size} values")
	}

	private fun openingModel(): PensumScreenModel {
		return samplePensumScreenModelWithSelectableYears().copy(
			progressPercent = OPENING_PERCENT,
			approvedCredits = OPENING_APPROVED_CREDITS,
			totalCredits = OPENING_TOTAL_CREDITS
		)
	}

	private companion object {
		const val ROW_PROBE = "pensum-summary-row"
		const val OPENING_FRAMES = 60
		const val OPENING_PERCENT = 40
		const val OPENING_APPROVED_CREDITS = 32
		const val OPENING_TOTAL_CREDITS = 80
		const val PROGRESS_SUFFIX = "Avance"
		const val FINAL_PERCENT = "40% Avance"
		const val FINAL_CREDITS = "32 / 80 UC"
		const val MIN_PERCENTS_ON_THE_WAY = 5
	}
}

// Every text the summary draws, read from the tree of what is drawn.
@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.shownTexts(): List<String> {
	val texts = mutableListOf<String>()
	onRoot(useUnmergedTree = true).fetchSemanticsNode().collectTexts(texts)

	return texts
}

private fun SemanticsNode.collectTexts(into: MutableList<String>) {
	config.getOrNull(SemanticsProperties.Text)?.forEach { text -> into += text.text }
	children.forEach { child -> child.collectTexts(into) }
}
