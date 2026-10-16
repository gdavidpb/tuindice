package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.domain.model.ShareBackgroundArt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ShareBackgroundSelectionTest {
	@Test
	fun when_thePreferredBackgroundIsInTheList_then_itIsTheOneSelected() {
		val selected = backgrounds().selectShareBackground(preferredId = "segundo")

		assertEquals("segundo", selected?.id)
	}

	@Test
	fun when_thePreferredBackgroundLeftTheList_then_theFirstOneIsSelected() {
		val selected = backgrounds().selectShareBackground(preferredId = "retirado")

		assertEquals(ShareBackground.INCLUDED_ID, selected?.id)
	}

	@Test
	fun when_noBackgroundWasPreferred_then_theFirstOneIsSelected() {
		val selected = backgrounds().selectShareBackground(preferredId = null)

		assertEquals(ShareBackground.INCLUDED_ID, selected?.id)
	}

	@Test
	fun when_thereAreNoBackgrounds_then_nothingIsSelected() {
		assertNull(emptyList<ShareBackground>().selectShareBackground(preferredId = "segundo"))
	}

	private fun backgrounds() = listOf(
		ShareBackground.included,
		background(id = "segundo"),
		background(id = "tercero")
	)

	private fun background(id: String) = ShareBackground(
		id = id,
		title = "Obra $id",
		author = null,
		shortName = "Obra",
		art = ShareBackgroundArt.Remote(cardUrl = "card", thumbUrl = "thumb")
	)
}
