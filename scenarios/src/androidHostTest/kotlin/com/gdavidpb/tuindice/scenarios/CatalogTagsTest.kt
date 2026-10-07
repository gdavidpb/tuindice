package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.scenariokit.codec.CatalogCodec
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CatalogTagsTest {
	/** Tags no `*UiTags` declares, on purpose. */
	private val exceptions = setOf("poc_never_present")

	private val real = UiTagSources(RepoFiles.uiTagFiles().map { it.readText() })

	@Test
	fun everyTagIsAUiTagConstantOrFitsABuilderTemplate() {
		assertTrue(real.hasTags, "no UiTags found")

		val unknown = resolvedTags().filter { tag -> tag !in exceptions && !real.accepts(tag) }

		assertTrue(unknown.isEmpty(), "tags that no UiTags declares: ${unknown.sorted()}")
	}

	@Test
	fun noBuilderTemplateLacksALiteralPrefix() {
		assertTrue(real.degenerate.isEmpty(), "templates that would accept any tag: ${real.degenerate}")
	}

	@Test
	fun theExceptionsAreStillUsed() {
		val stale = exceptions - resolvedTags()

		assertTrue(stale.isEmpty(), "exceptions no scenario uses: $stale")
	}

	@Test
	fun aMisspelledTagIsRejected() {
		assertTrue(real.accepts(AuthUiTags.SignInButton))
		assertFalse(real.accepts("auth_sign_in_buton"))
		assertFalse(real.accepts("not_a_tag_at_all"))
	}

	@Test
	fun aTemplateResolvesTheConstantsOfItsFileAndOnlyLeavesTheCallerPartFree() {
		val sources = UiTagSources(
			listOf(
				"""
				const val Action = "grade_action"
				fun actionFor(id: String): String = "${'$'}{Action}_${'$'}{id}"
				""".trimIndent()
			)
		)

		assertTrue(sources.degenerate.isEmpty())
		assertTrue(sources.accepts("grade_action_7"))
		assertFalse(sources.accepts("other_action_7"))
		assertFalse(sources.accepts("grade_acton_7"))
	}

	@Test
	fun aTemplateWithoutALiteralPrefixIsReportedInsteadOfAcceptingEveryUnderscoreTag() {
		val sources = UiTagSources(
			listOf("""fun both(a: String, b: String): String = "${'$'}{a}_${'$'}{b}"""")
		)

		assertEquals(listOf("${'$'}{a}_${'$'}{b}"), sources.degenerate)
		assertFalse(sources.accepts("any_tag"))
	}

	@Test
	fun aTemplateWhoseOnlyPrefixIsAnUnknownNameIsReportedToo() {
		val sources = UiTagSources(
			listOf("""fun unresolved(id: String): String = "${'$'}{Missing}_${'$'}{id}"""")
		)

		assertTrue(sources.degenerate.isNotEmpty())
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
