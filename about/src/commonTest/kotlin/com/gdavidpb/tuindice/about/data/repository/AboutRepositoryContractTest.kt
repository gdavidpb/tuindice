package com.gdavidpb.tuindice.about.data.repository

import com.gdavidpb.tuindice.about.data.source.AboutDataSource
import com.gdavidpb.tuindice.about.testing.FakeAppInfoDataSource
import com.gdavidpb.tuindice.about.testing.FakeEnvironmentDataSource
import com.gdavidpb.tuindice.about.testing.CURRENT_APP_VERSION_CODE
import com.gdavidpb.tuindice.about.testing.CURRENT_APP_VERSION_NAME
import com.gdavidpb.tuindice.about.testing.CURRENT_PRODUCTION_VERSION_TEXT
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AboutRepositoryContractTest {
	@Test
	fun getVersionDescription_returnsDebugLabelWithVersionData() = runTest {
		val repository = AboutDataSource(
			environmentDataSource = FakeEnvironmentDataSource(isDebug = true),
			appInfoDataSource = FakeAppInfoDataSource(
				versionName = "9.9.9",
				versionCode = 999L
			)
		)

		assertEquals("Desarrollo v9.9.9 (999)", repository.getVersionDescription())
	}

	@Test
	fun getVersionDescription_returnsProductionLabelWithVersionData() = runTest {
		val repository = AboutDataSource(
			environmentDataSource = FakeEnvironmentDataSource(isDebug = false),
			appInfoDataSource = FakeAppInfoDataSource(
				versionName = CURRENT_APP_VERSION_NAME,
				versionCode = CURRENT_APP_VERSION_CODE
			)
		)

		assertEquals(CURRENT_PRODUCTION_VERSION_TEXT, repository.getVersionDescription())
	}
}
