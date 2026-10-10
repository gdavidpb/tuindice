package com.gdavidpb.tuindice.scenariokit.dsl

import com.gdavidpb.tuindice.scenariokit.model.Site
import java.io.File

private const val DSL_PACKAGE = "com.gdavidpb.tuindice.scenariokit.dsl."
private const val SCENARIO_SOURCES = "scenarios/src/commonMain/kotlin"
private val REPO_ROOTS = listOf(File(".."), File("."))
private val resolved = HashMap<String, String>()

/** First stack frame outside the DSL, with the file path made repo-relative when it sits in `:scenarios`. */
internal actual fun captureCallSite(): Site? =
	Throwable("call site").stackTrace
		.firstOrNull { !it.className.startsWith(DSL_PACKAGE) && it.fileName != null && it.lineNumber > 0 }
		?.let { Site(repoRelative(it.fileName.orEmpty()), it.lineNumber) }

private fun repoRelative(fileName: String): String = synchronized(resolved) {
	resolved.getOrPut(fileName) { locate(fileName) ?: fileName }
}

private fun locate(fileName: String): String? =
	REPO_ROOTS.firstNotNullOfOrNull { root ->
		File(root, SCENARIO_SOURCES)
			.takeIf { it.isDirectory }
			?.walkTopDown()
			?.firstOrNull { it.isFile && it.name == fileName }
			?.canonicalFile
			?.relativeTo(root.canonicalFile)
			?.invariantSeparatorsPath
	}
