package com.gdavidpb.tuindice.presentation.model

data class SummaryCreditsItem(
        val enrolledCredits: Int,
        val approvedCredits: Int,
        val retiredCredits: Int,
        val failedCredits: Int
) : SummaryItemBase