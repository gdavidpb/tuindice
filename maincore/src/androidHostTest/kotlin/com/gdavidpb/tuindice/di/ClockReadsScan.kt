package com.gdavidpb.tuindice.di

import java.io.File

/** What the clock guards read: the sources of the app, and the patterns that read the clock in them. */
internal object ClockReadsScan {
	private val repoRoot = File("..")

	// The harness and its catalog are not the app.
	private val notTheApp = setOf("scenariokit", "scenariorunner", "scenarios", "testkit")

	// `Clock.System` by any route, and an alias of `Clock` itself (`import kotlin.time.Clock as C`).
	val systemClockReads = listOf(literal("Clock.System"), Regex("""import\s+[\w.]*Clock\s+as\s+\w+"""))

	// `currentTimeMillis` called or referenced (`::currentTimeMillis`).
	val millisClockReads = listOf(literal("currentTimeMillis"))

	val platformClockReads = listOf(
		literal("System.currentTimeMillis"),
		literal("NSDate"),
		Regex("""\b(?:LocalDate|LocalDateTime|LocalTime|ZonedDateTime|OffsetDateTime|Instant)\.now"""),
		literal("Calendar.getInstance"),
		Regex("""(?<![\w.])Date\(\)"""),
		literal("CFAbsoluteTimeGetCurrent"),
		Regex("""\bgetTimeMillis\b""")
	) + systemClockReads + millisClockReads

	val swiftClockReads = listOf(
		Regex("""(?<![\w.])Date\(\)"""),
		Regex("""\bDate\.(?:now|init\(\))"""),
		literal("NSDate()"),
		literal("CFAbsoluteTimeGetCurrent")
	)

	fun sourcesOf(sourceSet: String): Map<String, String> =
		repoRoot.listFiles { file -> file.isDirectory && file.name !in notTheApp }.orEmpty()
			.map { module -> File(module, "src/$sourceSet") }
			.let(::readKotlinFiles)

	// The host's production and debug source sets; its tests may read what they like.
	fun appSources(): Map<String, String> =
		listOf("main", "debug").map { variant -> File(repoRoot, "app/src/$variant") }.let(::readKotlinFiles)

	// The Swift of the iOS host: the files of the app, not Pods nor its tests.
	fun swiftSources(): Map<String, String> =
		File(repoRoot, "iosApp/Sources")
			.walkTopDown().filter { it.isFile && it.extension == "swift" }.toList()
			.associate { file -> file.relativeTo(repoRoot).path to file.readText() }

	fun findReads(sources: Map<String, String>, reads: List<Regex>, allowed: Set<String>): List<String> =
		sources.filter { (path, text) -> path !in allowed && readsAny(text, reads) }.keys.sorted()

	fun readsAny(text: String, reads: List<Regex>): Boolean = reads.any { read -> read.containsMatchIn(text) }

	private fun readKotlinFiles(roots: List<File>): Map<String, String> =
		roots.filter { it.isDirectory }
			.flatMap { dir -> dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList() }
			.associate { file -> file.relativeTo(repoRoot).path to file.readText() }

	private fun literal(text: String) = Regex(Regex.escape(text))
}
