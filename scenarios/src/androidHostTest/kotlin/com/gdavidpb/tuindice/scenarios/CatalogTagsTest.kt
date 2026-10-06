package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.codec.CatalogCodec
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertTrue

class CatalogTagsTest {
	private val constant = Regex("""const\s+val\s+\w+\s*=\s*"([^"]+)"""")
	private val builder = Regex("""fun\s+\w+\([^)]*\)\s*:\s*String\s*=\s*"((?:[^"\\]|\\.)*)"""")
	private val interpolation = Regex("""\$\{[^}]*}|\$\w+""")

	/** Tags no `*UiTags` declares, on purpose. */
	private val exceptions = setOf("poc_never_present")

	@Test
	fun everyTagIsAUiTagConstantOrFitsABuilderTemplate() {
		val sources = RepoFiles.uiTagFiles().map { it.readText() }
		val constants = sources.flatMap { text -> constant.findAll(text).map { it.groupValues[1] }.toList() }.toSet()
		val templates = sources.flatMap { text -> builder.findAll(text).map { it.groupValues[1] }.toList() }
			.filter { interpolation.containsMatchIn(it) }
			.map(::templateRegex)

		assertTrue(constants.isNotEmpty() && templates.isNotEmpty(), "no UiTags found")

		val unknown = resolvedTags().filter { tag ->
			tag !in exceptions && tag !in constants && templates.none { it.matches(tag) }
		}

		assertTrue(unknown.isEmpty(), "tags that no UiTags declares: ${unknown.sorted()}")
	}

	@Test
	fun theExceptionsAreStillUsed() {
		val stale = exceptions - resolvedTags()

		assertTrue(stale.isEmpty(), "exceptions no scenario uses: $stale")
	}

	private fun templateRegex(template: String): Regex {
		val literals = template.split(interpolation).map(Regex::escape)

		return Regex(literals.joinToString(".+"))
	}

	/** Every `Query.Tag` of the catalog as it is exported, wherever a step holds it. */
	private fun resolvedTags(): Set<String> {
		val tree = Json.parseToJsonElement(CatalogCodec.encode(E2eCatalog.catalog()))
		val tags = mutableSetOf<String>()

		collectTags(tree, tags)

		return tags
	}

	private fun collectTags(element: JsonElement, into: MutableSet<String>) {
		when (element) {
			is JsonObject -> {
				if ((element["type"] as? JsonPrimitive)?.content == "tag") {
					(element["value"] as? JsonPrimitive)?.content?.let(into::add)
				}

				element.values.forEach { collectTags(it, into) }
			}
			is JsonArray -> element.forEach { collectTags(it, into) }
			else -> Unit
		}
	}
}
