package com.gdavidpb.tuindice.data.model.service

data class DstRecord(
        val quarters: List<DstQuarter>,
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
        val EMPTY = DstRecord(listOf(),
                0, 0,
                0, 0,
                0, 0,
                0, 0)
    }
}