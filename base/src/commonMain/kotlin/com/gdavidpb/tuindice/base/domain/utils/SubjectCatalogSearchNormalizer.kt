package com.gdavidpb.tuindice.base.domain.utils

object SubjectCatalogSearchNormalizer {
	private val whitespaceRegex = Regex("\\s+")

	fun normalize(value: String): String {
		return value
			.trim()
			.uppercase()
			.map(::foldAccent)
			.joinToString(separator = "")
			.replace(whitespaceRegex, " ")
	}

	private fun foldAccent(char: Char): Char {
		return when (char) {
			'Á', 'À', 'Ä', 'Â', 'Ã' -> 'A'
			'É', 'È', 'Ë', 'Ê' -> 'E'
			'Í', 'Ì', 'Ï', 'Î' -> 'I'
			'Ó', 'Ò', 'Ö', 'Ô', 'Õ' -> 'O'
			'Ú', 'Ù', 'Ü', 'Û' -> 'U'
			'Ñ' -> 'N'
			else -> char
		}
	}
}
