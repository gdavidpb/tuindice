package com.gdavidpb.tuindice.presentation.model

data class SummaryItem(
        val headerText: CharSequence,
        val enrolled: Int,
        val approved: Int,
        val retired: Int,
        val failed: Int
)