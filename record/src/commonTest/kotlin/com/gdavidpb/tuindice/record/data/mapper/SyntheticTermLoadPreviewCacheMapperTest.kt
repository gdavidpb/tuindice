package com.gdavidpb.tuindice.record.data.mapper

import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadBand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SyntheticTermLoadPreviewCacheMapperTest {
	@Test
	fun syntheticTermLoadPreviewCacheKeyIsStableForSubjectOrderAndCase() {
		val first = syntheticTermLoadPreviewCacheKey(
			termKey = "2026-JUL_AUG",
			subjectCodes = listOf("ep5855", "EP1308")
		)
		val second = syntheticTermLoadPreviewCacheKey(
			termKey = "2026-JUL_AUG",
			subjectCodes = listOf("EP1308", "EP5855")
		)

		assertEquals(first, second)
	}

	@Test
	fun syntheticTermLoadPreviewCacheKeyChangesWithTerm() {
		val first = syntheticTermLoadPreviewCacheKey(
			termKey = "2026-JUL_AUG",
			subjectCodes = listOf("EP1308")
		)
		val second = syntheticTermLoadPreviewCacheKey(
			termKey = "2026-SEP_DEC",
			subjectCodes = listOf("EP1308")
		)

		assertNotEquals(first, second)
	}

	@Test
	fun syntheticTermLoadPreviewCacheEntityRoundTripsPreview() {
		val preview = SyntheticTermLoadPreview(
			available = true,
			band = SyntheticTermLoadBand.NORMAL,
			credits = 7,
			weightedDifficulty = 42.5,
			loadIndex = 9.975,
			baselineLoadIndex = 10.0,
			effectiveTerms = 4
		)

		val entity = preview.toSyntheticTermLoadPreviewCacheEntity(
			cacheKey = "cache-key",
			termKey = "2026-JUL_AUG",
			subjectCodes = listOf("EP5855", "EP1308"),
			updatedAt = 1_000L
		)

		assertEquals(preview, entity.toSyntheticTermLoadPreview())
	}
}
