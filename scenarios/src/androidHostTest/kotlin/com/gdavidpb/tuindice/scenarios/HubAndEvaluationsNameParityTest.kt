package com.gdavidpb.tuindice.scenarios

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * The name of an evaluation is written the same on the summary hub and in Evaluations. Each of the two modules keeps
 * its own copy of those texts: a module cannot read another module's resources without exposing base's, so this test
 * is what keeps the copies equal.
 */
class HubAndEvaluationsNameParityTest {
	@Test
	fun theNameAndTheTypeLabelsOfAnEvaluationReadTheSameOnTheHubAndInEvaluations() {
		val evaluations = stringsOf("evaluations")
		val summary = stringsOf("summary")

		PAIRS.forEach { (evaluationsName, summaryName) ->
			assertEquals(
				textOf(evaluations, evaluationsName, "evaluations"),
				textOf(summary, summaryName, "summary"),
				"'$evaluationsName' and '$summaryName' read differently"
			)
		}
	}

	private fun textOf(strings: Map<String, String>, name: String, module: String): String =
		strings[name] ?: fail("no string '$name' in module '$module'")

	/** Resource name to its text in a module's `strings.xml`, with the Android escapes read as on the screen. */
	private fun stringsOf(module: String): Map<String, String> {
		val file = File(RepoFiles.file(module), "src/commonMain/composeResources/values/strings.xml")
		val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
		val strings = document.getElementsByTagName("string")

		return (0 until strings.length).associate { index ->
			val node = strings.item(index)

			node.attributes.getNamedItem("name").nodeValue to unescape(node.textContent)
		}
	}

	private fun unescape(raw: String): String = raw.replace("\\'", "'").replace("\\\"", "\"").replace("\\n", "\n")

	private companion object {
		val TYPES = listOf(
			"test", "essay", "attendance", "interventions", "laboratory", "model", "presentation", "project", "quiz",
			"report", "workshop", "written_work", "other"
		)

		// The name of an evaluation, then each of its 13 type labels: the summary keeps them under its own names.
		val PAIRS = listOf("evaluation_name" to "summary_evaluation_name") +
			TYPES.map { type -> "evaluation_$type" to "summary_evaluation_type_$type" }
	}
}
