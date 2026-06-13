package com.gdavidpb.tuindice.record.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class RecordViewModeTest {
	@Test
	fun fromStorageValue_returnsMatchingMode_forKnownValues() {
		assertEquals(RecordViewMode.Historical, RecordViewMode.fromStorageValue("historical"))
		assertEquals(RecordViewMode.Projection, RecordViewMode.fromStorageValue("projection"))
	}

	@Test
	fun fromStorageValue_fallsBackToProjection_forUnknownOrNullValues() {
		assertEquals(RecordViewMode.Projection, RecordViewMode.fromStorageValue(null))
		assertEquals(RecordViewMode.Projection, RecordViewMode.fromStorageValue(""))
		assertEquals(RecordViewMode.Projection, RecordViewMode.fromStorageValue("HISTORICAL"))
		assertEquals(RecordViewMode.Projection, RecordViewMode.fromStorageValue("legacy"))
	}

	@Test
	fun other_togglesBetweenModes() {
		assertEquals(RecordViewMode.Projection, RecordViewMode.Historical.other())
		assertEquals(RecordViewMode.Historical, RecordViewMode.Projection.other())
	}
}
