package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PensumRefreshingIndicatorViewUiTest {
	@Test
	fun when_indicatorIsRendered_then_showsTheRefreshingLabelInsideItsContainer() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumRefreshingIndicatorView()
		}

		assertNodeVisible(PensumUiTags.RefreshingIndicator)
		onNode(
			hasText("Actualizando pensum") and hasParent(hasTestTag(PensumUiTags.RefreshingIndicator))
		).assertExists()
	}

	@Test
	fun when_indicatorIsRendered_then_exposesAnIndeterminateProgressBar() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumRefreshingIndicatorView(modifier = Modifier.padding(16.dp))
		}

		onNode(
			SemanticsMatcher.expectValue(
				SemanticsProperties.ProgressBarRangeInfo,
				ProgressBarRangeInfo.Indeterminate
			) and hasParent(hasTestTag(PensumUiTags.RefreshingIndicator))
		).assertExists()
	}
}
