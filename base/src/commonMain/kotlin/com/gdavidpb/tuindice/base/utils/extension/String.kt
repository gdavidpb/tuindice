package com.gdavidpb.tuindice.base.utils.extension

fun String.capitalize(): String {
	return replaceFirstChar { char ->
		if (char.isLowerCase()) char.titlecase() else char.toString()
	}
}
