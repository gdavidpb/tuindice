package com.gdavidpb.tuindice.scenariokit.codec

import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Site
import com.gdavidpb.tuindice.scenariokit.model.Step
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class StepsHashTest {
	@Test
	fun fnv1a_matchesTheReferenceVectors() {
		assertEquals("cbf29ce484222325", Fnv1a.hex(""))
		assertEquals("af63dc4c8601ec8c", Fnv1a.hex("a"))
		assertEquals("85944171f73967e8", Fnv1a.hex("foobar"))
	}

	@Test
	fun hash_isSixteenHexDigits() {
		assertTrue16(StepsHash.of(allStepKinds()))
	}

	private fun assertTrue16(hash: String) {
		assertEquals(16, hash.length)
		assertEquals(hash.lowercase(), hash)
	}

	@Test
	fun hash_doesNotChangeWhenOnlySitesMove() {
		val before = StepsHash.of(allStepKinds(Site("a/Auth.kt", 10)))
		val after = StepsHash.of(allStepKinds(Site("a/Auth.kt", 99)))
		val noSites = StepsHash.of(allStepKinds(site = null))

		assertEquals(before, after)
		assertEquals(before, noSites)
	}

	@Test
	fun hash_changesWhenAStepChanges() {
		val base = allStepKinds()
		val changed = base.toMutableList().apply { this[2] = Step.Tap(Query.Tag("other"), true, SAMPLE_SITE) }
		val reordered = base.toMutableList().apply { add(0, removeAt(1)) }
		val differentTap = Step.Tap(Query.Tag("different"), true, SAMPLE_SITE)
		val nested = base.dropLast(1) + Step.Group("sign in", listOf(differentTap), SAMPLE_SITE)

		assertNotEquals(StepsHash.of(base), StepsHash.of(changed))
		assertNotEquals(StepsHash.of(base), StepsHash.of(reordered))
		assertNotEquals(StepsHash.of(base), StepsHash.of(nested))
	}

	@Test
	fun hash_changesWithAParameterOfAStep() {
		val enabled = listOf<Step>(Step.Tap(Query.Tag("a"), true))
		val notRequired = listOf<Step>(Step.Tap(Query.Tag("a"), false))

		assertNotEquals(StepsHash.of(enabled), StepsHash.of(notRequired))
	}
}
