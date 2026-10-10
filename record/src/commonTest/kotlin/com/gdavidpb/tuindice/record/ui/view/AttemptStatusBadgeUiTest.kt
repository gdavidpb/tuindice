package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.record.testing.textLayout
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalTestApi::class)
class AttemptStatusBadgeUiTest {
	@Test
	fun when_noColorsAreGiven_then_theTextIsDrawnInTheSurfacesOwnColor() = runTuIndiceUiTest {
		var onSurface = Color.Unspecified

		setTuIndiceTestContent {
			onSurface = MaterialTheme.colorScheme.onSurface

			AttemptStatusBadge(text = "Aprobada")
		}

		onNodeWithText("Aprobada")
			.assertIsDisplayed()
			// A badge states a status; it is never the thing to tap.
			.assertHasNoClickAction()
		assertEquals(onSurface, onNodeWithText("Aprobada").textLayout().layoutInput.style.color)
	}

	@Test
	fun when_aContentColorIsGiven_then_theTextTakesIt_insteadOfTheDefault() = runTuIndiceUiTest {
		var onSurface = Color.Unspecified

		setTuIndiceTestContent {
			onSurface = MaterialTheme.colorScheme.onSurface

			AttemptStatusBadge(
				modifier = Modifier.testTag("badge"),
				text = "Seleccionar",
				containerColor = Color.Yellow,
				contentColor = Color.Magenta
			)
		}

		// The modifier lands on the badge itself, so a caller's tag reads its text.
		onNodeWithTag("badge").assertTextEquals("Seleccionar")

		val color = onNodeWithTag("badge").textLayout().layoutInput.style.color

		assertEquals(Color.Magenta, color)
		assertNotEquals(onSurface, color)
	}
}
