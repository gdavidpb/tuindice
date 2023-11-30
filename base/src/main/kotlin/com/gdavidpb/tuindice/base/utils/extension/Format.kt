package com.gdavidpb.tuindice.base.utils.extension

import com.gdavidpb.tuindice.base.utils.DEFAULT_LOCALE

fun Double.formatGrade(decimals: Int) = "%.${decimals}f".format(round(decimals))

fun Float.formatGrade(decimals: Int) = "%.${decimals}f".format(toDouble().round(decimals))

fun String.capitalize() =
	replaceFirstChar { c ->
		if (c.isLowerCase())
			c.titlecase(DEFAULT_LOCALE)
		else
			"$c"
	}