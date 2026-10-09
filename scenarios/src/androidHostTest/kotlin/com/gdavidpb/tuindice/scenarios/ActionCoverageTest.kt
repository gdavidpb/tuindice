package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.catalog.ActionDisposition
import com.gdavidpb.tuindice.scenarios.catalog.ActionDispositions
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import com.gdavidpb.tuindice.scenarios.catalog.scenarioModuleOf
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Every MVI action a user can trigger is covered by a scenario, or dispositioned as internal or
 * platform-edge.
 */
class ActionCoverageTest {
	private val contractActions: Set<String> = contractActions()
	private val covered: Set<String> = E2eCatalog.all.flatMap { it.covers }.toSet()
	private val dispositioned: Set<String> = ActionDispositions.all.map { it.action }.toSet()

	@Test
	fun theContractSourcesYieldActions() {
		assertTrue(contractActions.size >= MINIMUM_ACTIONS, "only ${contractActions.size} actions found")
		assertTrue(contractActions.contains("auth.SignIn.ClickCancelSignIn"))
	}

	@Test
	fun scenariosCoverOnlyActionsThatExist() {
		E2eCatalog.all.forEach { scenario ->
			val unknown = scenario.covers.filter { it !in contractActions }

			assertTrue(unknown.isEmpty(), "${scenario.id} covers actions no contract declares: $unknown")
		}
	}

	@Test
	fun dispositionsAreNeitherStaleNorDuplicated() {
		val actions = ActionDispositions.all.map { it.action }

		val duplicated = actions.groupBy { it }.filterValues { it.size > 1 }.keys

		assertEquals(actions.toSet().size, actions.size, "duplicate dispositions: $duplicated")
		assertTrue(actions.all { it in contractActions }, "stale dispositions: ${actions.filter { it !in contractActions }}")
		assertTrue(ActionDispositions.all.all { it.reason.isNotBlank() })
	}

	@Test
	fun theDispositionsKeepTheirCountsPerKind() {
		assertEquals(INTERNAL_COUNT, ActionDispositions.all.count { it is ActionDisposition.Internal })
		assertEquals(PLATFORM_EDGE_COUNT, ActionDispositions.all.count { it is ActionDisposition.PlatformEdge })
		assertEquals(PENDING_COUNT, ActionDispositions.all.count { it is ActionDisposition.Pending })
	}

	@Test
	fun noActionIsBothCoveredAndDispositionedWhateverTheKind() {
		val overlap = overlapOf(covered, ActionDispositions.all)

		assertTrue(
			overlap.isEmpty(),
			"actions a scenario covers and a disposition also excuses; keep only the true one: $overlap"
		)
	}

	@Test
	fun theOverlapCheckSeesEveryKindOfDisposition() {
		val dispositions = listOf(
			ActionDisposition.Internal("a.A.One", "it fires itself"),
			ActionDisposition.PlatformEdge("a.A.Two", "the OS does it"),
			ActionDisposition.Pending("a.A.Three", "nobody fires it"),
			ActionDisposition.Internal("a.A.Four", "it fires itself")
		)

		assertEquals(
			listOf("a.A.One", "a.A.Two", "a.A.Three"),
			overlapOf(setOf("a.A.One", "a.A.Two", "a.A.Three", "a.A.Five"), dispositions)
		)
	}

	@Test
	fun everyActionIsCoveredOrDispositioned() {
		val uncovered = contractActions.filter { it !in covered && it !in dispositioned }

		assertTrue(uncovered.isEmpty(), "actions with no scenario and no disposition: ${uncovered.sorted()}")
	}

	@Test
	fun everyActionModuleHasAScenarioModule() {
		val modules = E2eCatalog.byModule.keys

		assertTrue(
			contractActions.map { scenarioModuleOf(it.substringBefore('.')) }.toSet()
				.all { it in modules },
			"an action module has no scenario module"
		)
	}

	/**
	 * `<module>.<Contract>.<Action>` for every action of every `presentation/contract` file, found the way the old
	 * shell validator found them: lines that open a nested class or object while inside `sealed ... Action`.
	 */
	private fun contractActions(): Set<String> =
		RepoFiles.moduleDirectories().flatMap { module ->
			File(module, "src/commonMain/kotlin").walkTopDown()
				.filter { it.isFile && it.extension == "kt" && it.parentFile.path.endsWith("presentation/contract") }
				.flatMap { file -> actionsOf(file).map { "${module.name}.${file.nameWithoutExtension}.$it" } }
				.toList()
		}.toSet()

	private fun actionsOf(file: File): List<String> {
		val actions = mutableListOf<String>()
		var inAction = false
		var depth = 0

		file.readLines().forEach { line ->
			if (!inAction) {
				if (actionStart.containsMatchIn(line)) {
					inAction = true
					depth = line.count { it == '{' } - line.count { it == '}' }
				}
			} else {
				val code = line.substringBefore("//")

				declaration.find(code.trimStart())?.let { actions += it.groupValues[1] }
				depth += code.count { it == '{' } - code.count { it == '}' }
				if (depth <= 0) inAction = false
			}
		}

		return actions
	}

	private companion object {
		const val MINIMUM_ACTIONS = 100
		const val INTERNAL_COUNT = 24
		const val PLATFORM_EDGE_COUNT = 6
		const val PENDING_COUNT = 0
		val actionStart = Regex("""^\s*sealed\s+(class|interface)\s+Action(\s|:|\{|$)""")
		val declaration = Regex("""^(?:data\s+)?(?:object|class)\s+([A-Za-z_][A-Za-z0-9_]*)""")
	}
}

private fun overlapOf(covers: Set<String>, dispositions: List<ActionDisposition>): List<String> =
	dispositions.map { it.action }.filter { it in covers }
