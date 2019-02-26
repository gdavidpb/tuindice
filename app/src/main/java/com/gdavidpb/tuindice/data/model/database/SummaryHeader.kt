package com.gdavidpb.tuindice.data.model.database

data class SummaryHeader(
        val uid: String,
        val name: String,
        val careerName: String,
        val photoUrl: String,
        val grade: Double
) : SummaryBase