package com.gdavidpb.tuindice.about.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.about.testing.FakeAboutRepository
import com.gdavidpb.tuindice.about.testing.FakeConfigRepository
import com.gdavidpb.tuindice.about.testing.FakeStoreUrlDataSource
import com.gdavidpb.tuindice.about.testing.RecordingBrowserRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AboutUseCaseContractTest {
	@Test
	fun loadVersionUseCase_emitsLoadingThenVersionDescription() = runTest {
		val useCase = LoadVersionUseCase(
			aboutRepository = FakeAboutRepository(
				versionDescription = "Producción v3.5.1 (351)"
			)
		)

		useCase.execute(Unit).test {
			assertTrue(awaitItem() is UseCaseState.Loading<*, *>)

			val data = awaitItem()
			assertTrue(data is UseCaseState.Data<*, *>)
			assertEquals("Producción v3.5.1 (351)", data.value)

			awaitComplete()
		}
	}

	@Test
	fun sendSupportEmailUseCase_emitsEncodedMailtoUri() = runTest {
		val useCase = SendSupportEmailUseCase(
			configRepository = FakeConfigRepository(
				email = "info@tuindice.app",
				subject = "TuIndice Soporte"
			)
		)

		useCase.execute(Unit).test {
			assertTrue(awaitItem() is UseCaseState.Loading<*, *>)

			val data = awaitItem()
			assertTrue(data is UseCaseState.Data<*, *>)
			assertEquals(
				"mailto:info@tuindice.app?subject=TuIndice%20Soporte&body=",
				data.value
			)

			awaitComplete()
		}
	}

	@Test
	fun openStoreUseCase_emitsResolvedStoreUrl() = runTest {
		val useCase = OpenStoreUseCase(
			storeUrlDataSource = FakeStoreUrlDataSource(
				storeUrl = "itms-apps://apps.apple.com/app/id123"
			)
		)

		useCase.execute(Unit).test {
			assertTrue(awaitItem() is UseCaseState.Loading<*, *>)

			val data = awaitItem()
			assertTrue(data is UseCaseState.Data<*, *>)
			assertEquals("itms-apps://apps.apple.com/app/id123", data.value)

			awaitComplete()
		}
	}

	@Test
	fun openExternalUrlUseCase_opensBrowserAndEmitsUnitData() = runTest {
		val browserRepository = RecordingBrowserRepository()
		val useCase = OpenExternalUrlUseCase(
			browserRepository = browserRepository
		)

		useCase.execute("https://tuindice.app/about").test {
			assertTrue(awaitItem() is UseCaseState.Loading<*, *>)

			val data = awaitItem()
			assertTrue(data is UseCaseState.Data<*, *>)
			assertEquals(Unit, data.value)
			assertEquals("https://tuindice.app/about", browserRepository.lastOpenedUrl)

			awaitComplete()
		}
	}
}
