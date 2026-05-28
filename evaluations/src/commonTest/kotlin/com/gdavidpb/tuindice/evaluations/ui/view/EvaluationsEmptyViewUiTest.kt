package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class EvaluationsEmptyViewUiTest {
	@Test
	fun when_addActionTapped_then_invokesCallback() = runTuIndiceUiTest {
		var addClicks = 0

		setTuIndiceTestContent {
			EvaluationsEmptyView(
				title = "Sin evaluaciones",
				message = "Aun no hay evaluaciones registradas",
				actionLabel = "Agregar",
				onAddEvaluationClick = { addClicks++ }
			)
		}

		assertNodeVisible(BaseUiTags.EmptyViewContainer)
		assertNodeVisible(BaseUiTags.EmptyViewTitle)
		assertNodeVisible(BaseUiTags.EmptyViewMessage)
		assertNodeVisible(BaseUiTags.EmptyViewActionButton)

		onNodeWithTag(BaseUiTags.EmptyViewActionButton).performClick()

		assertEquals(1, addClicks)
	}

	@Test
	fun when_headerContentProvided_then_rendersHeaderContainer() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationsEmptyView(
				title = "Sin evaluaciones",
				message = "Sin contenido",
				actionLabel = "Agregar",
				onAddEvaluationClick = {},
				headerContent = {
					Box(
						modifier = Modifier
							.size(1.dp)
							.testTag("evaluations_empty_header")
					)
				}
			)
		}

		assertNodeVisible("evaluations_empty_header")
	}
}
