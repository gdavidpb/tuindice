package com.gdavidpb.tuindice.scenarios

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * What the sources of the two drivers must keep true about their refusals and no device test can see:
 *
 * - every refusal names the primitive that was refused as its first word (ZC-3), because the harness counts the
 *   `[refusal] <reason>` lines by that word and a key such as `tag:x` says nothing; so does every literal forwarded
 *   by a helper (YB-8), and the call may be wrapped over several lines;
 * - the tags the Android probes aim at are tags the product declares (they are literals of the probe app, outside the
 *   catalog, so no other test sees them go stale).
 *
 * The rules about tolerances, touches and build settings are in [DriverSourceRulesTest].
 */
class DriverSourcesTest {
	private val androidDriver = DriverSourceFiles.android
	private val iosDriver = DriverSourceFiles.ios

	private fun violations(files: List<File>) =
		files.flatMap { DriverRefusalSources.violations(it.name, it.readText()) }

	@Test
	fun theSourcesOfBothDriversWereFound() {
		assertTrue(androidDriver.any { it.name == "DriverLog.kt" }, "the Android driver sources: $androidDriver")
		assertTrue(iosDriver.any { it.name == "XCUIScenarioDriver.swift" }, "the iOS driver sources: $iosDriver")
	}

	@Test
	fun everyRefusalOfTheAndroidDriverNamesAPrimitive() {
		assertEquals(emptyList(), violations(androidDriver), "refusals that do not begin with a primitive")
	}

	@Test
	fun everyRefusalOfTheIosDriverNamesAPrimitive() {
		assertEquals(emptyList(), violations(iosDriver), "refusals that do not begin with a primitive")
	}

	@Test
	fun theCheckSeesARefusalThatBeginsWithATargetInsteadOfAPrimitive() {
		val kotlin = "fun f() {\n\tsession.log.refuse(\"tag:x: not on screen\")\n}\n"
		val swift = "func f() -> Bool {\n    return refuse(\"\\(q): the app is not running\")\n}\n"
		val guarded = "func g() {\n    _ = guarded(\"dismiss alert\", \"alert\", log: log) {}\n}\n"

		assertEquals(1, DriverRefusalSources.violations("a.kt", kotlin).size)
		assertEquals(1, DriverRefusalSources.violations("a.swift", swift).size)
		assertEquals(1, DriverRefusalSources.violations("a.swift", guarded).size)
	}

	@Test
	fun theCheckAcceptsAPrimitiveAndAForwardedPrimitiveButNotAnyVariable() {
		val literal = "fun f() {\n\tsession.log.refuse(\"typeKeys\", \"x\")\n\trefuse(\"guard\", \"y\")\n}\n"
		val forwarded = "private fun refuse(primitive: String, reason: String): Boolean {\n" +
			"\tsession.log.refuse(primitive, reason)\n}\n"
		val variable = "private fun other(gesture: String) {\n\tsession.log.refuse(gesture, \"z\")\n}\n"
		val notDeclared = "private fun other(name: String) {\n\tsession.log.refuse(primitive, \"z\")\n}\n"
		val comment = "/**\n * Writes it through `refuse(\"tag:x\")`\n * `refuse(reason)` is the funnel\n */\n" +
			"// refuse(\"x\")\n"

		assertEquals(emptyList(), DriverRefusalSources.violations("a.kt", literal))
		assertEquals(emptyList(), DriverRefusalSources.violations("a.kt", forwarded))
		assertEquals(1, DriverRefusalSources.violations("a.kt", variable).size)
		assertEquals(1, DriverRefusalSources.violations("a.kt", notDeclared).size)
		assertEquals(emptyList(), DriverRefusalSources.violations("a.kt", comment))
	}

	@Test
	fun theCheckSeesACallWhoseFirstArgumentIsOnTheNextLine() {
		val wrapped = "fun f() {\n\tsession.log.refuse(\n\t\t\"tag:x: not on screen\",\n\t\t\"y\"\n\t)\n}\n"
		val swift = "func f() -> Bool {\n    return refuse(\n        \"\\(q): the app is not running\"\n    )\n}\n"
		val fine = "fun f() {\n\tsession.log.refuse(\n\t\t\"tap\",\n\t\t\"y\"\n\t)\n}\n"

		assertEquals(1, DriverRefusalSources.violations("a.kt", wrapped).size)
		assertEquals(1, DriverRefusalSources.violations("a.swift", swift).size)
		assertEquals(emptyList(), DriverRefusalSources.violations("a.kt", fine))
	}

	@Test
	fun theCheckSeesAPrimitiveThatIsForwardedWithALiteralThatIsNotOne() {
		val named = "func f() {\n    _ = resolver.placed(q, primitive: \"long press\")\n}\n"
		val third = "private fun g(): Boolean {\n" +
			"\treturn click(place.centerX(), place.centerY(), \"tag:x\", \"on \$q\")\n}\n"
		val closure = "func h() {\n" +
			"    guard tapper(resolved, center(of: facts), \"caret\", \"\\(q)\") else { return }\n}\n"
		val fine = "func f() {\n    _ = resolver.placed(q, primitive: \"tap\")\n" +
			"    _ = click(1, 2, \"tapAt\", \"x\")\n    _ = tapper(r, p, \"clearText\", \"y\")\n}\n"
		val twoArguments = "fun g() {\n\tsession.device.click(x, y)\n}\n"

		assertEquals(1, DriverRefusalSources.violations("a.swift", named).size)
		assertEquals(1, DriverRefusalSources.violations("a.kt", third).size)
		assertEquals(1, DriverRefusalSources.violations("a.swift", closure).size)
		assertEquals(emptyList(), DriverRefusalSources.violations("a.swift", fine))
		assertEquals(emptyList(), DriverRefusalSources.violations("a.kt", twoArguments))
	}

	@Test
	fun theCommentsAreNotCodeAndAStringKeepsItsSlashes() {
		val text = "val url = \"http://x\" // refuse(\"tag:x\")\n/* refuse(\"tag:y\")\n   still comment */ val z = 1\n"
		val code = DriverRefusalSources.withoutComments(text)

		assertEquals(text.length, code.length, "the offsets and the lines stay")
		assertTrue(code.contains("\"http://x\""))
		assertTrue(!code.contains("refuse"))
	}

	@Test
	fun theTagsOfTheAndroidProbesAreTagsTheProductDeclares() {
		val tags = UiTagSources(RepoFiles.uiTagFiles().map { it.readText() })
		val literal = Regex("""Query\.Tag\(\s*"([^"]+)"""")
		val probed = androidDriver.flatMap { file ->
			literal.findAll(file.readText()).map { file.name to it.groupValues[1] }
		}

		assertTrue(probed.isNotEmpty(), "no tag literal found in the probe sources")
		val unknown = probed.filter { (_, tag) -> !tags.accepts(tag) }

		assertTrue(unknown.isEmpty(), "tags the probes use that no UiTags declares: $unknown")
	}
}
