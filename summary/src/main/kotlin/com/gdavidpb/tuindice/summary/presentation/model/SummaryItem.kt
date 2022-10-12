package com.gdavidpb.tuindice.summary.presentation.model

data class SummaryItem(
	val headerText: CharSequence,
	val enrolled: Int,
	val approved: Int,
	val retired: Int,
	val failed: Int
)