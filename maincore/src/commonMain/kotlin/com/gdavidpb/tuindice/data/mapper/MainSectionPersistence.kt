package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.base.domain.model.MainSection

private const val SUMMARY_VALUE = "summary"
private const val RECORD_VALUE = "record"
private const val PENSUM_VALUE = "pensum"
private const val EVALUATIONS_VALUE = "evaluations"

// «About» was the fifth tab until Settings took its place. The value is still read so a student who closed the app on that
// tab opens on the summary, the first section, instead of the app failing to read the preference.
private const val LEGACY_ABOUT_VALUE = "about"

fun MainSection.toPersistedName(): String = when (this) {
	MainSection.SUMMARY -> SUMMARY_VALUE
	MainSection.RECORD -> RECORD_VALUE
	MainSection.PENSUM -> PENSUM_VALUE
	MainSection.EVALUATIONS -> EVALUATIONS_VALUE
}

fun String.toMainSectionOrThrow(): MainSection = when (this) {
	SUMMARY_VALUE -> MainSection.SUMMARY
	RECORD_VALUE -> MainSection.RECORD
	PENSUM_VALUE -> MainSection.PENSUM
	EVALUATIONS_VALUE -> MainSection.EVALUATIONS
	LEGACY_ABOUT_VALUE -> MainSection.SUMMARY
	else -> throw IllegalArgumentException("Unknown main section name: $this")
}
