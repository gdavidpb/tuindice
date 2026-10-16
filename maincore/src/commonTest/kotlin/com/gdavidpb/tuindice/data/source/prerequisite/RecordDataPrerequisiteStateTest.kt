package com.gdavidpb.tuindice.data.source.prerequisite

import com.gdavidpb.tuindice.base.domain.model.RecordDataPrerequisiteState
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class RecordDataPrerequisiteStateTest {
	@Test
	fun withTheRecordStored_itIsReady_andNoStatusFailsIt() {
		SyncStatus.entries.forEach { status ->
			assertEquals(
				RecordDataPrerequisiteState(isReady = true, hasFailed = false, isNewStudentWithoutRecord = false),
				recordDataPrerequisiteStateOf(isReady = true, syncStatus = status)
			)
		}
	}

	@Test
	fun withoutTheRecord_aBlockingStatusFailsIt() {
		assertEquals(
			RecordDataPrerequisiteState(isReady = false, hasFailed = true, isNewStudentWithoutRecord = false),
			recordDataPrerequisiteStateOf(isReady = false, syncStatus = SyncStatus.Failed)
		)
	}

	@Test
	fun aNewStudentWithoutRecord_isItsOwnState_andStillBlocksItsReaders() {
		assertEquals(
			RecordDataPrerequisiteState(isReady = false, hasFailed = true, isNewStudentWithoutRecord = true),
			recordDataPrerequisiteStateOf(isReady = false, syncStatus = SyncStatus.NewStudentNoRecord)
		)
	}

	@Test
	fun withoutTheRecord_andAHealthySync_nothingFailsIt() {
		assertEquals(
			RecordDataPrerequisiteState(isReady = false, hasFailed = false, isNewStudentWithoutRecord = false),
			recordDataPrerequisiteStateOf(isReady = false, syncStatus = SyncStatus.Healthy)
		)
	}
}
