package com.gdavidpb.tuindice.presentation.model

data class SummarySubjectsItem(
        val enrolledSubjects: Int,
        val approvedSubjects: Int,
        val retiredSubjects: Int,
        val failedSubjects: Int
) : SummaryItemBase