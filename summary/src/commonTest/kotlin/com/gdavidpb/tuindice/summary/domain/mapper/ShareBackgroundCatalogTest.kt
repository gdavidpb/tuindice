package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.domain.model.ShareBackgroundArt
import kotlin.test.Test
import kotlin.test.assertEquals

class ShareBackgroundCatalogTest {
	@Test
	fun when_theCatalogIsGiven_then_theIncludedBackgroundComesFirstAndTheRestKeepTheirOrder() {
		val catalog = listOf(background("tercero"), background("primero"), background("segundo"))

		val ids = shareBackgroundsOf(catalog).map { background -> background.id }

		assertEquals(listOf(ShareBackground.INCLUDED_ID, "tercero", "primero", "segundo"), ids)
	}

	@Test
	fun when_theCatalogRepeatsTheIncludedBackground_then_itAppearsOnceAsTheAppShipsIt() {
		val catalog = listOf(background(ShareBackground.INCLUDED_ID, title = "Otro título"))

		assertEquals(listOf(ShareBackground.included), shareBackgroundsOf(catalog))
	}

	@Test
	fun when_theCatalogIsEmpty_then_onlyTheIncludedBackgroundIsServed() {
		assertEquals(listOf(ShareBackground.included), shareBackgroundsOf(emptyList()))
	}

	private fun background(id: String, title: String = "Título $id") = ShareBackground(
		id = id,
		title = title,
		author = null,
		shortName = "Corto",
		art = ShareBackgroundArt.Bundled
	)
}
