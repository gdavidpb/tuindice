package com.gdavidpb.tuindice.about.data.repository

import com.gdavidpb.tuindice.about.testing.FakeAppInfoDataSource
import com.gdavidpb.tuindice.about.testing.FakeEnvironmentDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AboutRepositoryContractTest {
	@Test
	fun getVersionDescription_returnsDebugLabelWithVersionData() = runTest {
		val repository = AboutDataRepository(
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
		val repository = AboutDataRepository(
			environmentDataSource = FakeEnvironmentDataSource(isDebug = false),
			appInfoDataSource = FakeAppInfoDataSource(
				versionName = "3.5.1",
				versionCode = 351L
			)
		)

		assertEquals("Producción v3.5.1 (351)", repository.getVersionDescription())
	}
}
