package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.MockJson.array
import com.gdavidpb.tuindice.scenarios.MockJson.string
import com.gdavidpb.tuindice.scenarios.fixture.Copy
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import kotlinx.serialization.json.JsonObject
import java.time.Instant
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The texts and keys the scenarios derive from a date (`Copy.TermSepDec2026`, `E2eFixtures.NextTermKey`) are
 * the ones the app builds from the instant the scenarios freeze it at (`E2eFixtures.Now`): the first term it
 * offers for planning on top of the canonical record. The app reads the term in the university's time zone.
 */
class E2eClockFixtureTest {
	private val universityZone = ZoneId.of("America/Caracas")
	private val now = Instant.parse(E2eFixtures.Now).atZone(universityZone)
	private val recordTerms = (MockJson.obj(RepoFiles.file("mocks/__files/sync/post-sync-success.json"))["record"] as JsonObject)
		.let { revision -> (revision["record"] as JsonObject).array("terms") }
		.map { it as JsonObject }

	@Test
	fun theFrozenInstantFallsInTheSeptemberDecemberTermOf2026() {
		assertEquals(2026, now.year)
		assertTrue(now.monthValue in 9..12, "'${E2eFixtures.Now}' is month ${now.monthValue} in ${universityZone.id}")
	}

	@Test
	fun theDerivedKeyAndTextNameTheTermTheFrozenInstantFallsIn() {
		assertEquals("${now.year}-SEP_DEC", E2eFixtures.NextTermKey.value)
		assertEquals("Sep - Dic ${now.year}", Copy.TermSepDec2026)
	}

	@Test
	fun thatTermIsFreeInTheCanonicalRecordSoTheAppOffersIt() {
		val taken = recordTerms.map { term -> term.string("period_year") + "-" + term.string("period_code") }

		assertTrue(taken.isNotEmpty())
		assertFalse(E2eFixtures.NextTermKey.value in taken, "the record already has ${E2eFixtures.NextTermKey.value}")
	}

	@Test
	fun noTermOfTheCanonicalRecordIsLaterThanTheFrozenTerm() {
		val years = recordTerms.map { term -> term.string("period_year")!!.toInt() }

		assertTrue(years.max() <= now.year, "a term of the record is after ${now.year}")
	}
}
