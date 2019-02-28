package com.gdavidpb.tuindice.presentation.model

import java.util.*

data class SummaryHeader(
        val uid: String,
        val name: String,
        val careerName: String,
        val photoUrl: String,
        val grade: Double,
        val lastUpdate: Date
) : SummaryBase