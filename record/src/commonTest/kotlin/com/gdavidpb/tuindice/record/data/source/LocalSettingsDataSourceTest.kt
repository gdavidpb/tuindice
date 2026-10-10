package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.testing.FakeSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocalSettingsDataSourceTest {
	@Test
	fun setScheduleViewMode_persistsAndPublishesTheView() = runTest {
		val settings = FakeSettings()
		val dataSource = LocalSettingsDataSource(settings)

		dataSource.setScheduleViewMode(ScheduleViewMode.Week)

		assertEquals(ScheduleViewMode.Week, dataSource.observeScheduleViewMode().first())
		assertEquals(ScheduleViewMode.Week, LocalSettingsDataSource(settings).observeScheduleViewMode().first())
	}

	// The wipe empties the store but this object outlives the session: without the reset the next
	// account would open on what the last one chose.
	@Test
	fun clearSessionMemory_afterTheStoreIsWiped_goesBackToTheDefaults() = runTest {
		val settings = FakeSettings()
		val dataSource = LocalSettingsDataSource(settings)

		dataSource.setScheduleViewMode(ScheduleViewMode.Week)
		dataSource.setRecordViewMode(RecordViewMode.Historical)
		dataSource.setSelectedTermId(RecordViewMode.Historical, "historical-term")
		dataSource.setSelectedTermId(RecordViewMode.Projection, "projection-term")

		settings.clear()
		dataSource.clearSessionMemory()

		assertEquals(ScheduleViewMode.Table, dataSource.observeScheduleViewMode().first())
		assertEquals(RecordViewMode.Projection, dataSource.observeRecordViewMode().first())
		assertNull(dataSource.observeSelectedTermId(RecordViewMode.Historical).first())
		assertNull(dataSource.observeSelectedTermId(RecordViewMode.Projection).first())
	}

	// Memory follows the store, it does not get ahead of it: a wipe that stopped halfway keeps
	// what is still stored.
	@Test
	fun clearSessionMemory_whenTheStoreStillHoldsTheValue_keepsIt() = runTest {
		val settings = FakeSettings()
		val dataSource = LocalSettingsDataSource(settings)

		dataSource.setScheduleViewMode(ScheduleViewMode.Week)
		dataSource.clearSessionMemory()

		assertEquals(ScheduleViewMode.Week, dataSource.observeScheduleViewMode().first())
	}
}
