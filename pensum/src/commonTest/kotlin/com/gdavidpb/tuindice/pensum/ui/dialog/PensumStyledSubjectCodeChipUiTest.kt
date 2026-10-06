package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusType
import com.gdavidpb.tuindice.pensum.testing.PensumPixelHost
import com.gdavidpb.tuindice.pensum.testing.assertPixelColor
import com.gdavidpb.tuindice.pensum.testing.pensumHostPixel
import com.gdavidpb.tuindice.pensum.testing.samplePensumVisualStyle
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PensumStyledSubjectCodeChipUiTest {
	@Test
	fun when_codeIsProvided_then_chipShowsItOnTheTaggedNode() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumStyledSubjectCodeChip(
				modifier = Modifier.testTag(CHIP_TAG),
				code = "EA1",
				visualStyle = samplePensumVisualStyle(PensumNodeStatusType.AVAILABLE)
			)
		}

		onNodeWithTag(CHIP_TAG).assertTextEquals("EA1")
	}

	@Test
	fun when_codeHasNoCoursePattern_then_chipUsesTheVisualStyleChipColor() = runTuIndiceUiTest {
		val visualStyle = samplePensumVisualStyle(PensumNodeStatusType.APPROVED)

		setTuIndiceTestContent {
			PensumPixelHost {
				PensumStyledSubjectCodeChip(code = "EA1", visualStyle = visualStyle)
			}
		}

		assertPixelColor(
			expected = Color(visualStyle.chipArgb),
			actual = pensumHostPixel(CHIP_PADDING_X, 0.5f)
		)
	}

	@Test
	fun when_codeIsACourseCode_then_generatedColorWinsOverTheVisualStyle() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumPixelHost {
				PensumStyledSubjectCodeChip(
					code = "CI4325",
					visualStyle = samplePensumVisualStyle(PensumNodeStatusType.APPROVED)
				)
			}
		}

		assertPixelColor(
			expected = checkNotNull(CourseCodeColorGenerator.fromCodeOrNull("CI4325")).containerColor,
			actual = pensumHostPixel(CHIP_PADDING_X, 0.5f)
		)
	}
}

private const val CHIP_TAG = "pensum_styled_subject_code_chip"

// Inside the chip's 10dp horizontal padding: container color, never a glyph.
private const val CHIP_PADDING_X = 0.08f
