package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.codec.CatalogCodec
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The catalog JSON tells a reader which identifier the backend receives, so no reader reimplements the rule. */
class CatalogAccountExportTest {
	private val exported = Json.parseToJsonElement(CatalogCodec.encode(E2eCatalog.catalog()))
		.jsonObject.getValue("accounts").jsonArray.map { it.jsonObject }

	@Test
	fun everyAccountExportsTheIdentifierTheBackendReceives() {
		assertEquals(E2eAccounts.all.size, exported.size)

		E2eAccounts.all.forEach { account ->
			val entry = exported.single { it.getValue("id").jsonPrimitive.content == account.id }

			assertEquals(account.backendIdentifier, entry.getValue("backendIdentifier").jsonPrimitive.content, account.id)
		}
	}

	@Test
	fun anEmailAccountExportsItWithoutTheDomainTheAppDrops() {
		val email = E2eAccounts.all.filter { "@" in it.usbIdFormatted }

		assertTrue(email.isNotEmpty(), "no account signs in with an email, so the case this export exists for is untested")
		email.forEach { account ->
			val entry = exported.single { it.getValue("id").jsonPrimitive.content == account.id }

			assertTrue("@" !in entry.getValue("backendIdentifier").jsonPrimitive.content, account.id)
			assertTrue("@" in entry.getValue("usbId").jsonPrimitive.content, account.id)
		}
	}
}
