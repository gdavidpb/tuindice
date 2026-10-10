package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.debug.DebugLaunchArguments
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import com.gdavidpb.tuindice.scenarios.catalog.E2eContractFixture
import java.lang.reflect.Modifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * What the scenarios send as launch arguments must be what both debug hosts read: one definition,
 * parsed the same on Android and iOS, with each parsed value consumed on the platform it applies to.
 */
class LaunchArgsParityTest {
	private val androidDebug = RepoFiles.file("app/src/debug").walkTopDown()
		.filter { it.isFile && it.extension == "kt" }.joinToString("\n") { it.readText() }
	private val iosKotlin = RepoFiles.file("maincore/src/iosMain").walkTopDown()
		.filter { it.isFile && it.extension == "kt" }.joinToString("\n") { it.readText() }
	private val iosSwiftHost = RepoFiles.file("iosApp/Sources/TuIndiceHost").walkTopDown()
		.filter { it.isFile && it.extension == "swift" }.joinToString("\n") { it.readText() }

	/** Parsed property to where each platform consumes it; null means the platform does not apply it. */
	private val consumers: Map<String, Pair<String?, String?>> = mapOf(
		"apiBaseUrl" to (null to iosSwiftHost),
		"webBaseUrl" to (null to iosSwiftHost),
		"networkAvailable" to (androidDebug to iosSwiftHost),
		"animationsDisabled" to (androidDebug to iosKotlin),
		"fixedNow" to (androidDebug to iosKotlin),
		"availabilityNotice" to (androidDebug to iosKotlin),
		"sessionSeed" to (androidDebug to iosKotlin)
	)

	@Test
	fun everyParsedPropertyIsConsumedOnTheRightPlatforms() {
		val properties = DebugLaunchArguments::class.java.declaredFields
			.filter { !Modifier.isStatic(it.modifiers) }.map { it.name }.toSet()

		assertEquals(consumers.keys, properties, "a launch argument was added or removed: update this table")

		consumers.forEach { (property, hosts) ->
			val (android, ios) = hosts

			android?.let { assertTrue(".$property" in it, "the Android debug code never reads '$property'") }
			ios?.let { assertTrue(".$property" in it, "the iOS host never reads '$property'") }
		}
	}

	@Test
	fun bothHostsParseTheValuesTheSameWay() {
		assertTrue("DebugLaunchArguments.parse(" in androidDebug)
		assertTrue("DebugLaunchArguments.companion.parse(" in iosSwiftHost)
		assertTrue("DebugLaunchArguments.PREFIX" in androidDebug)
		assertTrue("DebugLaunchArguments.companion.PREFIX" in iosSwiftHost)
	}

	@Test
	fun everyLaunchTheCatalogMakesParses() {
		val launches = E2eCatalog.all.flatMap { scenario ->
			listOf(scenario.id to scenario.start.arguments) +
				scenario.steps.flattened().filterIsInstance<Step.Relaunch>().map { "${scenario.id} (relaunch)" to it.arguments }
		} + ("contract fixture" to E2eContractFixture.fixture.start.arguments)

		launches.forEach { (name, arguments) ->
			val unknown = arguments.keys.filter { it !in DebugLaunchArguments.keys }

			assertTrue(unknown.isEmpty(), "$name sends keys the app does not declare: $unknown")
			DebugLaunchArguments.parse(arguments)
		}
	}

	@Test
	fun theCatalogNeverSendsTheKeysTheIosDriverOwns() {
		val driverOwned = setOf(DebugLaunchArguments.API_BASE_URL, DebugLaunchArguments.WEB_BASE_URL)
		val sent = E2eCatalog.all.flatMap { it.start.arguments.keys }.toSet()

		assertTrue(sent.none { it in driverOwned }, "the iOS driver adds the base URLs itself")
	}
}
