package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SubjectDetailLoadingViewUiTest {
	@Test
	fun when_rendered_then_displaysTitleAndMessageInTheirTaggedNodes() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailLoadingView(
				title = "Calculando estadísticas",
				message = "Estamos cruzando notas de esta materia..."
			)
		}

		assertNodeVisible(SubjectsUiTags.Loading)
		onNodeWithTag(SubjectsUiTags.LoadingTitle)
			.assertTextEquals("Calculando estadísticas")
		onNodeWithTag(SubjectsUiTags.LoadingMessage)
			.assertTextEquals("Estamos cruzando notas de esta materia...")
	}

	@Test
	fun when_rendered_then_displaysStatsLoadingAnimationAsHeader() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailLoadingView(
				title = "Titulo",
				message = "Mensaje"
			)
		}

		assertNodeVisible(BaseUiTags.StatsLoadingAnimation)
		assertNodeVisible(SubjectsUiTags.LoadingTitle)
		assertNodeVisible(SubjectsUiTags.LoadingMessage)
	}
}
