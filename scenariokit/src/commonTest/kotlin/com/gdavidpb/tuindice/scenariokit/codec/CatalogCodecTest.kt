package com.gdavidpb.tuindice.scenariokit.codec

import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.ScenarioCatalog
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CatalogCodecTest {
	private fun scenarioJson(catalog: ScenarioCatalog): JsonObject =
		Json.parseToJsonElement(CatalogCodec.encode(catalog)).jsonObject.getValue("scenarios").jsonArray.first().jsonObject

	private fun assertSorted(element: JsonElement) {
		when (element) {
			is JsonObject -> {
				assertEquals(element.keys.sorted(), element.keys.toList())
				element.values.forEach(::assertSorted)
			}
			is JsonArray -> element.forEach(::assertSorted)
			else -> Unit
		}
	}

	@Test
	fun encode_isDeterministicSoTheSameCatalogIsTheSameBytes() {
		assertEquals(CatalogCodec.encode(sampleCatalog()), CatalogCodec.encode(sampleCatalog()))
	}

	@Test
	fun encode_doesNotDependOnMapInsertionOrder() {
		val first = sampleScenario().copy(start = LaunchSpec(linkedMapOf("A" to "1", "B" to "2")))
		val second = sampleScenario().copy(start = LaunchSpec(linkedMapOf("B" to "2", "A" to "1")))

		assertEquals(CatalogCodec.encode(sampleCatalog(listOf(first))), CatalogCodec.encode(sampleCatalog(listOf(second))))
	}

	@Test
	fun encode_sortsKeysAtEveryLevelAndEndsWithANewline() {
		val text = CatalogCodec.encode(sampleCatalog())

		assertSorted(Json.parseToJsonElement(text))
		assertTrue(text.endsWith("}\n"))
	}

	@Test
	fun encode_declaresTheSchema() {
		val root = Json.parseToJsonElement(CatalogCodec.encode(sampleCatalog())).jsonObject

		assertEquals("tuindice-e2e-catalog/1", root.getValue("schema").jsonPrimitive.content)
		assertEquals(listOf("accounts", "contractFixture", "scenarios", "schema"), root.keys.toList())
	}

	@Test
	fun decodeOfEncode_givesBackTheCatalog() {
		val catalog = sampleCatalog()

		assertEquals(catalog, CatalogCodec.decode(CatalogCodec.encode(catalog)))
	}

	@Test
	fun encodeOfDecodeOfEncode_isTheSameBytes() {
		val text = CatalogCodec.encode(sampleCatalog())

		assertEquals(text, CatalogCodec.encode(CatalogCodec.decode(text)))
	}

	@Test
	fun encode_addsTheDerivedFieldsPerScenario() {
		val scenario = scenarioJson(sampleCatalog())

		assertEquals(StepsHash.of(sampleScenario().steps), scenario.getValue("stepsHash").jsonPrimitive.content)
		assertEquals(
			"TuIndiceUITests/AuthScenarioTests/test_auth_login_success",
			scenario.getValue("ios").jsonObject.getValue("onlyTesting").jsonPrimitive.content
		)
		assertEquals(
			"auth-login-success",
			scenario.getValue("android").jsonObject.getValue("scenarioArg").jsonPrimitive.content
		)
	}

	@Test
	fun encode_givesEachPlatformDerivedEntryOnlyToTheScenariosThatRunThere() {
		val androidOnly = sampleScenario("conformance-back").copy(platforms = listOf(Platform.Android))
		val iosOnly = sampleScenario("ios-only").copy(platforms = listOf(Platform.Ios))
		val scenarios = Json.parseToJsonElement(CatalogCodec.encode(sampleCatalog(listOf(androidOnly, iosOnly))))
			.jsonObject.getValue("scenarios").jsonArray.map { it.jsonObject }

		assertTrue("ios" !in scenarios[0] && "android" in scenarios[0])
		assertTrue("android" !in scenarios[1] && "ios" in scenarios[1])
	}

	@Test
	fun encode_writesPlatformsInLowerCaseAndTheScenarioMetadata() {
		val scenario = scenarioJson(sampleCatalog())

		assertEquals(listOf("android", "ios"), scenario.getValue("platforms").jsonArray.map { it.jsonPrimitive.content })
		assertEquals("canonical", scenario.getValue("account").jsonPrimitive.content)
		assertEquals("true", scenario.getValue("signsIn").jsonPrimitive.content)
		assertEquals("120", scenario.getValue("timeoutSeconds").jsonPrimitive.content)
		assertEquals("2026-12-31", scenario.getValue("quarantine").jsonObject.getValue("until").jsonPrimitive.content)
	}

	@Test
	fun encode_carriesTheBackendIdentifierOfEachAccountAndNullWhenThereIsNone() {
		val accounts = Json.parseToJsonElement(CatalogCodec.encode(sampleCatalog())).jsonObject.getValue("accounts").jsonArray
		val identifiers = accounts.associate {
			it.jsonObject.getValue("id").jsonPrimitive.content to it.jsonObject["backendIdentifier"]
		}

		assertEquals("11-11111", identifiers.getValue("canonical")?.jsonPrimitive?.content)
		assertEquals(JsonNull, identifiers.getValue("invalid"))
	}

	@Test
	fun decode_rejectsAnotherSchema() {
		val text = CatalogCodec.encode(sampleCatalog()).replace("tuindice-e2e-catalog/1", "tuindice-e2e-catalog/2")

		assertFailsWith<IllegalArgumentException> { CatalogCodec.decode(text) }
	}

	@Test
	fun scenario_lookupFailsForAnUnknownId() {
		assertFailsWith<NoSuchElementException> { sampleCatalog().scenario("nope") }
	}
}
