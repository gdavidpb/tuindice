package com.gdavidpb.tuindice.base.ui.dialog

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ConfirmationDialogEntryUiTest {
	@Test
	fun when_entryTapped_then_invokesOnClick() = runTuIndiceUiTest {
		var clickCount = 0

		setTuIndiceTestContent {
			ConfirmationDialogEntry(
				icon = testVector(),
				text = "Eliminar",
				onClick = { clickCount++ }
			)
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogEntry)
		onNodeWithText("Eliminar").assertIsDisplayed()
		onNodeWithTag(BaseUiTags.ConfirmationDialogEntry).performClick()
		assertEquals(1, clickCount)
	}

	@Test
	fun when_entryTappedMultipleTimes_then_invokesOnClickForEachTap() = runTuIndiceUiTest {
		var clickCount = 0

		setTuIndiceTestContent {
			ConfirmationDialogEntry(
				icon = testVector(),
				text = "Compartir",
				onClick = { clickCount++ }
			)
		}

		onNodeWithTag(BaseUiTags.ConfirmationDialogEntry).performClick()
		onNodeWithTag(BaseUiTags.ConfirmationDialogEntry).performClick()

		assertEquals(2, clickCount)
	}

	private fun testVector(): ImageVector = ImageVector.Builder(
		name = "testVector",
		defaultWidth = 24.dp,
		defaultHeight = 24.dp,
		viewportWidth = 24f,
		viewportHeight = 24f
	).build()
}
