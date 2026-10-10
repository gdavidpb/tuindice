package com.gdavidpb.tuindice.scenarios

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * A step knows the line that built it by the name of the file alone (`CallSite.android` finds that file under
 * `scenarios/src/commonMain` and takes the first one with the name), so two sources of the scenarios with the same name
 * would send every failure of one of them to the other.
 */
class SourceFileNamesTest {
	@Test
	fun noTwoScenarioSourcesShareAFileName() {
		val sources = File("src/commonMain").walkTopDown().filter { it.isFile && it.name.endsWith(".kt") }.toList()
		val repeated = sources.groupBy { it.name }.filterValues { it.size > 1 }

		assertTrue(sources.isNotEmpty(), "no scenario source was found from ${File(".").absolutePath}")
		assertTrue(repeated.isEmpty(), "file names used twice: ${repeated.mapValues { (_, files) -> files.map { it.path } }}")
	}
}
