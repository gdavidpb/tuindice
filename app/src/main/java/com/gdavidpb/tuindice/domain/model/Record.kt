package com.gdavidpb.tuindice.domain.model

data class Record(
        val quarters: List<Quarter>,
        val enrolledSubjects: Int,
        val enrolledCredits: Int,
        val approvedSubject: Int,
        val approvedCredits: Int,
        val retiredSubjects: Int,
        val retiredCredits: Int,
        val failedSubjects: Int,
        val failedCredits: Int
) {
    companion object {
        val EMPTY = Record(listOf(),
                0, 0,
                0, 0,
                0, 0,
                0, 0)
    }
}