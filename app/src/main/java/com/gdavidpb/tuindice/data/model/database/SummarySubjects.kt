package com.gdavidpb.tuindice.data.model.database

data class SummarySubjects(
        val enrolledSubjects: Int,
        val approvedSubjects: Int,
        val retiredSubjects: Int,
        val failedSubjects: Int
) : SummaryBase