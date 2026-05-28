package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class EmptyViewUiTest {
	@Test
	fun when_emptyViewHasAction_then_displaysContentAndInvokesCallback() = runTuIndiceUiTest {
		var clickCount = 0

		setTuIndiceTestContent {
			EmptyView(
				title = "Sin resultados",
				message = "No hay elementos para mostrar",
				actionLabel = "Reintentar",
				onActionClick = { clickCount++ }
			)
		}

		assertNodeVisible(BaseUiTags.EmptyViewContainer)
		assertNodeVisible(BaseUiTags.EmptyViewTitle)
		assertNodeVisible(BaseUiTags.EmptyViewMessage)
		assertNodeVisible(BaseUiTags.EmptyViewActionButton)

		onNodeWithTag(BaseUiTags.EmptyViewActionButton).performClick()
		assertEquals(1, clickCount)
	}

	@Test
	fun when_emptyViewHasNoAction_then_hidesActionButton() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EmptyView(
				title = "Sin resultados",
				message = "No hay elementos para mostrar",
				actionLabel = null
			)
		}

		assertNodeVisible(BaseUiTags.EmptyViewTitle)
		assertNodeVisible(BaseUiTags.EmptyViewMessage)
		assertNodeHidden(BaseUiTags.EmptyViewActionButton)
	}

	@Test
	fun when_headerContentProvided_then_rendersHeaderSlot() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EmptyView(
				title = "Sin resultados",
				message = "No hay elementos para mostrar",
				headerContent = {
					Text("Header slot")
				}
			)
		}

		onNodeWithText("Header slot").assertIsDisplayed()
	}
}
