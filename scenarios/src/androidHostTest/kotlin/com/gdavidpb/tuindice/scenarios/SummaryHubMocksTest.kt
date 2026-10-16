package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.MockJson.string
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccount
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Which mapping answers each request of the two hub accounts: the same record and evaluations for both, the shared
 * pensum for one and a 404 for the other, and everyone else keeps the shared evaluations.
 */
class SummaryHubMocksTest {
	@Test
	fun theHubTokensGetTheHubEvaluationsAndRecord() {
		val replay = MockReplay.everything()

		listOf(E2eAccounts.SummaryHub, E2eAccounts.SummaryHubNoPensum).forEach { account ->
			val bearer = bearerOf(account)

			assertEquals(
				"get-evaluations-summary-hub.json",
				replay.send("GET", "/evaluations/v3", bearer).mapping,
				account.id
			)
			assertEquals("get-record-summary-hub.json", replay.send("GET", "/record/v5", bearer).mapping, account.id)
		}
		assertEquals(
			"get-evaluations-success.json",
			replay.send("GET", "/evaluations/v3", mapOf("Authorization" to "Bearer any.session.token")).mapping,
			"the other accounts keep the shared evaluations"
		)
	}

	@Test
	fun onlyTheNoPensumAccountGetsNoPensum() {
		val replay = MockReplay.everything()

		assertEquals(
			MockReplay.Reply(OK, "get-pensum-success.json"),
			replay.send("GET", "/pensums/v4", bearerOf(E2eAccounts.SummaryHub))
		)
		assertEquals(
			MockReplay.Reply(NOT_FOUND, "get-pensum-summary-hub-no-pensum.json"),
			replay.send("GET", "/pensums/v4", bearerOf(E2eAccounts.SummaryHubNoPensum))
		)
	}

	@Test
	fun bothAccountsSyncToTheSameBodyByPasswordAndBearer() {
		val bodies = listOf(E2eAccounts.SummaryHub, E2eAccounts.SummaryHubNoPensum).map { account ->
			val reply = MockReplay.everything().send(
				"POST",
				"/record/v5/sync",
				bearerOf(account),
				body = mapOf("password" to account.password)
			)
			val mapping = MockJson.obj(RepoFiles.file("mocks/mappings/sync/${reply.mapping}"))

			assertEquals(OK, reply.status, account.id)
			mapping.string("response", "bodyFileName")
		}

		assertEquals(listOf("sync/post-sync-summary-hub.json", "sync/post-sync-summary-hub.json"), bodies)
		assertNotNull(RepoFiles.file("mocks/__files/${bodies.first()}").takeIf { it.isFile })
	}

	private fun bearerOf(account: E2eAccount) =
		mapOf("Authorization" to "Bearer ${checkNotNull(account.session).accessToken}")

	private companion object {
		const val OK = 200
		const val NOT_FOUND = 404
	}
}
