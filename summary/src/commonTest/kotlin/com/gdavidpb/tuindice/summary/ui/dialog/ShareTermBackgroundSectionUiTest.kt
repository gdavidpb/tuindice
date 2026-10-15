package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareTermArtwork
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem
import com.gdavidpb.tuindice.summary.testing.bibliotecaArtwork
import com.gdavidpb.tuindice.summary.ui.share.ShareTermUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ShareTermBackgroundSectionUiTest {
	@Test
	fun when_thereAreBackgrounds_then_eachHasItsTileInTheStrip() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermBackgroundSection(backgrounds = backgrounds(), onBackgroundClick = {})
		}

		onNodeWithTag(ShareTermUiTags.background(ShareBackground.included.id)).assertExists()
		onNodeWithTag(ShareTermUiTags.background(BIBLIOTECA_ID)).assertExists()
	}

	@Test
	fun when_aTileIsTapped_then_itsBackgroundIsChosen() = runTuIndiceUiTest {
		val chosen = mutableListOf<String>()

		setTuIndiceTestContent {
			ShareTermBackgroundSection(
				backgrounds = backgrounds(),
				onBackgroundClick = { backgroundId -> chosen += backgroundId }
			)
		}
		onNodeWithTag(ShareTermUiTags.background(BIBLIOTECA_ID)).performClick()

		assertEquals(listOf(BIBLIOTECA_ID), chosen)
	}

	private fun backgrounds() = listOf(
		ShareTermBackgroundItem(artwork = ShareBackground.included.toShareTermArtwork(), selected = true),
		ShareTermBackgroundItem(artwork = bibliotecaArtwork(), selected = false)
	)

	private companion object {
		const val BIBLIOTECA_ID = "biblioteca-central"
	}
}
