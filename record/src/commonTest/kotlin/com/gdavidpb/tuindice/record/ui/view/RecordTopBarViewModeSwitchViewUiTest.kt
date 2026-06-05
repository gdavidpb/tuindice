package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RecordTopBarViewModeSwitchViewUiTest {
	@Test
	fun when_projectionModeIsSelected_then_stateDescriptionExplainsIncludedTerms() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordTopBarViewModeSwitchView(
				selectedMode = RecordViewMode.Projection,
				onModeSelected = {}
			)
		}

		onNodeWithTag(RecordUiTags.TopBarViewModeButton).assert(
			SemanticsMatcher.expectValue(
				SemanticsProperties.StateDescription,
				"Modo Proyección: trimestre actual y simulaciones"
			)
		)
	}

	@Test
	fun when_historicalModeIsSelected_then_stateDescriptionExplainsFilteredTerms() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordTopBarViewModeSwitchView(
				selectedMode = RecordViewMode.Historical,
				onModeSelected = {}
			)
		}

		onNodeWithTag(RecordUiTags.TopBarViewModeButton).assert(
			SemanticsMatcher.expectValue(
				SemanticsProperties.StateDescription,
				"Modo Histórico: solo trimestres cerrados"
			)
		)
	}
}
