package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.pensum.testing.PensumPixelHost
import com.gdavidpb.tuindice.pensum.testing.assertPixelColor
import com.gdavidpb.tuindice.pensum.testing.pensumHostPixel
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PensumSubjectCodeChipUiTest {
	@Test
	fun when_codeIsProvided_then_chipShowsItOnTheTaggedNode() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumSubjectCodeChip(
				code = "MA1111",
				modifier = Modifier.testTag(CHIP_TAG)
			)
		}

		onNodeWithTag(CHIP_TAG).assertTextEquals("MA1111")
	}

	@Test
	fun when_codeIsACourseCode_then_containerUsesItsGeneratedColor() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumPixelHost {
				PensumSubjectCodeChip(
					code = "MA1111",
					fallbackContainer = FALLBACK_CONTAINER,
					fallbackContent = Color.Black
				)
			}
		}

		assertPixelColor(
			expected = checkNotNull(CourseCodeColorGenerator.fromCodeOrNull("MA1111")).containerColor,
			actual = pensumHostPixel(CHIP_PADDING_X, 0.5f)
		)
	}

	@Test
	fun when_codeHasNoCoursePattern_then_containerUsesTheFallbackColor() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumPixelHost {
				PensumSubjectCodeChip(
					code = "EA1",
					fallbackContainer = FALLBACK_CONTAINER,
					fallbackContent = Color.Black
				)
			}
		}

		assertPixelColor(expected = FALLBACK_CONTAINER, actual = pensumHostPixel(CHIP_PADDING_X, 0.5f))
	}
}

private const val CHIP_TAG = "pensum_subject_code_chip"

// Inside the chip's 10dp horizontal padding: container color, never a glyph.
private const val CHIP_PADDING_X = 0.08f

private val FALLBACK_CONTAINER = Color(0xFFFF00FF)
