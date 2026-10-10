package com.gdavidpb.tuindice.scenariokit.model

/** Prefix of [describe] for OS-level elements. */
const val SYSTEM_QUERY_PREFIX = "system:"

fun Query.describe(): String = when (this) {
	is Query.Tag -> "tag:$value"
	is Query.Text -> if (contains) "text~\"$value\"" else "text:\"$value\""
	is Query.System -> "$SYSTEM_QUERY_PREFIX$value"
}
