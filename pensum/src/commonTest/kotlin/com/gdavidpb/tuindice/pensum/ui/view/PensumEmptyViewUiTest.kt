package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PensumEmptyViewUiTest {
	@Test
	fun when_noActionLabelIsProvided_then_showsIllustratedTitleAndMessageOnly() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumEmptyView(
				title = "Pensum no disponible",
				message = "No encontramos un pensum para esta carrera."
			)
		}

		assertNodeVisible(BaseUiTags.EmptyViewContainer)
		assertNodeVisible(BaseUiTags.EmptyStateAnimation)
		onNodeWithTag(BaseUiTags.EmptyViewTitle).assertTextEquals("Pensum no disponible")
		onNodeWithTag(BaseUiTags.EmptyViewMessage)
			.assertTextEquals("No encontramos un pensum para esta carrera.")
		assertNodeHidden(BaseUiTags.EmptyViewActionButton)
	}

	@Test
	fun when_actionLabelIsProvided_then_actionButtonInvokesTheCallback() = runTuIndiceUiTest {
		var actionClickCount = 0

		setTuIndiceTestContent {
			PensumEmptyView(
				title = "Historial no sincronizado",
				message = "No pudimos leer tu historial académico.",
				actionLabel = "Reintentar",
				onActionClick = { actionClickCount += 1 }
			)
		}

		onNodeWithTag(BaseUiTags.EmptyViewActionButton)
			.assertTextEquals("Reintentar")
			.assertHasClickAction()
			.performClick()

		runOnIdle { assertEquals(1, actionClickCount) }
	}
}
