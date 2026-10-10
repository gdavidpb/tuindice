package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Translating a module touches one file: `catalog/<Module>Scenarios.kt` declares the scenarios of that module and
 * lists them, and `E2eCatalog` already includes the list. This keeps each scenario in its own module's file.
 */
class ModuleCatalogTest {
	private val declaration = Regex("""val\s+\w+\s*=\s*scenario\(\s*"([^"]+)"\s*,\s*"([^"]+)"""")

	@Test
	fun everyModuleHasItsOwnScenarioFileExposingItsList() {
		E2eCatalog.byModule.keys.forEach { module ->
			val file = moduleFile(module)

			assertTrue(file.isFile, "missing ${file.path}")
			assertTrue(
				"val ${module}Scenarios: List<Scenario>" in file.readText(),
				"${file.name} does not expose ${module}Scenarios"
			)
		}
	}

	@Test
	fun everyScenarioSitsInTheListOfItsModule() {
		E2eCatalog.byModule.forEach { (module, scenarios) ->
			scenarios.forEach { assertEquals(module, it.module, "${it.id} is in the list of '$module'") }
		}
	}

	@Test
	fun eachFileDeclaresExactlyTheScenariosOfItsListUnderItsOwnModuleName() {
		E2eCatalog.byModule.forEach { (module, scenarios) ->
			val file = moduleFile(module)
			val declared = declaration.findAll(file.readText()).map { it.groupValues[1] to it.groupValues[2] }.toList()

			assertEquals(
				scenarios.map { it.id }.sorted(),
				declared.map { it.first }.sorted(),
				"${file.name} and its list differ"
			)
			declared.forEach { (id, declaredModule) ->
				assertEquals(module, declaredModule, "$id is declared with module '$declaredModule'")
			}
		}
	}

	@Test
	fun noScenarioFileIsMissingFromTheCatalog() {
		val files = RepoFiles.catalogSources.listFiles { file -> file.name.endsWith("Scenarios.kt") }.orEmpty()
			.map { it.name }.toSet()

		assertEquals(E2eCatalog.byModule.keys.map { moduleFile(it).name }.toSet(), files)
	}

	private fun moduleFile(module: String): File =
		File(RepoFiles.catalogSources, "${module.replaceFirstChar { it.uppercase() }}Scenarios.kt")
}
