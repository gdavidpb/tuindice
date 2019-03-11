package com.gdavidpb.tuindice.data.model.database

data class AccountEntity(
        val uid: String,
        val id: String,
        val usbId: String,
        val email: String,
        val fullName: String,
        val firstNames: String,
        val lastNames: String,
        val careerName: String,
        val careerCode: Int,
        val scholarship: Boolean,
        val grade: Double,
        val photoUrl: String,
        val enrolledSubjects: Int,
        val enrolledCredits: Int,
        val approvedSubjects: Int,
        val approvedCredits: Int,
        val retiredSubjects: Int,
        val retiredCredits: Int,
        val failedSubjects: Int,
        val failedCredits: Int
)