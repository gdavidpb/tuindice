package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusType
import com.gdavidpb.tuindice.pensum.testing.PensumPixelHost
import com.gdavidpb.tuindice.pensum.testing.assertPixelColor
import com.gdavidpb.tuindice.pensum.testing.pensumHostPixel
import com.gdavidpb.tuindice.pensum.testing.samplePensumStatusDisplay
import com.gdavidpb.tuindice.pensum.ui.view.pensumGraphColors
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PensumSubjectRelationStatusMarkerUiTest {
	@Test
	fun when_markerIsRendered_then_occupiesItsTwentyDpSlot() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(modifier = Modifier.testTag(MARKER_TAG)) {
				PensumSubjectRelationStatusMarker(
					status = samplePensumStatusDisplay(PensumNodeStatusType.BLOCKED)
				)
			}
		}

		onNodeWithTag(MARKER_TAG)
			.assertWidthIsEqualTo(20.dp)
			.assertHeightIsEqualTo(20.dp)
	}

	@Test
	fun when_statusIsApproved_then_drawsAnApprovedRingAroundAPanelBackground() = runTuIndiceUiTest {
		var approvedColor = Color.Unspecified
		var panelBackground = Color.Unspecified

		setTuIndiceTestContent {
			PensumPixelHost {
				approvedColor = pensumGraphColors().approved
				panelBackground = pensumGraphColors().panelBackground
				PensumSubjectRelationStatusMarker(
					status = samplePensumStatusDisplay(PensumNodeStatusType.APPROVED)
				)
			}
		}

		assertPixelColor(expected = approvedColor, actual = pensumHostPixel(0.5f, RING_EDGE))
		assertPixelColor(expected = panelBackground, actual = pensumHostPixel(0.5f, INSIDE_RING))
	}

	@Test
	fun when_statusIsCurrent_then_iconFillsTheSlotWithoutAnOuterRing() = runTuIndiceUiTest {
		var currentColor = Color.Unspecified

		setTuIndiceTestContent {
			PensumPixelHost {
				currentColor = pensumGraphColors().current
				PensumSubjectRelationStatusMarker(
					status = samplePensumStatusDisplay(PensumNodeStatusType.CURRENT)
				)
			}
		}

		// The current-route glyph is its own ring: nothing at the slot edge, the
		// glyph's band where a bordered marker would show its background.
		assertPixelColor(expected = Color.White, actual = pensumHostPixel(0.5f, RING_EDGE))
		assertPixelColor(expected = currentColor, actual = pensumHostPixel(0.5f, INSIDE_RING))
		assertPixelColor(expected = currentColor, actual = pensumHostPixel(0.5f, 0.5f))
	}
}

private const val MARKER_TAG = "pensum_subject_relation_status_marker"

// Fractions of the 20dp slot along its vertical axis: inside the 1.2dp border,
// and in the gap between that border and the 13dp glyph.
private const val RING_EDGE = 0.025f
private const val INSIDE_RING = 0.1f
