package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ScheduleEvaluationMarkIconUiTest {
	@Test
	fun when_itIsShown_then_itIsTheSmallIconSize() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleEvaluationMarkIcon(modifier = Modifier.testTag(MARK_TAG))
		}

		val bounds = onNodeWithTag(MARK_TAG).apply { assertIsDisplayed() }.getUnclippedBoundsInRoot()

		assertEquals(TuIndiceComponentSizes.IconSmall, bounds.width)
		assertEquals(TuIndiceComponentSizes.IconSmall, bounds.height)
	}

	@Test
	fun when_itIsShown_then_itIsDecorative_andTintingItKeepsItsSize() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleEvaluationMarkIcon(modifier = Modifier.testTag(MARK_TAG), tint = Color.Red)
		}

		// Whoever draws it says in words what it means: the icon has nothing to announce.
		onNodeWithTag(MARK_TAG).assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
		assertEquals(
			TuIndiceComponentSizes.IconSmall,
			onNodeWithTag(MARK_TAG).getUnclippedBoundsInRoot().width
		)
	}

	private companion object {
		const val MARK_TAG = "schedule_evaluation_mark_icon"
	}
}
