package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.summary.data.model.ShareBackgroundResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ShareBackgroundSettingsDataSourceTest {
	@Test
	fun when_theCatalogIsStored_then_itReadsBackAsTheSameBackgrounds() {
		val backgrounds = listOf(response(id = "primero", order = 1, author = null))

		val stored = encodeShareBackgroundsCatalog(backgrounds)

		assertEquals(backgrounds, decodeShareBackgroundsCatalog(stored))
	}

	@Test
	fun when_theStoredTextIsNoCatalog_then_thereIsNoCatalog() {
		assertNull(decodeShareBackgroundsCatalog("no es un catálogo"))
	}

	@Test
	fun when_theStoredCatalogHasKeysOfANewerVersion_then_itStillReads() {
		val stored = """
			{"backgrounds":[{"id":"primero","order":1,"version":1,"title":"Primero","author":null,
			"shortName":"Corto","card":"card","thumb":"thumb","tono":"claro"}]}
		""".trimIndent()

		assertEquals("primero", decodeShareBackgroundsCatalog(stored)?.single()?.id)
	}

	private fun response(id: String, order: Int, author: String?) = ShareBackgroundResponse(
		id = id,
		order = order,
		version = 1,
		title = "Primero",
		author = author,
		shortName = "Corto",
		card = "card",
		thumb = "thumb"
	)
}
