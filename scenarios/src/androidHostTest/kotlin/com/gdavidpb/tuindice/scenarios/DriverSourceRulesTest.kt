package com.gdavidpb.tuindice.scenarios

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * What else the sources of the two drivers must keep true and no device test can see:
 *
 * - the three lists of the primitives a refusal may name are the same (YB-10);
 * - every tolerance goes through the funnel with a declared key (YB-9), because the harness counts the
 *   `[tolerance] <key>` lines;
 * - the focus and text paths of the Android driver make no touch of their own (YB-2);
 * - the Objective-C shim is built with ARC (YB-10).
 *
 * The rules about refusals are in [DriverSourcesTest].
 */
class DriverSourceRulesTest {
	private val androidDriver = DriverSourceFiles.android
	private val iosDriver = DriverSourceFiles.ios

	private fun code(files: List<java.io.File>) =
		files.joinToString("\n") { DriverRefusalSources.withoutComments(it.readText()) }

	@Test
	fun theThreeListsOfPrimitivesAreTheSame() {
		val kotlin = androidDriver.single { it.name == "DriverLog.kt" }.readText()
		val swift = iosDriver.single { it.name == "RunConfig.swift" }.readText()

		for (primitive in DriverRefusalSources.PRIMITIVES) {
			assertTrue("`$primitive`" in kotlin, "the KDoc of DriverLog.refuse does not name `$primitive`")
			assertTrue("`$primitive`" in swift, "the comment of DriverLog.refuse in RunConfig.swift lacks `$primitive`")
		}
	}

	@Test
	fun everyToleranceOfTheAndroidDriverNamesADeclaredKey() {
		val keys = DriverToleranceSources.ANDROID_KEYS
		val violations = androidDriver.flatMap { DriverToleranceSources.violations(it.name, it.readText(), keys) }
		val used = code(androidDriver)

		assertEquals(emptyList(), violations, "tolerances that do not name a declared key")
		for (key in keys) {
			assertTrue(
				Regex("""tolerate\(\s*"$key"""").containsMatchIn(used),
				"the key $key is declared and nobody tolerates it"
			)
		}
	}

	@Test
	fun everyToleranceOfTheIosDriverNamesACaseOfTheEnum() {
		val cases = DriverToleranceSources.swiftCases(iosDriver.single { it.name == "RunConfig.swift" }.readText())
		val violations = iosDriver.flatMap { DriverToleranceSources.violations(it.name, it.readText(), cases) }
		val used = code(iosDriver)

		assertTrue(cases.isNotEmpty(), "no case of enum Tolerance found")
		assertEquals(emptyList(), violations, "tolerances that do not name a case of the enum")
		for (case in cases) {
			assertTrue(
				Regex("""tolerate\(\s*\.$case\b""").containsMatchIn(used),
				"the case $case is declared and nobody tolerates it"
			)
		}
	}

	@Test
	fun theToleranceCheckSeesAKeyNobodyDeclaredAndAVariable() {
		val keys = DriverToleranceSources.ANDROID_KEYS
		val kotlin = "fun f() {\n\tsession.log.tolerate(\"keyboard-gone\", \"x\")\n" +
			"\tsession.log.tolerate(key, \"y\")\n}\n"
		val fine = "fun g() {\n\tsession.log.tolerate(\n\t\t\"foreground-request\",\n\t\t\"x\"\n\t)\n}\n" +
			"fun tolerate(key: String, detail: String) {}\n"
		val swift = "func f() {\n    log.tolerate(.unknownKind, \"x\")\n    log.tolerate(.dismissedAlert, \"y\")\n}\n"
		val enumText = "enum Tolerance: String {\n    /** one */\n" +
			"    case dismissedAlert = \"dismissed-alert\"\n}\nlet x = 1\n"

		assertEquals(2, DriverToleranceSources.violations("a.kt", kotlin, keys).size)
		assertEquals(emptyList(), DriverToleranceSources.violations("a.kt", fine, keys))
		assertEquals(setOf("dismissedAlert"), DriverToleranceSources.swiftCases(enumText))
		assertEquals(1, DriverToleranceSources.violations("a.swift", swift, setOf("dismissedAlert")).size)
	}

	@Test
	fun theAndroidFocusAndTextPathsMakeNoTouchOfTheirOwn() {
		val paths = listOf("FieldFocus.kt", "TextInjector.kt", "KeyInjector.kt")

		for (name in paths) {
			val file = androidDriver.single { it.name == name }

			assertEquals(emptyList(), TouchSources.touches(name, file.readText()), "$name puts a touch on the screen")
		}
	}

	@Test
	fun theTouchCheckSeesEveryWayToTouchTheScreen() {
		val clicks = "fun f() {\n\tselectors.find(q)?.click()\n\tdevice.click(1, 2)\n\tnode.longClick()\n}\n"
		val injected = "fun g() {\n\tval e = MotionEvent.obtain(down, up, action, x, y, state)\n" +
			"\tinstrumentation.sendPointerSync(e)\n}\n"
		val comment = "/** Not `UiObject2.click()`: a touch on a point. */\nfun h() {\n\t// device.click(1, 2)\n}\n"

		assertEquals(
			listOf("a.kt:2: .click(", "a.kt:3: .click(", "a.kt:4: .longClick("),
			TouchSources.touches("a.kt", clicks)
		)
		assertEquals(
			listOf("a.kt:2: MotionEvent", "a.kt:3: sendPointerSync"),
			TouchSources.touches("a.kt", injected)
		)
		assertEquals(emptyList(), TouchSources.touches("a.kt", comment))
	}

	@Test
	fun theCheckOfTheProbeTagsSeesATagThatWentStale() {
		val tags = UiTagSources(RepoFiles.uiTagFiles().map { it.readText() })

		assertTrue(tags.accepts("auth_password_text_field"))
		assertTrue(!tags.accepts("auth_password_text_feld"))
	}

	@Test
	fun theObjectiveCShimIsCompiledWithArc() {
		val config = RepoFiles.file("iosApp/Config/UITests.xcconfig").readText()
		val setting = Regex("""(?m)^CLANG_ENABLE_OBJC_ARC\s*=\s*YES\s*$""")
		val off = config.replace("CLANG_ENABLE_OBJC_ARC = YES", "CLANG_ENABLE_OBJC_ARC = NO")
		val commented = config.replace("CLANG_ENABLE_OBJC_ARC = YES", "// CLANG_ENABLE_OBJC_ARC = YES")

		assertTrue(setting.containsMatchIn(config), "UITests.xcconfig does not set CLANG_ENABLE_OBJC_ARC = YES")
		assertTrue(!setting.containsMatchIn(off))
		assertTrue(!setting.containsMatchIn(commented))
	}
}
