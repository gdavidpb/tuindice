package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.summary.testing.FakeSettings
import com.gdavidpb.tuindice.summary.utils.PreferencesKeys
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocalSettingsDataSourceTest {
	@Test
	fun when_aBackgroundIsChosen_then_itIsObservedAndStored() = runTest {
		val settings = FakeSettings()
		val dataSource = LocalSettingsDataSource(settings)

		dataSource.setPreferredBackgroundId("primero")

		assertEquals("primero", dataSource.observePreferredBackgroundId().first())
		assertEquals("primero", settings.getStringOrNull(PreferencesKeys.SHARE_BACKGROUND_ID))
	}

	@Test
	fun when_noBackgroundWasChosen_then_thereIsNoPreference() = runTest {
		assertNull(LocalSettingsDataSource(FakeSettings()).observePreferredBackgroundId().first())
	}

	@Test
	fun when_theSessionIsCleared_then_thePreferenceIsReadBackFromTheStore() = runTest {
		val settings = FakeSettings()
		val dataSource = LocalSettingsDataSource(settings)
		dataSource.setPreferredBackgroundId("primero")

		settings.remove(PreferencesKeys.SHARE_BACKGROUND_ID)
		dataSource.clearSessionMemory()

		assertNull(dataSource.observePreferredBackgroundId().first())
	}
}
