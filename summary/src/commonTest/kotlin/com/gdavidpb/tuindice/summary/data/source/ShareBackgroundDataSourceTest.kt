package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.summary.data.model.ShareBackgroundResponse
import com.gdavidpb.tuindice.summary.data.repository.share.ShareBackgroundRemoteDataRepository
import com.gdavidpb.tuindice.summary.data.repository.share.ShareBackgroundSettingsDataRepository
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.domain.model.ShareBackgroundArt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class ShareBackgroundDataSourceTest {
	@Test
	fun when_theCatalogIsObserved_then_theIncludedBackgroundComesFirstAndTheRestByOrder() = runTest {
		val settings = FakeCatalogSettings(
			catalog = listOf(
				response(id = "tercero", order = 3),
				response(id = "primero", order = 1),
				response(id = "segundo", order = 2)
			)
		)

		val ids = dataSource(settings = settings).observeShareBackgrounds().first().map { background -> background.id }

		assertEquals(listOf(ShareBackground.INCLUDED_ID, "primero", "segundo", "tercero"), ids)
	}

	@Test
	fun when_theCatalogRepeatsTheIncludedBackground_then_itAppearsOnceAsTheAppShipsIt() = runTest {
		val settings = FakeCatalogSettings(
			catalog = listOf(response(id = ShareBackground.INCLUDED_ID, order = 1, title = "Otro título"))
		)

		val backgrounds = dataSource(settings = settings).observeShareBackgrounds().first()

		assertEquals(listOf(ShareBackground.included), backgrounds)
	}

	@Test
	fun when_noCatalogWasSaved_then_onlyTheIncludedBackgroundIsObserved() = runTest {
		val backgrounds = dataSource().observeShareBackgrounds().first()

		assertEquals(listOf(ShareBackground.included), backgrounds)
	}

	@Test
	fun when_theCatalogIsRefreshed_then_thatListIsSavedAndObserved() = runTest {
		val settings = FakeCatalogSettings()
		val remote = FakeRemote { listOf(response(id = "primero", order = 1)) }
		val dataSource = dataSource(remote = remote, settings = settings)

		dataSource.refreshShareBackgrounds()

		assertEquals(1, settings.saves)
		assertEquals(
			listOf(ShareBackground.INCLUDED_ID, "primero"),
			dataSource.observeShareBackgrounds().first().map { background -> background.id }
		)
	}

	@Test
	fun when_theCatalogFails_then_theLastSavedCatalogIsKept() = runTest {
		val remote = FakeRemote { listOf(response(id = "primero", order = 1)) }
		val settings = FakeCatalogSettings()
		val dataSource = dataSource(remote = remote, settings = settings)
		dataSource.refreshShareBackgrounds()

		remote.answer = { throw IllegalStateException("503") }
		dataSource.refreshShareBackgrounds()

		assertEquals(1, settings.saves)
		assertEquals(
			listOf(ShareBackground.INCLUDED_ID, "primero"),
			dataSource.observeShareBackgrounds().first().map { background -> background.id }
		)
	}

	@Test
	fun when_theCatalogFailsWithoutALastCatalog_then_onlyTheIncludedBackgroundIsObserved() = runTest {
		val remote = FakeRemote { throw IllegalStateException("sin red") }
		val dataSource = dataSource(remote = remote)

		dataSource.refreshShareBackgrounds()

		assertEquals(listOf(ShareBackground.included), dataSource.observeShareBackgrounds().first())
	}

	@Test
	fun when_theRefreshIsCancelled_then_theCancellationIsNotHiddenByTheLastCatalog() = runTest {
		val remote = FakeRemote { throw CancellationException("cancelado") }

		assertFailsWith<CancellationException> {
			dataSource(remote = remote).refreshShareBackgrounds()
		}
	}

	@Test
	fun when_aCatalogBackgroundIsServed_then_itsArtIsRemoteWithItsCardAndThumb() = runTest {
		val remote = FakeRemote { listOf(response(id = "primero", order = 1)) }
		val dataSource = dataSource(remote = remote)
		dataSource.refreshShareBackgrounds()

		val background = dataSource.observeShareBackgrounds().first().last()

		assertIs<ShareBackgroundArt.Remote>(background.art)
		assertEquals("https://cdn.example/primero/card.webp", background.art.cardUrl)
	}

	private fun dataSource(
		remote: ShareBackgroundRemoteDataRepository = FakeRemote { emptyList() },
		settings: FakeCatalogSettings = FakeCatalogSettings()
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

	private class FakeCatalogSettings(
		catalog: List<ShareBackgroundResponse>? = null
	) : ShareBackgroundSettingsDataRepository {
		private val lastCatalog = MutableStateFlow(catalog)

		var saves = 0
			private set

		override fun observeLastCatalog(): Flow<List<ShareBackgroundResponse>?> = lastCatalog

		override suspend fun saveLastCatalog(backgrounds: List<ShareBackgroundResponse>) {
			saves++
			lastCatalog.value = backgrounds
		}
	}
}
