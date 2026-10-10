package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.MockJson.string
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccount
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The accounts the scenarios sign in with must be the ones the WireMock mappings accept. */
class AccountFixturesTest {
	private val accounts = E2eAccounts.all
	private val canonicalUsbId = Regex("""\d{2}-\d{5}""")

	/** The one credential no mapping accepts: the 401 fallback answers it. */
	private val rejectedAccount = E2eAccounts.Invalid

	@Test
	fun thereAreThirtyAccountsWithUniqueKebabCaseIds() {
		assertEquals(ACCOUNT_COUNT, accounts.size)
		assertEquals(accounts.size, accounts.map { it.id }.toSet().size)
		accounts.forEach { assertTrue(Regex("[a-z0-9-]+").matches(it.id), "'${it.id}' is not kebab-case") }
	}

	@Test
	fun theCatalogCarriesEveryAccountInOrder() {
		assertEquals(accounts.map { it.id }, E2eCatalog.catalog().accounts.map { it.id })
	}

	@Test
	fun usbIdFieldsAgreeWithEachOther() {
		accounts.forEach { account ->
			if (account.usbIdFormatted.endsWith("@usb.ve")) {
				assertEquals(account.usbIdFormatted, account.usbIdDigits, account.id)
			} else {
				assertTrue(canonicalUsbId.matches(account.usbIdFormatted), account.id)
				assertEquals(account.usbIdFormatted.replace("-", ""), account.usbIdDigits, account.id)
			}
		}
	}

	@Test
	fun theBackendReceivesTheIdentifierWithoutTheEmailSuffix() {
		assertEquals("mail", E2eAccounts.CanonicalEmail.backendIdentifier)
		assertEquals("11-11111", E2eAccounts.Canonical.backendIdentifier)
	}

	@Test
	fun everyCredentialButTheRejectedOneIsAcceptedByALoginMapping() {
		val loginMappings = loginMappingTexts()

		accounts.filter { it != rejectedAccount }.forEach { account ->
			assertTrue(
				loginMappings.any { acceptsCredential(it, account) },
				"no mapping under mocks/mappings/login accepts ${account.backendIdentifier}:${account.password} (${account.id})"
			)
		}
	}

	@Test
	fun theRejectedCredentialIsAcceptedByNoMappingAndFallsToTheUnauthorizedOne() {
		assertTrue(loginMappingTexts().none { acceptsCredential(it, rejectedAccount) })

		val fallback = MockJson.objects(RepoFiles.loginMappings)
			.single { it.string("request", "urlPath") == "/auth/v2/bootstrap" && it.string("response", "status") == "401" }

		assertEquals(
			"Basic ${basicCredential(E2eAccounts.Canonical)}",
			fallback.string("request", "headers", "Authorization", "doesNotMatch"),
			"the 401 fallback must stay 'anything but the canonical credential'"
		)
		assertTrue(basicCredential(rejectedAccount) != basicCredential(E2eAccounts.Canonical))
	}

	private fun loginMappingTexts(): List<String> =
		RepoFiles.loginMappings.listFiles { file -> file.extension == "json" }.orEmpty().map { it.readText() }

	private fun acceptsCredential(mapping: String, account: E2eAccount): Boolean =
		Regex(""""equalTo"\s*:\s*"Basic ${Regex.escape(basicCredential(account))}"""").containsMatchIn(mapping)

	private fun basicCredential(account: E2eAccount): String = AccountCredentials.basic(account)

	private companion object {
		const val ACCOUNT_COUNT = 30
	}
}
