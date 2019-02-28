package com.gdavidpb.tuindice.presentation.model

data class SummaryCredits(
        val enrolledCredits: Int,
        val approvedCredits: Int,
        val retiredCredits: Int,
        val failedCredits: Int
) : SummaryBase