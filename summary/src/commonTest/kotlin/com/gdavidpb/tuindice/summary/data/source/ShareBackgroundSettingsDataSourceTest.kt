package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.summary.data.model.ShareBackgroundResponse
import com.gdavidpb.tuindice.summary.testing.FakeSettings
import com.gdavidpb.tuindice.summary.utils.PreferencesKeys
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
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

	@Test
	fun when_aCatalogIsSaved_then_itIsObservedAndStored() = runTest {
		val backgrounds = listOf(response(id = "primero", order = 1, author = null))
		val settings = FakeSettings()
		val dataSource = ShareBackgroundSettingsDataSource(settings)

		dataSource.saveLastCatalog(backgrounds)

		assertEquals(backgrounds, dataSource.observeLastCatalog().first())
		assertEquals(
			backgrounds,
			settings.getStringOrNull(PreferencesKeys.SHARE_BACKGROUNDS_CATALOG)?.let(::decodeShareBackgroundsCatalog)
		)
	}

	@Test
	fun when_aCatalogWasSavedBefore_then_aNewSourceObservesIt() = runTest {
		val backgrounds = listOf(response(id = "primero", order = 1, author = null))
		val settings = FakeSettings()
		ShareBackgroundSettingsDataSource(settings).saveLastCatalog(backgrounds)

		val observed = ShareBackgroundSettingsDataSource(settings).observeLastCatalog().first()

		assertEquals(backgrounds, observed)
	}

	@Test
	fun when_theSessionIsCleared_then_theCatalogIsReadBackFromTheStore() = runTest {
		val settings = FakeSettings()
		val dataSource = ShareBackgroundSettingsDataSource(settings)
		dataSource.saveLastCatalog(listOf(response(id = "primero", order = 1, author = null)))

		settings.remove(PreferencesKeys.SHARE_BACKGROUNDS_CATALOG)
		dataSource.clearSessionMemory()

		assertNull(dataSource.observeLastCatalog().first())
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
