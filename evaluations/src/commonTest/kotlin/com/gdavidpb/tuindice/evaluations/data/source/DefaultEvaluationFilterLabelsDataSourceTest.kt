package com.gdavidpb.tuindice.evaluations.data.source

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultEvaluationFilterLabelsDataSourceTest {
	@Test
	fun providesExpectedStateLabels() {
		val provider = DefaultEvaluationFilterLabelsDataSource()

		assertEquals("Pendientes", provider.pending())
		assertEquals("Completadas", provider.completed())
		assertEquals("Sin nota", provider.noGrade())
	}

	@Test
	fun dateLabel_isNotBlankForKnownTimestamp() {
		val provider = DefaultEvaluationFilterLabelsDataSource()

		assertTrue(provider.date(0L).isNotBlank())
	}
}
