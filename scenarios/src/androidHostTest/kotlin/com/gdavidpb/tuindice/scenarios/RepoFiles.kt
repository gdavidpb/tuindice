package com.gdavidpb.tuindice.scenarios

import java.io.File

/** Host tests run from the module directory; the repo files they check sit one level up. */
internal object RepoFiles {
	private val root = File("..")

	val catalogSources = File("src/commonMain/kotlin/com/gdavidpb/tuindice/scenarios/catalog")

	val loginMappings = File(root, "mocks/mappings/login")

	val allMappings = File(root, "mocks/mappings")

	/** The `*UiTags.kt` of every module, the one place app tags are declared. */
	fun uiTagFiles(): List<File> =
		root.listFiles { file -> file.isDirectory }.orEmpty()
			.map { File(it, "src/commonMain") }
			.filter { it.isDirectory }
			.flatMap { source -> source.walkTopDown().filter { it.isFile && it.name.endsWith("UiTags.kt") }.toList() }
			.sortedBy { it.path }
}
