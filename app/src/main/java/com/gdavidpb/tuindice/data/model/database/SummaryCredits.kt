package com.gdavidpb.tuindice.data.model.database

data class SummaryCredits(
        val enrolledCredits: Int,
        val approvedCredits: Int,
        val retiredCredits: Int,
        val failedCredits: Int
) : SummaryBase