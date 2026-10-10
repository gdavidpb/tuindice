package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.pensum.testing.PensumPixelHost
import com.gdavidpb.tuindice.pensum.testing.assertPixelColor
import com.gdavidpb.tuindice.pensum.testing.pensumHostPixel
import com.gdavidpb.tuindice.pensum.ui.view.pensumGraphColors
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PensumRouteConnectorUiTest {
	@Test
	fun when_connectorIsRendered_then_reservesTopPaddingAboveItsStroke() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(modifier = Modifier.testTag(CONNECTOR_TAG)) {
				PensumRouteConnector(modifier = Modifier.width(SubjectDetailRouteConnectorWidth))
			}
		}

		onNodeWithTag(CONNECTOR_TAG)
			.assertWidthIsEqualTo(SubjectDetailRouteConnectorWidth)
			.assertHeightIsEqualTo(
				SubjectDetailRouteConnectorTopPadding + SubjectDetailRouteConnectorStrokeWidth
			)
	}

	@Test
	fun when_connectorIsDrawn_then_strokeUsesSelectedRouteColorOnlyBelowThePadding() = runTuIndiceUiTest {
		var selectedColor = Color.Unspecified

		setTuIndiceTestContent {
			PensumPixelHost {
				selectedColor = pensumGraphColors().selected
				PensumRouteConnector(modifier = Modifier.width(SubjectDetailRouteConnectorWidth))
			}
		}

		// The host is 56dp tall: 52dp of padding, then the 4dp stroke.
		assertPixelColor(expected = selectedColor, actual = pensumHostPixel(0.5f, STROKE_CENTER))
		assertPixelColor(expected = Color.White, actual = pensumHostPixel(0.5f, PADDING_CENTER))
	}
}

private const val CONNECTOR_TAG = "pensum_route_connector_host"
private const val STROKE_CENTER = 54f / 56f
private const val PADDING_CENTER = 26f / 56f
