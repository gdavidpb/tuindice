package com.gdavidpb.tuindice.presentation.model

data class SummarySubjects(
        val enrolledSubjects: Int,
        val approvedSubjects: Int,
        val retiredSubjects: Int,
        val failedSubjects: Int
) : SummaryBase