package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.summary.data.model.ShareBackgroundResponse
import com.gdavidpb.tuindice.summary.data.repository.share.ShareBackgroundRemoteDataRepository
import com.gdavidpb.tuindice.summary.data.repository.share.ShareBackgroundSettingsDataRepository
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.domain.model.ShareBackgroundArt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class ShareBackgroundDataSourceTest {
	@Test
	fun when_theCatalogIsRead_then_theIncludedBackgroundComesFirstAndTheRestByOrder() = runTest {
		val remote = FakeRemote {
			listOf(response(id = "tercero", order = 3), response(id = "primero", order = 1), response(id = "segundo", order = 2))
		}

		val ids = dataSource(remote = remote).getShareBackgrounds().map { background -> background.id }

		assertEquals(listOf(ShareBackground.INCLUDED_ID, "primero", "segundo", "tercero"), ids)
	}

	@Test
	fun when_theCatalogRepeatsTheIncludedBackground_then_itAppearsOnceAsTheAppShipsIt() = runTest {
		val remote = FakeRemote {
			listOf(response(id = ShareBackground.INCLUDED_ID, order = 1, title = "Otro título"))
		}

		val backgrounds = dataSource(remote = remote).getShareBackgrounds()

		assertEquals(listOf(ShareBackground.included), backgrounds)
	}

	@Test
	fun when_theCatalogIsRead_then_thatListIsKeptAsTheLastCatalog() = runTest {
		val settings = FakeSettings()
		val catalog = listOf(response(id = "primero", order = 1))

		dataSource(remote = FakeRemote { catalog }, settings = settings).getShareBackgrounds()

		assertEquals(catalog, settings.lastCatalog)
	}

	@Test
	fun when_theCatalogFails_then_theLastCatalogThatWasReadIsServed() = runTest {
		val remote = FakeRemote { listOf(response(id = "primero", order = 1)) }
		val dataSource = dataSource(remote = remote)
		dataSource.getShareBackgrounds()

		remote.answer = { throw IllegalStateException("503") }

		val ids = dataSource.getShareBackgrounds().map { background -> background.id }

		assertEquals(listOf(ShareBackground.INCLUDED_ID, "primero"), ids)
	}

	@Test
	fun when_theCatalogFailsWithoutALastCatalog_then_onlyTheIncludedBackgroundIsServed() = runTest {
		val remote = FakeRemote { throw IllegalStateException("sin red") }

		val backgrounds = dataSource(remote = remote).getShareBackgrounds()

		assertEquals(listOf(ShareBackground.included), backgrounds)
	}

	@Test
	fun when_theReadIsCancelled_then_theCancellationIsNotHiddenByTheLastCatalog() = runTest {
		val settings = FakeSettings().apply {
			lastCatalog = listOf(response(id = "primero", order = 1))
		}
		val remote = FakeRemote { throw CancellationException("cancelado") }

		assertFailsWith<CancellationException> {
			dataSource(remote = remote, settings = settings).getShareBackgrounds()
		}
	}

	@Test
	fun when_aBackgroundIsPreferred_then_thatPreferenceIsWhatIsRead() = runTest {
		val dataSource = dataSource(remote = FakeRemote { emptyList() })

		dataSource.setPreferredBackgroundId("primero")

		assertEquals("primero", dataSource.getPreferredBackgroundId())
	}

	@Test
	fun when_noBackgroundWasPreferred_then_thereIsNoPreference() = runTest {
		assertNull(dataSource(remote = FakeRemote { emptyList() }).getPreferredBackgroundId())
	}

	@Test
	fun when_aCatalogBackgroundIsServed_then_itsArtIsRemoteWithItsCardAndThumb() = runTest {
		val remote = FakeRemote { listOf(response(id = "primero", order = 1)) }

		val background = dataSource(remote = remote).getShareBackgrounds().last()

		assertIs<ShareBackgroundArt.Remote>(background.art)
		assertEquals("https://cdn.example/primero/card.webp", background.art.cardUrl)
	}

	private fun dataSource(
		remote: ShareBackgroundRemoteDataRepository,
		settings: ShareBackgroundSettingsDataRepository = FakeSettings()
	) = ShareBackgroundDataSource(remoteDataSource = remote, settingsDataSource = settings)

	private fun response(id: String, order: Int, title: String = "Título $id") = ShareBackgroundResponse(
		id = id,
		order = order,
		version = 1,
		title = title,
		author = "Autor de $id",
		shortName = "Corto",
		card = "https://cdn.example/$id/card.webp",
		thumb = "https://cdn.example/$id/thumb.webp"
	)

	private class FakeRemote(
		var answer: suspend () -> List<ShareBackgroundResponse>
	) : ShareBackgroundRemoteDataRepository {
		override suspend fun getShareBackgrounds(): List<ShareBackgroundResponse> = answer()
	}

	private class FakeSettings : ShareBackgroundSettingsDataRepository {
		var lastCatalog: List<ShareBackgroundResponse>? = null
		var preferredId: String? = null

		override suspend fun getLastCatalog() = lastCatalog

		override suspend fun saveLastCatalog(backgrounds: List<ShareBackgroundResponse>) {
			lastCatalog = backgrounds
		}

		override suspend fun getPreferredBackgroundId() = preferredId

		override suspend fun setPreferredBackgroundId(id: String) {
			preferredId = id
		}
	}
}
