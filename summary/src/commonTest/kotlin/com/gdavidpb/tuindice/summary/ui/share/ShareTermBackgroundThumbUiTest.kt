package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem
import com.gdavidpb.tuindice.summary.testing.bibliotecaArtwork
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ShareTermBackgroundThumbUiTest {
	@Test
	fun when_itIsChosen_then_itsBoxCarriesTheMarkOfTheChoice() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermBackgroundThumb(
				item = ShareTermBackgroundItem(artwork = bibliotecaArtwork(), selected = true),
				onClick = {}
			)
		}

		onNodeWithTag(ShareTermUiTags.backgroundSelected(BIBLIOTECA_ID)).assertExists()
	}

	@Test
	fun when_itIsNotChosen_then_noBoxCarriesTheMarkOfTheChoice() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermBackgroundThumb(
				item = ShareTermBackgroundItem(artwork = bibliotecaArtwork(), selected = false),
				onClick = {}
			)
		}

		onAllNodesWithTag(ShareTermUiTags.backgroundSelected(BIBLIOTECA_ID)).assertCountEquals(0)
	}

	private companion object {
		const val BIBLIOTECA_ID = "biblioteca-central"
	}
}
