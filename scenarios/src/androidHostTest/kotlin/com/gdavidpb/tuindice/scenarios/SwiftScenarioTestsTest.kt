package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.codec.ScenarioNaming
import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SwiftScenarioTestsTest {
	private fun scenario(id: String, platforms: List<Platform>) =
		Scenario(id = id, module = "conformance", start = LaunchSpec(emptyMap()), steps = emptyList(), platforms = platforms)

	@Test
	fun aScenarioThatDoesNotRunOnIosGetsNoSwiftTest() {
		val swift = SwiftScenarioTests.render(
			listOf(scenario("both", Platform.entries), scenario("android-only", listOf(Platform.Android)))
		)

		assertTrue(swift.contains("func test_both()"))
		assertFalse(swift.contains("android_only"))
	}

	@Test
	fun aModuleWithoutIosScenariosGetsNoClass() {
		val swift = SwiftScenarioTests.render(listOf(scenario("android-only", listOf(Platform.Android))))

		assertFalse(swift.contains("final class"))
	}

	@Test
	fun theCatalogSwiftHasExactlyTheIosScenarios() {
		val swift = SwiftScenarioTests.render(E2eCatalog.all)
		val generated = Regex("""runScenario\("([^"]+)"\)""").findAll(swift).map { it.groupValues[1] }.toSet()
		val onIos = E2eCatalog.all.filter { Platform.Ios in it.platforms }.map { it.id }.toSet()

		assertEquals(onIos, generated)
		assertFalse(swift.contains(ScenarioNaming.swiftMethodName("conformance-back") + "()"))
	}
}
