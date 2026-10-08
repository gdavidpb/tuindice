package com.gdavidpb.tuindice.scenarios

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The host tests read repo files that belong to no classpath of this module, so Gradle only reruns them when
 * the build script declares those files as inputs. Each path a test reads must be named there.
 */
class TestInputsDeclaredTest {
	private val script = File("build.gradle.kts").readText()

	/** What each reading test needs, by the path fragment the build script has to contain. */
	private val readByTests = mapOf(
		"mocks" to "mappings, bodies and transformers (MockContractTest, EntityFixturesTest, AccountFixturesTest...)",
		"*UiTags.kt" to "the tags (CatalogTagsTest)",
		"composeResources/values/strings.xml" to "the string resources (CopyTest)",
		"presentation/contract" to "the action contracts (ActionCoverageTest)",
		"mapper/CoachmarkSurface.kt" to "the screens' coachmarks (CoachmarkCoverageTest)",
		"mapper/AcademicWeek.kt" to "the last week of the evaluations strip (E2eClockFixtureTest)",
		"data/source/DebugSubjectScenarioResolver.kt" to "the debug subjects (SubjectSearchFixturesTest)",
		"presentation/machine/SubjectSearchMachine.kt" to "the minimum search length (SubjectSearchFixturesTest)",
		"app/src/debug" to "the Android debug host (LaunchArgsParityTest)",
		"maincore/src/iosMain" to "the iOS debug code (LaunchArgsParityTest)",
		"iosApp/Sources/TuIndiceHost" to "the iOS Swift host (LaunchArgsParityTest)"
	)

	@Test
	fun everyFileTheTestsReadIsDeclaredAsAnInput() {
		val missing = readByTests.filterKeys { it !in script }

		assertTrue(missing.isEmpty(), "build.gradle.kts does not declare as inputs: $missing")
	}

	@Test
	fun theCheckSeesAPathTheScriptLacks() {
		val missing = readByTests.filterKeys { it !in script.replace("iosApp/Sources/TuIndiceHost", "") }

		assertTrue("iosApp/Sources/TuIndiceHost" in missing)
	}
}
