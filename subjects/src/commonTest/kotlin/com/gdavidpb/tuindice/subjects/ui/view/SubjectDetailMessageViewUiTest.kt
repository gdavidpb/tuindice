package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SubjectDetailMessageViewUiTest {
	@Test
	fun when_rendered_then_displaysTitleAndBodyExactlyOnce() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailMessageView(
				modifier = Modifier.testTag(SubjectsUiTags.Unavailable),
				title = "Sin datos suficientes",
				body = "Cuando haya más historial podrás ver estadísticas.",
				actionText = "Cerrar",
				onActionClick = {}
			)
		}

		assertNodeVisible(SubjectsUiTags.Unavailable)
		onNodeWithText("Sin datos suficientes").assertIsDisplayed()
		onAllNodesWithText("Cuando haya más historial podrás ver estadísticas.")
			.assertCountEquals(1)
	}

	@Test
	fun when_actionTestTagProvided_then_taggedActionInvokesCallback() = runTuIndiceUiTest {
		var actionClicks = 0

		setTuIndiceTestContent {
			SubjectDetailMessageView(
				title = "No pudimos calcular estadísticas",
				body = "Intenta de nuevo.",
				actionText = "Reintentar",
				onActionClick = { actionClicks++ },
				actionTestTag = SubjectsUiTags.Retry
			)
		}

		onNodeWithTag(SubjectsUiTags.Retry)
			.assertTextEquals("Reintentar")
			.performClick()

		assertEquals(1, actionClicks)
	}

	@Test
	fun when_actionTestTagIsNull_then_actionIsReachableByItsText() = runTuIndiceUiTest {
		var actionClicks = 0

		setTuIndiceTestContent {
			SubjectDetailMessageView(
				title = "Sin datos suficientes",
				body = "Vuelve más tarde.",
				actionText = "Cerrar",
				onActionClick = { actionClicks++ }
			)
		}

		onNodeWithText("Cerrar").performClick()

		assertEquals(1, actionClicks)
	}

	@Test
	fun when_headerContentProvided_then_rendersItAboveTheMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailMessageView(
				title = "Sin datos suficientes",
				body = "Vuelve más tarde.",
				actionText = "Cerrar",
				onActionClick = {},
				headerContent = {
					Text(
						modifier = Modifier.testTag(HEADER_TAG),
						text = "Ilustracion"
					)
				}
			)
		}

		val headerBounds = onNodeWithTag(HEADER_TAG).fetchSemanticsNode().boundsInRoot
		val titleBounds = onNodeWithText("Sin datos suficientes").fetchSemanticsNode().boundsInRoot

		assertEquals(true, headerBounds.bottom <= titleBounds.top)
	}

	private companion object {
		const val HEADER_TAG = "message_header"
	}
}
