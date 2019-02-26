package com.gdavidpb.tuindice.domain.model.service

data class DstRecordStats(
        val grade: Double,
        val enrolledSubjects: Int,
        val enrolledCredits: Int,
        val approvedSubjects: Int,
        val approvedCredits: Int,
        val retiredSubjects: Int,
        val retiredCredits: Int,
        val failedSubjects: Int,
        val failedCredits: Int
)