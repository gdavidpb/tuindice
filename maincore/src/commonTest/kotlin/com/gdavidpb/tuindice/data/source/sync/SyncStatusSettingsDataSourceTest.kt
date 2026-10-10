package com.gdavidpb.tuindice.data.source.sync

import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.testing.FakeSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SyncStatusSettingsDataSourceTest {
	// A report persisted by a build that predates `enrollment_read_at` and `situation` must still
	// load: falling back to success() would hide an unavailable source.
	@Test
	fun getSyncReport_decodesAReportPersistedBeforeTheNewFields() = runTest {
		val settings = FakeSettings()

		settings.putString(
			key = "syncReport",
			value = """{"status":"partial","sources":{"record":{"status":"success"},""" +
				""""enrollment":{"status":"unavailable"}}}"""
		)

		val report = SyncStatusSettingsDataSource(settings).getSyncReport()

		assertEquals(SyncSourceStatus.Unavailable, report.sources.enrollment.status)
		assertNull(report.sources.enrollment.situation)
		assertNull(report.enrollmentReadAt)
	}

	// Signing in over another account's data wipes the stores without calling reset().
	@Test
	fun clearSessionMemory_afterTheStoreIsWiped_goesBackToAHealthyEmptyStatus() = runTest {
		val settings = FakeSettings()
		val dataSource = SyncStatusSettingsDataSource(settings)

		dataSource.setSyncStatus(SyncStatus.Failed)
		dataSource.setLastSuccessfulSyncAt(1_700_000_000_000)
		settings.clear()
		dataSource.clearSessionMemory()

		assertEquals(SyncStatus.Healthy, dataSource.getSyncStatus())
		assertNull(dataSource.getLastSuccessfulSyncAt())
		assertEquals(SyncReport.success(), dataSource.getSyncReport())
	}
}
