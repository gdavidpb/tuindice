package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

// Sizes are read from the layout (SemanticsNode.size), which the pulsing scale does not touch.
@OptIn(ExperimentalTestApi::class)
class PulsingIconHaloUiTest {
	@Test
	fun when_noSizeIsProvided_then_haloMeasuresTheDefaultIconSlot() = runTuIndiceUiTest {
		setTuIndiceTestContent(density = 1f) {
			PulsingIconHalo(
				color = Color.Red,
				testTag = HaloTag
			)
		}

		assertNodeVisible(HaloTag)
		assertEquals(
			expected = IntSize(width = 28, height = 28),
			actual = onNodeWithTag(HaloTag).fetchSemanticsNode().size
		)
	}

	@Test
	fun when_sizeIsProvided_then_haloMeasuresTheRequestedSize() = runTuIndiceUiTest {
		val haloSize = mutableStateOf(40.dp)

		setTuIndiceTestContent(density = 1f) {
			PulsingIconHalo(
				color = Color.Red,
				size = haloSize.value,
				testTag = HaloTag
			)
		}

		assertNodeVisible(HaloTag)
		assertEquals(
			expected = IntSize(width = 40, height = 40),
			actual = onNodeWithTag(HaloTag).fetchSemanticsNode().size
		)

		runOnIdle {
			haloSize.value = 16.dp
		}
		waitForIdle()

		assertEquals(
			expected = IntSize(width = 16, height = 16),
			actual = onNodeWithTag(HaloTag).fetchSemanticsNode().size
		)
	}

	@Test
	fun when_animationsAreDisabled_then_haloIsStillLaidOutAtItsSize() = runTuIndiceUiTest {
		setTuIndiceTestContent(density = 1f) {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				PulsingIconHalo(
					color = Color.Red,
					size = 32.dp,
					testTag = HaloTag
				)
			}
		}

		assertNodeVisible(HaloTag)
		assertEquals(
			expected = IntSize(width = 32, height = 32),
			actual = onNodeWithTag(HaloTag).fetchSemanticsNode().size
		)
	}

	@Test
	fun when_testTagIsOmitted_then_haloKeepsOnlyTheCallerModifier() = runTuIndiceUiTest {
		setTuIndiceTestContent(density = 1f) {
			PulsingIconHalo(
				modifier = Modifier.testTag(HostTag),
				color = Color.Red
			)
		}

		assertNodeVisible(HostTag)
		assertNodeHidden(HaloTag)
		assertEquals(
			expected = IntSize(width = 28, height = 28),
			actual = onNodeWithTag(HostTag).fetchSemanticsNode().size
		)
	}

	private companion object {
		const val HaloTag = "pulsing_icon_halo"
		const val HostTag = "pulsing_icon_halo_host"
	}
}
