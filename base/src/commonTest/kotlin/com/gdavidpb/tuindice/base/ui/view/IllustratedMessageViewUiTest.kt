package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class IllustratedMessageViewUiTest {
	@Test
	fun when_actionIsOutlined_then_stillInvokesTheCallback() = runTuIndiceUiTest {
		var clicks = 0

		setTuIndiceTestContent {
			IllustratedMessageView(
				title = "Aún no tienes expediente en la universidad",
				message = "Vuelve a intentarlo en unos días.",
				actionLabel = "Reintentar",
				onActionClick = { clicks++ },
				actionTestTag = "action",
				isActionOutlined = true
			)
		}

		assertNodeVisible("action")
		onNodeWithTag("action").performClick()
		assertEquals(1, clicks)
	}

	@Test
	fun when_noActionLabel_then_rendersNoButton() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			IllustratedMessageView(
				title = "Tu inscripción fue anulada",
				message = "Consulta en DACE.",
				actionTestTag = "action",
				isActionOutlined = true
			)
		}

		assertNodeHidden("action")
	}
}
