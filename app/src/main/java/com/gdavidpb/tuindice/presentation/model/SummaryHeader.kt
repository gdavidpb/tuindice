package com.gdavidpb.tuindice.presentation.model

data class SummaryHeader(
        val uid: String,
        val name: String,
        val careerName: String,
        val photoUrl: String,
        val grade: Double
) : SummaryBase