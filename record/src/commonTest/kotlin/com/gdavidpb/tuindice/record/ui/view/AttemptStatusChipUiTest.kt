package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.record.testing.fitsWhole
import com.gdavidpb.tuindice.record.testing.textLayout
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AttemptStatusChipUiTest {
	@Test
	fun when_aStatusIsGiven_then_theChipShowsIt_underTheCallersTag_inTheDefaultColor() = runTuIndiceUiTest {
		var onSurface = Color.Unspecified

		setTuIndiceTestContent {
			onSurface = MaterialTheme.colorScheme.onSurface

			AttemptStatusChip(
				modifier = Modifier.testTag(RecordUiTags.attemptStatusChip("attempt-1")),
				text = "Sin acta"
			)
		}

		assertNodeVisible(RecordUiTags.attemptStatusChip("attempt-1"))
		onNodeWithTag(RecordUiTags.attemptStatusChip("attempt-1"))
			.assertTextEquals("Sin acta")
			.assertHasNoClickAction()

		val layout = onNodeWithTag(RecordUiTags.attemptStatusChip("attempt-1")).textLayout()

		// A chip is the badge with nothing overridden: the neutral colours of a read-only status.
		assertEquals(onSurface, layout.layoutInput.style.color)
		assertTrue(layout.fitsWhole, "a status is short enough to be drawn whole")
	}

	@Test
	fun when_twoSubjectsHaveAStatus_then_eachChipSaysItsOwn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Column {
				AttemptStatusChip(
					modifier = Modifier.testTag(RecordUiTags.attemptStatusChip("attempt-1")),
					text = "Retirada"
				)
				AttemptStatusChip(
					modifier = Modifier.testTag(RecordUiTags.attemptStatusChip("attempt-2")),
					text = "Aprobada por equivalencia"
				)
			}
		}

		onNodeWithTag(RecordUiTags.attemptStatusChip("attempt-1")).assertTextEquals("Retirada")
		onNodeWithTag(RecordUiTags.attemptStatusChip("attempt-2")).assertTextEquals("Aprobada por equivalencia")
	}
}
