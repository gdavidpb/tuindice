package com.gdavidpb.tuindice.scenarios

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * What the sources of the two drivers must keep true and no device test can see:
 *
 * - every refusal names the primitive that was refused as its first word (ZC-3), because the harness counts the
 *   `[refusal] <reason>` lines by that word and a key such as `tag:x` says nothing;
 * - the tags the Android probes aim at are tags the product declares (they are literals of the probe app, outside the
 *   catalog, so no other test sees them go stale).
 */
class DriverSourcesTest {
	private val androidDriver = RepoFiles.file("scenariorunner/src/main").walkTopDown()
		.filter { it.extension == "kt" }
		.toList()
	private val iosDriver = RepoFiles.file("iosApp/UITests")
		.listFiles { file -> file.extension == "swift" }
		.orEmpty()
		.toList()

	private fun violations(files: List<File>) = files.flatMap { DriverRefusalSources.violations(it.name, it.readText()) }

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
		val comment = "/** Writes it through `refuse(\"tag:x\")` */\n\t * `refuse(reason)` is the funnel\n// refuse(\"x\")\n"

		assertEquals(emptyList(), DriverRefusalSources.violations("a.kt", literal))
		assertEquals(emptyList(), DriverRefusalSources.violations("a.kt", forwarded))
		assertEquals(1, DriverRefusalSources.violations("a.kt", variable).size)
		assertEquals(1, DriverRefusalSources.violations("a.kt", notDeclared).size)
		assertEquals(emptyList(), DriverRefusalSources.violations("a.kt", comment))
	}

	@Test
	fun theTagsOfTheAndroidProbesAreTagsTheProductDeclares() {
		val tags = UiTagSources(RepoFiles.uiTagFiles().map { it.readText() })
		val literal = Regex("""Query\.Tag\(\s*"([^"]+)"""")
		val probed = androidDriver.flatMap { file -> literal.findAll(file.readText()).map { file.name to it.groupValues[1] } }

		assertTrue(probed.isNotEmpty(), "no tag literal found in the probe sources")
		val unknown = probed.filter { (_, tag) -> !tags.accepts(tag) }

		assertTrue(unknown.isEmpty(), "tags the probes use that no UiTags declares: $unknown")
	}

	@Test
	fun theCheckOfTheProbeTagsSeesATagThatWentStale() {
		val tags = UiTagSources(RepoFiles.uiTagFiles().map { it.readText() })

		assertTrue(tags.accepts("auth_password_text_field"))
		assertTrue(!tags.accepts("auth_password_text_feld"))
	}
}
