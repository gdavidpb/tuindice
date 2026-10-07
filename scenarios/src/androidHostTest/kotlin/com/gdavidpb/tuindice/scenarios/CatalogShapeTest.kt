package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.codec.ScenarioNaming
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import com.gdavidpb.tuindice.scenarios.catalog.MigrationProgress
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalogShapeTest {
	private val scenarios = E2eCatalog.all
	private val idShape = Regex("[a-z0-9-]+")
	private val declaration = Regex("""val\s+\w+\s*=\s*scenario\(\s*"([^"]+)"""")

	@Test
	fun idsAreUniqueAndWellFormedWithTheModuleAsPrefix() {
		assertEquals(scenarios.size, scenarios.map { it.id }.toSet().size, "duplicate scenario ids")

		scenarios.forEach { scenario ->
			assertTrue(idShape.matches(scenario.id), "'${scenario.id}' is not [a-z0-9-]+")
			assertTrue(
				scenario.id.startsWith("${scenario.module}-") || scenario.id.startsWith("poc-"),
				"'${scenario.id}' does not start with its module '${scenario.module}'"
			)
		}
	}

	@Test
	fun swiftMethodNamesAreInjective() {
		val byName = scenarios.groupBy { ScenarioNaming.swiftMethodName(it.id) }
		val clashes = byName.filterValues { it.size > 1 }.mapValues { (_, value) -> value.map { it.id } }

		assertTrue(clashes.isEmpty(), "ids that map to one Swift method: $clashes")
	}

	@Test
	fun everyScenarioHasSteps() {
		val empty = scenarios.filter { it.steps.isEmpty() }.map { it.id }

		assertTrue(empty.isEmpty(), "scenarios without steps: $empty")
	}

	@Test
	fun everyDeclaredScenarioIsListedInTheCatalog() {
		val declared = RepoFiles.catalogSources.listFiles { file -> file.extension == "kt" }.orEmpty()
			.flatMap { file -> declaration.findAll(file.readText()).map { it.groupValues[1] }.toList() }
			.sorted()

		assertTrue(declared.isNotEmpty(), "no scenario declarations found under ${RepoFiles.catalogSources}")
		assertEquals(scenarios.map { it.id }.sorted(), declared, "E2eCatalog.all differs from the declared scenarios")
	}

	@Test
	fun everyTranslatedModuleHasASmokeScenario() {
		val translated = E2eCatalog.byModule.keys.filter { it != "poc" && it !in MigrationProgress.pendingModules }

		translated.forEach { module ->
			assertTrue(scenarios.any { it.module == module && "smoke" in it.tags }, "module '$module' has no smoke scenario")
		}
	}

	@Test
	fun noScenarioCarriesItsOwnModuleAsATag() {
		val repeated = scenarios.filter { it.module in it.tags }.map { it.id }

		assertTrue(repeated.isEmpty(), "scenarios tagged with their own module, which the model already holds: $repeated")
	}

	@Test
	fun aQuarantineHasAReasonAndAnIsoDate() {
		scenarios.mapNotNull { scenario -> scenario.quarantine?.let { scenario.id to it } }.forEach { (id, quarantine) ->
			assertTrue(quarantine.reason.isNotBlank(), "$id is quarantined without a reason")
			LocalDate.parse(quarantine.until)
		}
	}
}
