package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.base.domain.model.MainSection

private const val SUMMARY_VALUE = "summary"
private const val RECORD_VALUE = "record"
private const val PENSUM_VALUE = "pensum"
private const val EVALUATIONS_VALUE = "evaluations"
private const val ABOUT_VALUE = "about"

fun MainSection.toPersistedName(): String = when (this) {
	MainSection.SUMMARY -> SUMMARY_VALUE
	MainSection.RECORD -> RECORD_VALUE
	MainSection.PENSUM -> PENSUM_VALUE
	MainSection.EVALUATIONS -> EVALUATIONS_VALUE
	MainSection.ABOUT -> ABOUT_VALUE
}

fun String.toMainSectionOrThrow(): MainSection = when (this) {
	SUMMARY_VALUE -> MainSection.SUMMARY
	RECORD_VALUE -> MainSection.RECORD
	PENSUM_VALUE -> MainSection.PENSUM
	EVALUATIONS_VALUE -> MainSection.EVALUATIONS
	ABOUT_VALUE -> MainSection.ABOUT
	else -> throw IllegalArgumentException("Unknown main section name: $this")
}
