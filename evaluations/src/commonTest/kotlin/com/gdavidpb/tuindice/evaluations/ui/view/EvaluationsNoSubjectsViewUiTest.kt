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
class EvaluationsNoSubjectsViewUiTest {
	@Test
	fun when_rendered_then_displaysNoSubjectsMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationsNoSubjectsView(
				title = "Sin materias",
				message = "No se encontraron materias disponibles"
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
			EvaluationsNoSubjectsView(
				title = "Sin materias",
				message = "No se encontraron materias disponibles",
				headerContent = {
					Box(
						modifier = Modifier
							.size(1.dp)
							.testTag("evaluations_no_subjects_header")
					)
				}
			)
		}

		assertNodeVisible("evaluations_no_subjects_header")
	}
}
