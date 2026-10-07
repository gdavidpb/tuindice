package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.fixture.E2eAccount
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import java.io.File
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/** The passwords the update-password scenarios type must be the ones the reissue mappings decide on. */
class ReissuePasswordFixturesTest {
	@Test
	fun theUpdatedPasswordIsTheOneTheReissueMappingAcceptsAndTheSessionPasswordIsNot() {
		val mapping = mappingText("auth-update-password-reissue-success.json")
		val typed = E2eAccounts.UpdatePassword.copy(password = E2eFixtures.UpdatedPassword)

		assertTrue(matches(mapping, typed), "the reissue mapping does not accept the updated password")
		assertTrue(!matches(mapping, E2eAccounts.UpdatePassword), "the reissue mapping accepts the old password")
		assertTrue(answersWith(mapping, OK), "the reissue mapping no longer answers 200")
	}

	@Test
	fun theRejectedPasswordIsTheOneTheFailureMappingAnswersWithUnauthorized() {
		val mapping = mappingText("auth-update-password-failure-reissue-unauthorized.json")
		val typed = E2eAccounts.UpdatePasswordFailure.copy(password = E2eFixtures.RejectedPassword)

		assertTrue(matches(mapping, typed), "the failure mapping does not match the rejected password")
		assertTrue(!matches(mapping, E2eAccounts.UpdatePasswordFailure), "the failure mapping matches the old password")
		assertTrue(answersWith(mapping, UNAUTHORIZED), "the failure mapping no longer answers 401")
	}

	@Test
	fun theTwoPasswordsDiffer() {
		assertNotEquals(E2eFixtures.UpdatedPassword, E2eFixtures.RejectedPassword)
	}

	private fun mappingText(name: String): String = File(RepoFiles.loginMappings, name).readText()

	private fun matches(mapping: String, account: E2eAccount): Boolean =
		Regex(""""equalTo"\s*:\s*"Basic ${Regex.escape(AccountCredentials.basic(account))}"""").containsMatchIn(mapping)

	private fun answersWith(mapping: String, status: Int): Boolean =
		Regex(""""status"\s*:\s*$status""").containsMatchIn(mapping)

	private companion object {
		const val OK = 200
		const val UNAUTHORIZED = 401
	}
}
