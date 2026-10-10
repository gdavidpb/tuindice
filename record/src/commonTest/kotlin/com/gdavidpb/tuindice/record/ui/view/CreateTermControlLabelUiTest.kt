package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.testing.fitsWhole
import com.gdavidpb.tuindice.record.testing.textLayout
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CreateTermControlLabelUiTest {
	@Test
	fun when_theLabelFits_then_itIsShownWhole_aboveItsControl() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermControlLabel(text = "Trimestre")
		}

		onNodeWithText("Trimestre")
			.assertIsDisplayed()
			.assertHasNoClickAction()
		assertTrue(onNodeWithText("Trimestre").textLayout().fitsWhole, "a short label is not cut")
	}

	@Test
	fun when_theLabelIsLongerThanItsControl_then_itIsCutOnOneLine_insteadOfWrapping() = runTuIndiceUiTest {
		val label = "Carga académica estimada para el trimestre seleccionado"

		setTuIndiceTestContent {
			Box(modifier = Modifier.width(80.dp)) {
				CreateTermControlLabel(text = label)
			}
		}

		val layout = onNodeWithText(label).textLayout()

		// A second line would push the control down and break the row it shares with its neighbour.
		assertEquals(1, layout.lineCount)
		assertFalse(layout.fitsWhole, "the label that does not fit is cut on its one line")
	}
}
