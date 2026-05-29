package com.gdavidpb.tuindice.base.utils.extension

fun String.capitalize(): String {
	return replaceFirstChar { char ->
		if (char.isLowerCase()) char.titlecase() else char.toString()
	}
}

fun String.toSnakeCase(): String {
	return replace(Regex("([a-z0-9])([A-Z])"), "$1_$2")
		.replace(Regex("([A-Z])([A-Z][a-z])"), "$1_$2")
		.lowercase()
}
