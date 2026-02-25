package com.gdavidpb.tuindice.summary.presentation.resource

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultSummaryItemsTextProviderTest {
	@Test
	fun `returns singular subjects header when count is one`() {
		val provider = DefaultSummaryItemsTextProvider()

		assertEquals(
			expected = "1 materia inscrita",
			actual = provider.subjectsHeader(1)
		)
	}

	@Test
	fun `returns plural subjects header when count is greater than one`() {
		val provider = DefaultSummaryItemsTextProvider()

		assertEquals(
			expected = "2 materias inscritas",
			actual = provider.subjectsHeader(2)
		)
	}

	@Test
	fun `returns singular credits header when count is one`() {
		val provider = DefaultSummaryItemsTextProvider()

		assertEquals(
			expected = "1 crédito inscrito",
			actual = provider.creditsHeader(1)
		)
	}

	@Test
	fun `returns plural credits header when count is greater than one`() {
		val provider = DefaultSummaryItemsTextProvider()

		assertEquals(
			expected = "3 créditos inscritos",
			actual = provider.creditsHeader(3)
		)
	}
}
