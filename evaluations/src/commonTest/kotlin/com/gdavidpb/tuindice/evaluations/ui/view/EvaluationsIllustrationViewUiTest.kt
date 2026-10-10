package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsIllustration
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EvaluationsIllustrationViewUiTest {
	@Test
	fun when_illustrationIsEmpty_then_paintsTheEmptyArt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationsIllustrationView(illustration = EvaluationsIllustration.Empty)
		}

		assertNodeVisible(BaseUiTags.EmptyStateAnimation)
		assertNodeHidden(BaseUiTags.ErrorStateAnimation)
	}

	@Test
	fun when_illustrationIsError_then_paintsTheErrorArt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationsIllustrationView(illustration = EvaluationsIllustration.Error)
		}

		assertNodeVisible(BaseUiTags.ErrorStateAnimation)
		assertNodeHidden(BaseUiTags.EmptyStateAnimation)
	}
}
