package com.gdavidpb.tuindice.pensum.data.source

import com.gdavidpb.tuindice.pensum.testing.FakeSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalPensumSettingsDataSourceTest {
	@Test
	fun defaultSummaryState_isExpanded() = runTest {
		val dataSource = LocalPensumSettingsDataSource(FakeSettings())

		assertFalse(dataSource.isSummaryCollapsed())
		assertFalse(dataSource.observeSummaryCollapsed().first())
	}

	@Test
	fun setSummaryCollapsed_persistsAndPublishesState() = runTest {
		val settings = FakeSettings()
		val dataSource = LocalPensumSettingsDataSource(settings)

		dataSource.setSummaryCollapsed(true)

		assertTrue(dataSource.isSummaryCollapsed())
		assertTrue(dataSource.observeSummaryCollapsed().first())
		assertTrue(LocalPensumSettingsDataSource(settings).isSummaryCollapsed())
	}
}
