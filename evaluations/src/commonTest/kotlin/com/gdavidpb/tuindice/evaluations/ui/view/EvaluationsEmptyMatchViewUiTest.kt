package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EvaluationsEmptyMatchViewUiTest {
	@Test
	fun when_rendered_then_displaysEmptyMatchState() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationsEmptyMatchView(
				title = "Sin coincidencias",
				message = "No hay evaluaciones que cumplan con los filtros aplicados"
			)
		}

		assertNodeVisible(BaseUiTags.EmptyViewContainer)
		assertNodeVisible(BaseUiTags.EmptyViewTitle)
		assertNodeVisible(BaseUiTags.EmptyViewMessage)
		assertNodeHidden(BaseUiTags.EmptyViewActionButton)
	}

	@Test
	fun when_headerContentProvided_then_rendersHeaderContainer() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationsEmptyMatchView(
				title = "Sin coincidencias",
				message = "No hay evaluaciones que cumplan con los filtros aplicados",
				headerContent = {
					Box(
						modifier = Modifier
							.size(1.dp)
							.testTag("evaluations_empty_match_header")
					)
				}
			)
		}

		assertNodeVisible("evaluations_empty_match_header")
	}
}
