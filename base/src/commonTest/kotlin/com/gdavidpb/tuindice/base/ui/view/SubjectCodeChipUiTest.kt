package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.model.SubjectCodeChipVariant
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Sizes are compared in pixels at density 1, so one dp is one pixel.
@OptIn(ExperimentalTestApi::class)
class SubjectCodeChipUiTest {
	@Test
	fun when_subjectCodeIsProvided_then_displaysItOnASingleLine() = runTuIndiceUiTest {
		setTuIndiceTestContent(density = 1f) {
			Column(modifier = Modifier.width(80.dp)) {
				SubjectCodeChip(
					modifier = Modifier.testTag(ShortChipTag),
					subjectCode = "CI2511"
				)
				SubjectCodeChip(
					modifier = Modifier.testTag(LongChipTag),
					subjectCode = "CI2511 CI2512 CI2513 CI2514"
				)
			}
		}

		onNodeWithText("CI2511").assertIsDisplayed()
		onNodeWithTag(ShortChipTag).assertTextEquals("CI2511")
		// A code wider than its slot is cut, never wrapped into a taller chip.
		assertEquals(chipSize(ShortChipTag).height, chipSize(LongChipTag).height)
		assertTrue(chipSize(LongChipTag).width <= 80)
	}

	@Test
	fun when_variantIsDense_then_enforcesItsMinimumHeight() = runTuIndiceUiTest {
		setTuIndiceTestContent(density = 1f) {
			Column(horizontalAlignment = Alignment.Start) {
				SubjectCodeChip(
					modifier = Modifier.testTag(DenseChipTag),
					subjectCode = "MA1111",
					variant = SubjectCodeChipVariant.Dense,
					verticalPadding = 0.dp
				)
				// Same typography as Dense, without the minimum height.
				SubjectCodeChip(
					modifier = Modifier.testTag(GraphNodeChipTag),
					subjectCode = "MA1111",
					variant = SubjectCodeChipVariant.GraphNode,
					verticalPadding = 0.dp
				)
			}
		}

		assertNodeVisible(DenseChipTag)
		assertEquals(28, chipSize(DenseChipTag).height)
		assertTrue(chipSize(GraphNodeChipTag).height < 28)
		assertEquals(chipSize(GraphNodeChipTag).width, chipSize(DenseChipTag).width)
	}

	@Test
	fun when_paddingOverridesAreProvided_then_theyReplaceTheVariantDefaults() = runTuIndiceUiTest {
		setTuIndiceTestContent(density = 1f) {
			Column(horizontalAlignment = Alignment.Start) {
				SubjectCodeChip(
					modifier = Modifier.testTag(DefaultChipTag),
					subjectCode = "FS1111"
				)
				SubjectCodeChip(
					modifier = Modifier.testTag(PaddedChipTag),
					subjectCode = "FS1111",
					horizontalPadding = 24.dp,
					verticalPadding = 14.dp
				)
			}
		}

		val defaultSize = chipSize(DefaultChipTag)
		val paddedSize = chipSize(PaddedChipTag)

		// Default variant pads 10dp x 6dp: the overrides add 14dp and 8dp per side.
		assertEquals(defaultSize.width + 28, paddedSize.width)
		assertEquals(defaultSize.height + 16, paddedSize.height)
	}

	private fun ComposeUiTest.chipSize(tag: String): IntSize {
		return onNodeWithTag(tag).fetchSemanticsNode().size
	}

	private companion object {
		const val ShortChipTag = "subject_code_chip_short"
		const val LongChipTag = "subject_code_chip_long"
		const val DenseChipTag = "subject_code_chip_dense"
		const val GraphNodeChipTag = "subject_code_chip_graph_node"
		const val DefaultChipTag = "subject_code_chip_default"
		const val PaddedChipTag = "subject_code_chip_padded"
	}
}
