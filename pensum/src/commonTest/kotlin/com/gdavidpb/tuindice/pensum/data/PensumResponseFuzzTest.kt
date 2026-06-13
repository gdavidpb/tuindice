package com.gdavidpb.tuindice.pensum.data

import com.gdavidpb.tuindice.pensum.data.mapper.cacheKey
import com.gdavidpb.tuindice.pensum.data.mapper.toAvailableModalities
import com.gdavidpb.tuindice.pensum.data.mapper.toAvailablePensums
import com.gdavidpb.tuindice.pensum.data.mapper.toGraph
import com.gdavidpb.tuindice.pensum.data.mapper.toGraphs
import com.gdavidpb.tuindice.pensum.data.mapper.toSelection
import com.gdavidpb.tuindice.pensum.data.model.GetPensumResponse
import com.gdavidpb.tuindice.pensum.domain.model.PensumGraph
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeType
import com.gdavidpb.tuindice.pensum.domain.model.PensumRelationshipType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonUnquotedLiteral
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Fuzz / malformed-input contract tests for the pensum graph payload
 * ([GetPensumResponse] decoded by Ktor's ContentNegotiation and mapped by
 * PensumResponseMapper).
 *
 * The real decoding contract:
 * - The shared HttpClient decodes with the Json built by createSharedJson()
 *   (maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/di/KtorClient.kt):
 *   explicitNulls = true, ignoreUnknownKeys = true, prettyPrint = false.
 *   That function lives in maincore (not a pensum dependency), so [pensumWireJson]
 *   replicates the exact same configuration here.
 * - Malformed payloads must fail with SerializationException (controlled and
 *   translated upstream); the data->domain mapper performs no validation of its
 *   own and must never throw for structurally valid payloads.
 */
class PensumResponseFuzzTest {
	// Mirror of createSharedJson() in maincore/di/KtorClient.kt.
	private val pensumWireJson = Json {
		explicitNulls = true
		ignoreUnknownKeys = true
		prettyPrint = false
	}

	private fun decode(payload: String): GetPensumResponse =
		pensumWireJson.decodeFromString(GetPensumResponse.serializer(), payload)

	/** Exercises the full data->domain mapping surface used by the repository. */
	private fun GetPensumResponse.mapToDomain(): PensumGraph {
		cacheKey()
		toSelection()
		toAvailablePensums()
		toAvailableModalities()
		toGraphs()
		return toGraph()
	}

	@Test
	fun validPayload_decodesAndMapsWithoutErrors() {
		val response = decode(VALID_PENSUM_JSON)
		val graph = response.mapToDomain()

		assertEquals(3, graph.nodes.size, "baseline payload must map every node")
		assertEquals(1, graph.edges.size, "baseline payload must map every edge")
		assertEquals(2, graph.terms.size, "baseline payload must map every term")
		assertEquals("2019-degree_project", response.cacheKey(), "cache key combines year and modality")
	}

	@Test
	fun truncatedPayloads_failWithControlledSerializationException() {
		val truncations = listOf(
			VALID_PENSUM_JSON.dropLast(1),
			VALID_PENSUM_JSON.dropLast(40),
			VALID_PENSUM_JSON.take(VALID_PENSUM_JSON.length / 2)
		)

		truncations.forEachIndexed { index, payload ->
			// Contract: a cut-off body is a decode failure, never a different crash.
			assertFailsWith<SerializationException>(
				"truncation #$index must surface as SerializationException"
			) {
				decode(payload)
			}
		}
	}

	@Test
	fun missingRequiredFields_failWithSerializationException_butDefaultedFieldsDecode() {
		val root = pensumWireJson.parseToJsonElement(VALID_PENSUM_JSON).jsonObject

		// "career_name" has no default value in the DTO -> MissingFieldException (a SerializationException).
		val withoutCareerName = root.transformAt(emptyList()) { jsonObject ->
			JsonObject(jsonObject.filterKeys { key -> key != "career_name" })
		}
		assertFailsWith<SerializationException>(
			"missing top-level required field must fail decoding"
		) {
			decode(withoutCareerName.toString())
		}

		// "nodes" inside "pensum" has no default either.
		val withoutNodes = root.transformAt(listOf("pensum")) { jsonObject ->
			JsonObject(jsonObject.filterKeys { key -> key != "nodes" })
		}
		assertFailsWith<SerializationException>(
			"missing nested required field must fail decoding"
		) {
			decode(withoutNodes.toString())
		}

		// "fulfillment_rules" defaults to emptyList() in the DTO -> removal is tolerated by contract.
		val withoutRules = root.transformAt(listOf("pensum", "nodes", 2)) { jsonObject ->
			JsonObject(jsonObject.filterKeys { key -> key != "fulfillment_rules" })
		}
		val decoded = decode(withoutRules.toString())
		assertTrue(
			decoded.pensum.nodes[2].fulfillmentRules.isEmpty(),
			"defaulted field must fall back to emptyList() instead of failing"
		)
	}

	@Test
	fun typeMismatches_failWithSerializationException() {
		val root = pensumWireJson.parseToJsonElement(VALID_PENSUM_JSON).jsonObject

		// Non-numeric string where Int is expected.
		val yearAsText = root.transformAt(listOf("pensum")) { jsonObject ->
			JsonObject(jsonObject.toMutableMap().also { map -> map["year"] = JsonPrimitive("two-thousand-nineteen") })
		}
		assertFailsWith<SerializationException>(
			"non-numeric string where Int goes must fail decoding"
		) {
			decode(yearAsText.toString())
		}

		// Number token where String is expected (strict mode requires quoted strings).
		val careerAsNumber = root.transformAt(emptyList()) { jsonObject ->
			JsonObject(jsonObject.toMutableMap().also { map -> map["career_name"] = JsonPrimitive(123456) })
		}
		assertFailsWith<SerializationException>(
			"number token where String goes must fail decoding"
		) {
			decode(careerAsNumber.toString())
		}

		// Arbitrary string where Boolean is expected.
		val inferredAsText = root.transformAt(emptyList()) { jsonObject ->
			JsonObject(jsonObject.toMutableMap().also { map -> map["inferred"] = JsonPrimitive("yes") })
		}
		assertFailsWith<SerializationException>(
			"string where Boolean goes must fail decoding"
		) {
			decode(inferredAsText.toString())
		}

		// Documented kotlinx-json leniency: QUOTED NUMERIC strings are accepted for
		// number primitives even in strict mode, so "2019" silently decodes as 2019.
		val yearAsQuotedNumber = root.transformAt(listOf("pensum")) { jsonObject ->
			JsonObject(jsonObject.toMutableMap().also { map -> map["year"] = JsonPrimitive("2019") })
		}
		assertEquals(
			2019,
			decode(yearAsQuotedNumber.toString()).pensum.year,
			"quoted numeric strings are accepted by kotlinx-json (documented leniency, not a failure)"
		)
	}

	@Test
	fun listsWithNullEntries_failWithSerializationException() {
		val root = pensumWireJson.parseToJsonElement(VALID_PENSUM_JSON).jsonObject

		val nullInjections = listOf<Pair<String, JsonObject>>(
			"available_pensums" to root.transformAt(emptyList()) { jsonObject ->
				JsonObject(jsonObject.toMutableMap().also { map -> map["available_pensums"] = JsonArray(listOf(JsonNull)) })
			},
			"nodes" to root.transformAt(listOf("pensum")) { jsonObject ->
				JsonObject(jsonObject.toMutableMap().also { map -> map["nodes"] = JsonArray(listOf(JsonNull)) })
			},
			"edges" to root.transformAt(listOf("pensum")) { jsonObject ->
				JsonObject(jsonObject.toMutableMap().also { map -> map["edges"] = JsonArray(listOf(JsonNull)) })
			},
			"points" to root.transformAt(listOf("pensum", "edges", 0)) { jsonObject ->
				JsonObject(jsonObject.toMutableMap().also { map -> map["points"] = JsonArray(listOf(JsonNull)) })
			}
		)

		nullInjections.forEach { (field, mutated) ->
			// Contract: list elements are non-nullable in the DTO, so null entries
			// must fail decoding instead of producing half-initialized models.
			assertFailsWith<SerializationException>(
				"null entry inside '$field' must fail decoding"
			) {
				decode(mutated.toString())
			}
		}
	}

	@Test
	fun danglingEdgeReferences_areAcceptedWithoutValidationOrNormalization() {
		val root = pensumWireJson.parseToJsonElement(VALID_PENSUM_JSON).jsonObject

		val withGhostEdge = root.transformAt(listOf("pensum", "edges", 0)) { jsonObject ->
			JsonObject(
				jsonObject.toMutableMap().also { map ->
					map["from_node_id"] = JsonPrimitive("ghost-from")
					map["to_node_id"] = JsonPrimitive("ghost-to")
				}
			)
		}

		// Real contract: neither the DTO nor PensumResponseMapper validates edge
		// endpoints against the node list — the dangling edge flows verbatim into
		// the domain graph. Downstream, PensumScreenModelMapper degrades gracefully
		// (mapNotNull on nodesById lookups, missing status falls back to BLOCKED),
		// so the data layer's contract is "accept without throwing".
		val graph = decode(withGhostEdge.toString()).mapToDomain()
		val edge = graph.edges.single()

		assertEquals("ghost-from", edge.fromNodeId, "dangling source id is preserved verbatim")
		assertEquals("ghost-to", edge.toNodeId, "dangling target id is preserved verbatim")
		assertTrue(
			graph.nodes.none { node -> node.id == "ghost-from" || node.id == "ghost-to" },
			"ids are truly dangling: no node in the graph matches them"
		)
	}

	@OptIn(ExperimentalSerializationApi::class)
	@Test
	fun nanAndInfinityPositions_failWithSerializationException() {
		val root = pensumWireJson.parseToJsonElement(VALID_PENSUM_JSON).jsonObject

		val specialValues = listOf<Pair<String, JsonElement>>(
			// Bare NaN token: parsed leniently into "NaN".toDouble(), then rejected
			// by decodeDouble's isFinite gate because allowSpecialFloatingPointValues=false.
			"bare NaN" to JsonPrimitive(Double.NaN),
			// Quoted "NaN": same toDouble() path, same isFinite rejection.
			"quoted NaN" to JsonPrimitive("NaN"),
			// Bare Infinity token.
			"bare Infinity" to JsonUnquotedLiteral("Infinity"),
			// Valid JSON number that overflows Double into Infinity: also rejected
			// by the isFinite gate, so overflow cannot smuggle Infinity into layout math.
			"overflowing 1e999" to JsonUnquotedLiteral("1e999")
		)

		specialValues.forEach { (description, value) ->
			val mutated = root.transformAt(listOf("pensum", "nodes", 0)) { jsonObject ->
				JsonObject(jsonObject.toMutableMap().also { map -> map["x"] = value })
			}

			assertFailsWith<SerializationException>(
				"$description in a node position must fail decoding (allowSpecialFloatingPointValues=false)"
			) {
				decode(mutated.toString())
			}
		}

		// Documented kotlinx-json leniency: quoted FINITE numbers decode for Double fields.
		val quotedFinite = root.transformAt(listOf("pensum", "nodes", 0)) { jsonObject ->
			JsonObject(jsonObject.toMutableMap().also { map -> map["x"] = JsonPrimitive("12.5") })
		}
		assertEquals(
			12.5,
			decode(quotedFinite.toString()).pensum.nodes[0].x,
			"quoted finite numbers are accepted by kotlinx-json (documented leniency, not a failure)"
		)
	}

	@Test
	fun emptyPayloads_failWithSerializationException() {
		val emptyPayloads = listOf("", " ", "{}", "[]", "null")

		emptyPayloads.forEachIndexed { index, payload ->
			// Contract: an empty or non-object body is always a controlled decode failure
			// ("{}" fails on missing required fields, the rest fail on the root token).
			assertFailsWith<SerializationException>(
				"empty payload #$index ('$payload') must fail with SerializationException"
			) {
				decode(payload)
			}
		}
	}

	@Test
	fun unknownNodeAndRelationshipTypes_areNormalizedToFallbacks() {
		val root = pensumWireJson.parseToJsonElement(VALID_PENSUM_JSON).jsonObject

		val withUnknownTypes = root
			.transformAt(listOf("pensum", "nodes", 0)) { jsonObject ->
				JsonObject(jsonObject.toMutableMap().also { map -> map["node_type"] = JsonPrimitive("HOLOGRAM") })
			}
			.jsonObject
			.transformAt(listOf("pensum", "edges", 0)) { jsonObject ->
				JsonObject(jsonObject.toMutableMap().also { map -> map["relationship_type"] = JsonPrimitive("FRIENDSHIP") })
			}

		// Real contract: types travel as plain strings in the DTO and the mapper
		// normalizes unknown values instead of rejecting them (toNodeType/toRelationshipType
		// in PensumResponseMapper fall back to SLOT and REQUIREMENT).
		val graph = decode(withUnknownTypes.toString()).mapToDomain()

		assertEquals(
			PensumNodeType.SLOT,
			graph.nodes[0].nodeType,
			"unknown node_type must normalize to SLOT instead of throwing"
		)
		assertEquals(
			PensumRelationshipType.REQUIREMENT,
			graph.edges.single().relationshipType,
			"unknown relationship_type must normalize to REQUIREMENT instead of throwing"
		)
	}

	@Test
	fun fiftySeededMutations_neverEscapeControlledExceptions() {
		val random = Random(seed = 20260612)
		val root = pensumWireJson.parseToJsonElement(VALID_PENSUM_JSON).jsonObject
		val targets = root.collectKeyPaths(prefix = emptyList())
		var failures = 0
		var successes = 0

		assertTrue(targets.isNotEmpty(), "fuzz target collection must find object keys")

		repeat(50) { index ->
			val (parentPath, key) = targets[random.nextInt(targets.size)]
			val removeKey = random.nextBoolean()
			val description: String
			val mutated: JsonElement = if (removeKey) {
				description = "remove '$key' at $parentPath"
				root.transformAt(parentPath) { jsonObject ->
					JsonObject(jsonObject.filterKeys { candidate -> candidate != key })
				}
			} else {
				description = "type-corrupt '$key' at $parentPath"
				root.transformAt(parentPath) { jsonObject ->
					JsonObject(
						jsonObject.toMutableMap().also { map ->
							map[key] = incompatibleReplacement(jsonObject.getValue(key))
						}
					)
				}
			}

			// Contract under fuzzing: every mutation either decodes+maps successfully
			// (keys with defaults) or fails with SerializationException /
			// IllegalArgumentException. Any other throwable is an uncontrolled crash.
			val outcome = runCatching { decode(mutated.toString()).mapToDomain() }
			val throwable = outcome.exceptionOrNull()

			if (throwable == null) {
				successes++
			} else {
				failures++
				assertTrue(
					throwable is SerializationException || throwable is IllegalArgumentException,
					"mutation #$index ($description) escaped the controlled contract with " +
						"${throwable::class.simpleName}: ${throwable.message}"
				)
			}
		}

		assertEquals(50, failures + successes, "every seeded mutation must be evaluated")
		assertTrue(failures > 0, "seeded fuzzing must exercise the failure path at least once")
	}
}

/** Replaces a value with one of an incompatible JSON type (deterministic per original type). */
private fun incompatibleReplacement(original: JsonElement): JsonElement {
	return when {
		original is JsonObject || original is JsonArray -> JsonPrimitive("fuzz#collapsed")
		original is JsonNull -> JsonPrimitive(987654)
		original is JsonPrimitive && original.isString -> JsonPrimitive(424242)
		original is JsonPrimitive && original.booleanOrNull != null -> JsonPrimitive("fuzz#not-a-boolean")
		else -> JsonPrimitive("fuzz#not-a-number")
	}
}

/**
 * Collects every (path-to-containing-object, key) pair in the tree.
 * Paths are sequences of String (object key) and Int (array index) segments.
 */
private fun JsonElement.collectKeyPaths(prefix: List<Any>): List<Pair<List<Any>, String>> {
	return when (this) {
		is JsonObject -> entries.flatMap { (key, child) ->
			listOf(prefix to key) + child.collectKeyPaths(prefix + key)
		}

		is JsonArray -> flatMapIndexed { index, child ->
			child.collectKeyPaths(prefix + index)
		}

		else -> emptyList()
	}
}

/** Rebuilds the tree applying [transform] to the JsonObject located at [path]. */
private fun JsonElement.transformAt(
	path: List<Any>,
	transform: (JsonObject) -> JsonObject
): JsonObject {
	fun JsonElement.rebuild(remaining: List<Any>): JsonElement {
		if (remaining.isEmpty()) return transform(jsonObject)

		return when (val segment = remaining.first()) {
			is String -> JsonObject(
				jsonObject.toMutableMap().also { map ->
					map[segment] = map.getValue(segment).rebuild(remaining.drop(1))
				}
			)

			is Int -> JsonArray(
				jsonArray.mapIndexed { index, child ->
					if (index == segment) child.rebuild(remaining.drop(1)) else child
				}
			)

			else -> error("Unsupported path segment: $segment")
		}
	}

	return rebuild(path).jsonObject
}

/**
 * Structurally valid pensum payload covering every DTO branch: multiple terms,
 * COURSE and SLOT nodes, fulfillment rules, an explicit null subject_code and
 * an edge with intermediate points.
 */
private val VALID_PENSUM_JSON = """
{
  "career_name": "Ingenieria de Computacion",
  "selected_pensum_id": "0800-2019-degree_project",
  "inferred": false,
  "available_pensums": [{"year": 2016}, {"year": 2019}],
  "available_modalities": [
    {"id": "degree_project", "name": "Proyecto de Grado", "is_default": true},
    {"id": "long_internship", "name": "Pasantia Larga", "is_default": false}
  ],
  "pensum": {
    "id": "0800-2019-degree_project",
    "year": 2019,
    "modality_id": "degree_project",
    "modality_name": "Proyecto de Grado",
    "total_credits": 170,
    "canvas": {"width": 1200.0, "height": 900.0},
    "terms": [
      {"id": "T1", "label": "Trimestre 1", "x": 0.0, "width": 200.0},
      {"id": "T2", "label": "Trimestre 2", "x": 200.0, "width": 200.0}
    ],
    "nodes": [
      {"id": "ma1111", "node_type": "COURSE", "display_code": "MA1111", "subject_code": "MA1111", "name": "Matematicas I", "credits": 4, "category": "GENERAL", "term_id": "T1", "x": 40.0, "y": 80.0, "width": 160.0, "height": 120.0, "fulfillment_rules": []},
      {"id": "ma2112", "node_type": "COURSE", "display_code": "MA2112", "subject_code": "MA2112", "name": "Matematicas II", "credits": 4, "category": "GENERAL", "term_id": "T2", "x": 240.0, "y": 80.0, "width": 160.0, "height": 120.0, "fulfillment_rules": []},
      {"id": "slot_general", "node_type": "SLOT", "display_code": "EG-1", "subject_code": null, "name": "Estudio General", "credits": 3, "category": "GENERAL_STUDIES", "term_id": "T2", "x": 240.0, "y": 240.0, "width": 160.0, "height": 120.0, "fulfillment_rules": [
        {"id": "rule-1", "rule_type": "MIN_CREDITS", "subject_codes": ["EG1", "EG2"], "subject_code_prefixes": ["CS"], "slot_eligibility_kind": "GENERAL", "min_credits": 3, "min_subjects": 1}
      ]}
    ],
    "edges": [
      {"id": "edge-1", "from_node_id": "ma1111", "to_node_id": "ma2112", "relationship_type": "REQUIREMENT", "points": [{"x": 200.0, "y": 140.0}, {"x": 240.0, "y": 140.0}]}
    ]
  }
}
""".trimIndent()
