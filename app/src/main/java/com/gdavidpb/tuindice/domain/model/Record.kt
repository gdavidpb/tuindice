package com.gdavidpb.tuindice.domain.model

data class Record(
        val quarters: List<Quarter> = listOf(),
        val enrolledSubjects: Int = 0,
        val enrolledCredits: Int = 0,
        val approvedSubject: Int = 0,
        val approvedCredits: Int = 0,
        val retiredSubjects: Int = 0,
        val retiredCredits: Int = 0,
        val failedSubjects: Int = 0,
        val failedCredits: Int = 0
)