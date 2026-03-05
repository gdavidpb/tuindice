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
class EvaluationsFailedViewUiTest {
	@Test
	fun when_retryTapped_then_invokesCallback() = runTuIndiceUiTest {
		var retryClicks = 0

		setTuIndiceTestContent {
			EvaluationsFailedView(
				title = "Error",
				message = "No se pudo cargar",
				retryText = "Reintentar",
				onRetryClick = { retryClicks++ }
			)
		}

		assertNodeVisible(BaseUiTags.ErrorViewContainer)
		assertNodeVisible(BaseUiTags.ErrorViewTitle)
		assertNodeVisible(BaseUiTags.ErrorViewMessage)
		assertNodeVisible(BaseUiTags.ErrorViewRetryButton)

		onNodeWithTag(BaseUiTags.ErrorViewRetryButton).performClick()

		assertEquals(1, retryClicks)
	}

	@Test
	fun when_headerContentProvided_then_rendersHeaderContainer() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationsFailedView(
				title = "Error",
				message = "No se pudo cargar",
				retryText = "Reintentar",
				onRetryClick = {},
				headerContent = {
					Box(
						modifier = Modifier
							.size(1.dp)
							.testTag("evaluations_failed_header")
					)
				}
			)
		}

		assertNodeVisible("evaluations_failed_header")
	}
}
