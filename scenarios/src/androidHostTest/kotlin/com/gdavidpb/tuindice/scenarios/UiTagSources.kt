package com.gdavidpb.tuindice.scenarios

/**
 * What the `*UiTags.kt` files declare: the constants, and a pattern per builder function. A builder template
 * resolves the constants of its own file (`"${Prefix}_$id"` becomes the constant's value, then `_`, then
 * anything), so the part of a tag the product fixes is checked and only what the caller supplies is free.
 *
 * A template whose literal prefix is empty (`"${a}_${b}"`) would accept any tag with an underscore, so it is
 * reported in [degenerate] instead of being turned into a pattern.
 */
internal class UiTagSources(files: List<String>) {
	private val constantDeclaration = Regex("""const\s+val\s+(\w+)\s*=\s*"([^"]+)"""")
	private val builderDeclaration = Regex("""fun\s+\w+\([^)]*\)\s*:\s*String\s*=\s*"((?:[^"\\]|\\.)*)"""")
	private val interpolation = Regex("""\$\{([^}]*)}|\$(\w+)""")

	val constants: Set<String>

	/** The templates that cannot be checked, as written. */
	val degenerate: List<String>

	private val patterns: List<Regex>

	init {
		val constantsFound = mutableSetOf<String>()
		val degenerateFound = mutableListOf<String>()
		val patternsFound = mutableListOf<Regex>()

		files.forEach { text ->
			val ownConstants = constantDeclaration.findAll(text).associate { it.groupValues[1] to it.groupValues[2] }

			constantsFound += ownConstants.values

			builderDeclaration.findAll(text).map { it.groupValues[1] }
				.filter { interpolation.containsMatchIn(it) }
				.forEach { template ->
					val resolved = resolve(template, ownConstants)
					val literals = resolved.split(interpolation)

					if (literals.first().isEmpty()) {
						degenerateFound += template
					} else {
						patternsFound += Regex(literals.joinToString(".+") { Regex.escape(it) })
					}
				}
		}

		constants = constantsFound
		degenerate = degenerateFound
		patterns = patternsFound
	}

	val hasTags: Boolean get() = constants.isNotEmpty() && patterns.isNotEmpty()

	/** Whether [tag] is one of the constants or fits the pattern of a builder. */
	fun accepts(tag: String): Boolean = tag in constants || patterns.any { it.matches(tag) }

	/** Replaces the interpolations that name a constant of the same file by the constant's value. */
	private fun resolve(template: String, ownConstants: Map<String, String>): String =
		interpolation.replace(template) { match ->
			val name = match.groupValues[1].ifEmpty { match.groupValues[2] }

			ownConstants[name] ?: match.value
		}
}
