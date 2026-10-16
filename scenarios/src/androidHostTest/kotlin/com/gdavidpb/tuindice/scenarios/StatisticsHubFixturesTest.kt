package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.MockJson.string
import com.gdavidpb.tuindice.scenarios.SummaryHubData.long
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.E2eInputs
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The Statistics and share scenarios name the closed terms of the hub record by position: the newest is the last point
 * of the index chart and the term the finished card of the hub shares, and the one before it is the next chip of the
 * sheet. These tests check that the record holds them that way.
 */
class StatisticsHubFixturesTest {
	private val closedNewestFirst = SummaryHubData.termsOf(SummaryHubData.readRecord)
		.filter { term -> term.string("term_kind") == "historical" }
		.sortedByDescending { term -> checkNotNull(term.long("term_order")) }

	@Test
	fun theHubRecordHoldsTheClosedTermsTheIndexChartPlaces() {
		assertEquals(E2eInputs.StatisticsNewestIndexPoint + 1, closedNewestFirst.size)
	}

	@Test
	fun theNewestTwoClosedTermsAreTheOnesTheScenariosName() {
		assertEquals(E2eFixtures.HubNewestClosedTermKey.value, closedNewestFirst[0].string("term_key"))
		assertEquals(E2eFixtures.HubPreviousClosedTermKey.value, closedNewestFirst[1].string("term_key"))
	}
}
