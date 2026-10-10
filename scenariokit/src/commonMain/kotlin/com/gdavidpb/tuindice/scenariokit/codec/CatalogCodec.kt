package com.gdavidpb.tuindice.scenariokit.codec

import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.ScenarioCatalog
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * JSON form of the catalog, `e2e/catalog/scenarios.json`. Output is deterministic: keys are
 * sorted at every level and the format is fixed, so the same catalog is always the same
 * bytes. Each scenario also carries derived fields (`stepsHash`, `ios.onlyTesting`,
 * `android.scenarioArg`) that [decode] ignores, so `decode(encode(x)) == x`.
 */
object CatalogCodec {
	const val SCHEMA = "tuindice-e2e-catalog/1"

	private val json = Json {
		encodeDefaults = true
		explicitNulls = true
		ignoreUnknownKeys = true
		prettyPrint = true
	}

	fun encode(catalog: ScenarioCatalog): String {
		val tree = json.encodeToJsonElement(ScenarioCatalog.serializer(), catalog).jsonObject
		val scenarios = tree.getValue("scenarios").jsonArray.zip(catalog.scenarios) { element, scenario ->
			withDerivedFields(element.jsonObject, scenario)
		}
		val full = JsonObject(tree + ("scenarios" to JsonArray(scenarios)))
		return json.encodeToString(JsonElement.serializer(), CanonicalJson.sorted(full)) + "\n"
	}

	fun decode(text: String): ScenarioCatalog {
		val catalog = json.decodeFromString(ScenarioCatalog.serializer(), text)
		require(catalog.schema == SCHEMA) { "Unsupported catalog schema '${catalog.schema}', expected '$SCHEMA'" }
		return catalog
	}

	/** Each platform's entry exists only for a scenario that runs on that platform. */
	private fun withDerivedFields(element: JsonObject, scenario: Scenario): JsonObject {
		val derived = buildMap<String, JsonElement> {
			put("stepsHash", JsonPrimitive(StepsHash.of(scenario.steps)))
			if (Platform.Ios in scenario.platforms) {
				put("ios", JsonObject(mapOf("onlyTesting" to JsonPrimitive(ScenarioNaming.iosOnlyTesting(scenario)))))
			}
			if (Platform.Android in scenario.platforms) {
				put("android", JsonObject(mapOf("scenarioArg" to JsonPrimitive(ScenarioNaming.androidScenarioArg(scenario)))))
			}
		}

		return JsonObject(element + derived)
	}
}
