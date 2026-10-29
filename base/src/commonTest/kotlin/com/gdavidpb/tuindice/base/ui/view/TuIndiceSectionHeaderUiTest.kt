package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class TuIndiceSectionHeaderUiTest {
	@Test
	fun when_headerIsRendered_then_itShowsTheTitleUnderItsTag() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TuIndiceSectionHeader(
				text = "Dependencias",
				modifier = Modifier.testTag(TAG)
			)
		}

		assertNodeVisible(TAG)
		onNodeWithTag(TAG).assertTextEquals("Dependencias")
	}

	@Test
	fun when_headerHasALongTitle_then_theWholeTitleIsKept() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TuIndiceSectionHeader(
				text = "Restricciones con fecha de los trimestres que todavía no empiezan",
				modifier = Modifier.testTag(TAG)
			)
		}

		assertNodeVisible(TAG)
		onNodeWithTag(TAG).assertTextEquals("Restricciones con fecha de los trimestres que todavía no empiezan")
	}

	private companion object {
		const val TAG = "base_test_section_header"
	}
}
