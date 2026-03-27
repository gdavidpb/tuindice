package com.gdavidpb.tuindice.about.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.about.testing.FakeAboutRepository
import com.gdavidpb.tuindice.about.testing.FakeStoreUrlDataSource
import com.gdavidpb.tuindice.about.testing.CURRENT_PRODUCTION_VERSION_TEXT
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingBrowserRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AboutUseCaseContractTest {
	@Test
	fun loadVersionUseCase_emitsLoadingThenVersionDescription() = runTest {
		val useCase = LoadVersionUseCase(
			aboutRepository = FakeAboutRepository(
				versionDescription = CURRENT_PRODUCTION_VERSION_TEXT
			),
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			val data = awaitLoadingThenData(this)
			assertEquals(CURRENT_PRODUCTION_VERSION_TEXT, data)
			awaitComplete()
		}
	}

	@Test
	fun sendSupportEmailUseCase_emitsEncodedMailtoUri() = runTest {
		val useCase = SendSupportEmailUseCase(
			configRepository = FakeConfigRepository(
				email = "info@tuindice.app",
				subject = "TuIndice Soporte"
			),
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			val data = awaitLoadingThenData(this)
			assertEquals(
				"mailto:info@tuindice.app?subject=TuIndice%20Soporte&body=",
				data
			)

			awaitComplete()
		}
	}

	@Test
	fun openStoreUseCase_emitsResolvedStoreUrl() = runTest {
		val useCase = OpenStoreUseCase(
			storeUrlDataSource = FakeStoreUrlDataSource(
				storeUrl = "itms-apps://apps.apple.com/app/id123"
			),
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			val data = awaitLoadingThenData(this)
			assertEquals("itms-apps://apps.apple.com/app/id123", data)

			awaitComplete()
		}
	}

	@Test
	fun openExternalUrlUseCase_opensBrowserAndEmitsUnitData() = runTest {
		val browserRepository = RecordingBrowserRepository()
		val useCase = OpenExternalUrlUseCase(
			browserRepository = browserRepository,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute("https://tuindice.app/about").test {
			val data = awaitLoadingThenData(this)
			assertEquals(Unit, data)
			assertEquals("https://tuindice.app/about", browserRepository.lastOpenedUrl)

			awaitComplete()
		}
	}
}
